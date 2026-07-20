package com.wwb.leetcode.other.temporal.concurrent_slow_api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Problem: call a slow API concurrently and return true if any call returns true.
 *
 * <p>You are given a blocking API that may take 1-10 seconds to respond:
 * <pre>{@code
 * # API that may take 1-10 seconds to respond
 * def shot(index) -> bool:
 *     ...
 * }</pre>
 *
 * <p>Implement:
 * <pre>{@code
 * # Given a list of indices, return True if any call to shot returns True.
 * # Return False only if all calls return False.
 * def vollyShot(indices) -> bool:
 *     ...
 * }</pre>
 *
 * <p>Requirements:
 * <ul>
 *     <li>Use concurrency to speed up total runtime.
 *     <li>Return as soon as any {@code shot(i)} returns true (do not wait for all tasks).
 *     <li>Return false only if all calls return false.
 * </ul>
 *
 * <p>Discuss how you would handle:
 * <ul>
 *     <li>Cancelling/stopping outstanding tasks if supported.
 *     <li>Choosing thread-pool / coroutine-pool size.
 * </ul>
 *
 * <p>The original prompt phrasing ("thread-pool / coroutine-pool size") is Python-flavored and points at two
 * distinct idiomatic solutions there: {@code concurrent.futures.ThreadPoolExecutor} + {@code as_completed(...)}
 * for a blocking {@code shot} (this file's approach — {@link ExecutorCompletionService} is the Java analog of
 * {@code as_completed}), versus {@code asyncio} + {@code Task}/{@code Semaphore} if {@code shot} were instead an
 * {@code async def} returning a future-like handle immediately (Java analog: shot returning a
 * {@code CompletableFuture}, discussed as a hypothetical below). The two need different tools: you cannot feed a
 * pre-existing future into an {@link ExecutorCompletionService} (it only tracks tasks it submitted itself), so
 * the future-returning case forces a callback/combinator style instead. Cancellation also differs sharply from
 * Java's: Python's {@code Future.cancel()} on a thread-pool task is a no-op once the task has started running
 * (no interrupt mechanism exists for threads), whereas {@code asyncio.Task.cancel()} does cooperatively propagate
 * a {@code CancelledError} at the task's next {@code await}. Since {@code shot} is a given, opaque API in either
 * language, the safe default assumption for an interview answer is the pessimistic one either way: cancellation
 * lets us stop waiting on/counting a straggler, not force it to stop running.
 *
 * <p>Constraints (state assumptions if needed):
 * <ul>
 *     <li>{@code len(indices)} can be up to 1e3.
 *     <li>{@code shot} is an IO-bound blocking call (1-10 seconds per call).
 * </ul>
 *
 * <p>Examples:
 * <ul>
 *     <li>{@code indices = [1,2,3]}: if {@code shot(2)=True} and returns first,
 *     {@code vollyShot([1,2,3])} should return true quickly.
 *     <li>{@code indices = [1,2]}: if both return false, return false.
 * </ul>
 */
public class ConcurrentSlowApi {
    // IO-bound work, so threads spend most of their time blocked, not on CPU — pool can safely exceed core count.
    // Capped rather than sized to indices.size() (up to 1e3) so we don't open 1000 concurrent connections to the
    // downstream API; a bounded pool processes the list in waves instead.
    private static final int MAX_POOL_SIZE = 200;
    // Defensive upper bound matching the stated "1-10s per call" contract. This is NOT a correctness requirement
    // (shot() is assumed to always return within it) — it exists purely to stop a contract-violating call from
    // occupying a pool slot indefinitely and starving the remaining waves.
    private static final long PER_CALL_TIMEOUT_SECONDS = 10;

    private final SlowApi api = new SlowApi();

    // ===================================================================================
    // Interview progression: each step below is a complete, standalone solution — read
    // top to bottom. Each answers the "what if...?" question that motivates the next step.
    // ===================================================================================

    /**
     * Step 1 — naive: one thread per call, wait for every one of them, then check the results.
     * Correct (satisfies "true if any, false if all false"), but always pays the cost of the
     * SLOWEST call even when an earlier index was already true — it doesn't "return as soon as
     * any call is true", so it fails the stated requirement. Sets the baseline before optimizing.
     */
    boolean step1NaiveWaitForAll(List<Integer> indices) throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, indices.size()));
        try {
            List<Future<Boolean>> futures = new ArrayList<>(indices.size());
            for (int index : indices) {
                futures.add(pool.submit(() -> api.shot(index)));
            }

            boolean anyTrue = false;
            for (Future<Boolean> future : futures) {
                // get() here blocks in SUBMISSION order, not completion order — if futures.get(0) is the
                // slowest call, we sit on it even though a later future may have already finished true.
                if (future.get()) {
                    anyTrue = true;
                }
            }
            return anyTrue;
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Step 2 — early exit: swap the wait-for-all loop for an {@link ExecutorCompletionService}, which
     * returns whichever submitted task finishes NEXT, in actual completion order. This directly fixes
     * step 1's problem — we return the instant a true arrives, regardless of submission order.
     * Still one thread per call (unbounded), which is the next problem: indices can be ~1e3 long.
     */
    boolean step2EarlyExitCompletionService(List<Integer> indices) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, indices.size()));
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(pool);
        try {
            for (int index : indices) {
                completionService.submit(() -> api.shot(index));
            }

            for (int i = 0; i < indices.size(); i++) {
                try {
                    if (completionService.take().get()) {
                        return true; // remaining tasks are simply abandoned here — cancellation comes in step 3
                    }
                } catch (ExecutionException e) {
                    // a single failed call shouldn't sink the whole volley; real handling arrives in step 4
                }
            }
            return false;
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Step 3 — bounded pool + cancellation: cap concurrency instead of one-thread-per-call, and stop
     * wasting that bounded capacity on stragglers once an early true is found. {@code shot} is IO-bound
     * (blocked waiting, not burning CPU), so the pool can exceed core count, but with up to 1e3 indices,
     * one real thread each would open ~1000 concurrent connections to the downstream API and risk
     * exhausting OS threads. A bounded pool processes the list in waves instead.
     *
     * <p>{@code pool.shutdownNow()} in the {@code finally} below is doing double duty, and that's
     * deliberate: {@link ExecutorService#shutdownNow()} already interrupts every currently-running task
     * and drains every task still queued so it never starts at all — the exact same effect a separate,
     * explicit {@code futures.forEach(future -> future.cancel(true))} loop would have (verified: calling
     * only {@code shutdownNow()}, with no per-future {@code cancel()} anywhere, still interrupted two
     * in-flight tasks immediately and reported three queued-but-never-started tasks as drained). So no
     * separate "now add cancellation" step is needed — tracking individual futures just to cancel each
     * one explicitly would be redundant work this shutdown call already does for free. Same caveat either
     * way: this relies on {@code shot()}'s blocking call being interruption-aware; if it ignores
     * interrupts, the thread keeps running for its real duration regardless of which mechanism asked it to
     * stop.
     */
    boolean step3BoundedPool(List<Integer> indices) throws InterruptedException {
        int poolSize = Math.min(Math.max(1, indices.size()), MAX_POOL_SIZE);
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(pool);
        try {
            for (int index : indices) {
                completionService.submit(() -> api.shot(index));
            }

            for (int i = 0; i < indices.size(); i++) {
                try {
                    if (completionService.take().get()) {
                        return true; // stragglers cancelled by shutdownNow() below, best-effort per the caveat above
                    }
                } catch (ExecutionException e) {
                    // ignored for the same reason as step 2
                }
            }
            return false;
        } finally {
            // Cancels every remaining task too (see javadoc) -- without this, an early return would leave
            // every other future running to completion.
            pool.shutdownNow();
        }
    }

    /**
     * Step 4 — timeout + fault tolerance. Adds a watchdog that bounds a single call violating the stated
     * 1-10s contract, and treats a timed-out/cancelled call the same as a failed one — a single bad call
     * must not sink the whole volley, since the contract is "false only if ALL calls return false".
     */
    boolean step4TimeoutAndFaultTolerance(List<Integer> indices) throws InterruptedException {
        if (indices.isEmpty()) {
            return false;
        }

        int poolSize = Math.min(indices.size(), MAX_POOL_SIZE);
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        // Single extra thread, shared by all scheduled cancellations below; it only ever schedules cheap
        // cancel() calls, so it doesn't need its own pool.
        ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor();
        // CompletionService (not a plain List<Future>) is what makes "return as soon as any call is true" cheap:
        // take() hands back whichever submitted task finished next, in actual completion order, so a slow call
        // submitted first never blocks a fast call submitted later from being observed.
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(pool);
        List<Future<Boolean>> futures = new ArrayList<>(indices.size());

        try {
            for (int index : indices) {
                Future<Boolean> future = completionService.submit(() -> api.shot(index));
                futures.add(future);
                // Guard against a call that violates the stated 1-10s contract; cancel() is a no-op if it already
                // finished. CAVEAT: cancel(true) only sends an interrupt — it relies on shot()'s underlying
                // blocking call (e.g. a socket read) being interruption-aware. If shot() ignores interrupts, the
                // thread keeps running for its full real duration; we just stop waiting on/counting its result,
                // and it occupies a pool slot until it finishes on its own. A hard bound requires the API itself
                // to expose a timeout (e.g. a socket/read timeout), which this in-process cancellation cannot force.
                watchdog.schedule(() -> future.cancel(true), PER_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }

            for (int i = 0; i < indices.size(); i++) {
                try {
                    if (completionService.take().get()) { // blocks until the next call finishes, in completion order
                        return true; // first true wins; stragglers get cancelled in finally, best-effort per above
                    }
                } catch (ExecutionException | CancellationException e) {
                    // A single failed or timed-out call must not sink the whole volley — the contract is "false
                    // only if ALL calls return false", so we treat an unusable result as false and keep checking
                    // the rest rather than propagating the exception.
                }
            }
            return false; // every call finished with false
        } finally {
            // Runs whether we returned true early or exhausted the loop. In the early-return case this is the
            // only place remaining calls get cancelled — without it, stragglers would keep running to completion
            // (or the watchdog deadline) even though the answer is already known, wasting pool capacity.
            futures.forEach(future -> future.cancel(true)); // best-effort interrupt of anything still running
            watchdog.shutdownNow();
            pool.shutdownNow();
        }
    }

    /**
     * Step 5 — admission control. Layers a counting {@link Semaphore} on top of step 4 so the number of
     * calls actually in flight is gated independently of thread-pool size (e.g. a downstream rate limit
     * tighter than the pool), without sacrificing early-exit responsiveness.
     *
     * <p>ASSUMPTION this whole method leans on, stated explicitly because everything below depends on it:
     * {@link SlowApi} always eventually terminates — either it returns normally, or it genuinely honors
     * {@code cancel(true)}'s interrupt and returns promptly. It never blocks forever ignoring the
     * interrupt. Under that assumption, releasing the permit from exactly ONE place — the submitted
     * {@link Callable}'s own {@code finally} — is safe: that {@code finally} is guaranteed to run within
     * bounded time regardless of whether the call finished naturally or was cancelled, so no second
     * release path (and no {@code AtomicBoolean}-style guard) is needed anywhere in this method.
     *
     * <p>Submission runs on its own single {@code submitter} thread, decoupled from the consumer's
     * {@code take()} loop below. Without this split, a {@code true} discovered early during submission
     * would sit unread until every remaining index finishes submitting — each submission itself blocks on
     * {@code admission.acquire()} once {@code poolSize} permits are exhausted — silently reintroducing
     * step 1's "wait for everything" latency whenever {@code indices.size() > poolSize}. Splitting
     * submission onto its own thread lets the consumer react to a completion the moment it happens,
     * independent of how much submission work remains.
     *
     * <p>If the "never hangs" assumption above is ever wrong — a callee that ignores its interrupt and
     * blocks forever — this design fails in two ways at once: the permit for that call leaks forever (its
     * {@code finally} never runs), eventually starving {@code admission} once enough leaks accumulate; and
     * {@link CompletionService#take()} blocks forever on that specific index, since the completion-queue
     * push is tied to that same call's own thread reaching {@code done()} — which never happens. A version
     * hardened against a non-cooperative callee (a "supervisor" per index, owning its own bounded
     * {@code Future.get(timeout)} so neither failure mode above can occur, with no guard needed) exists in
     * this file's git history; it was reverted specifically because it's unneeded machinery when this
     * assumption holds, as it does for this file's {@link SlowApi}.
     */
    boolean step5AdmissionControl(List<Integer> indices) throws InterruptedException {
        if (indices.isEmpty()) {
            return false;
        }

        int poolSize = Math.min(indices.size(), MAX_POOL_SIZE);
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        // Single extra thread, shared by all scheduled cancellations below; same as step 4.
        ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor();
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(pool);
        // Written by the submitter thread below, read by the main thread only in the finally cleanup —
        // needs to be thread-safe; a lock-free queue is enough since nothing here needs indexed access.
        Queue<Future<Boolean>> futures = new ConcurrentLinkedQueue<>();
        // Sized to poolSize here, but could be smaller/independent — e.g. capped to a downstream API's own
        // concurrent-call limit even if we're willing to run more OS threads.
        Semaphore admission = new Semaphore(poolSize);

        // Dedicated single thread for the admission-gated submission loop — see javadoc: without this
        // split, an early true wouldn't be observed until every index finishes submitting.
        ExecutorService submitter = Executors.newSingleThreadExecutor();
        Future<?> submission = submitter.submit(() -> {
            try {
                for (int index : indices) {
                    admission.acquire(); // blocks only the submitter thread, never the consumer below
                    Future<Boolean> future = completionService.submit(() -> {
                        try {
                            return api.shot(index);
                        } finally {
                            // Single release path — always runs, exactly once, within bounded time per the
                            // "never hangs" assumption above. No second writer ever competes for this.
                            admission.release();
                        }
                    });
                    futures.add(future);
                    // Guard against a call violating the stated 1-10s contract; same caveat as step 4's
                    // watchdog — cancel(true) only helps if shot() actually honors the interrupt.
                    watchdog.schedule(() -> future.cancel(true), PER_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // told to stop early — a true was already found below
            }
        });

        try {
            for (int i = 0; i < indices.size(); i++) {
                try {
                    if (completionService.take().get()) { // blocks until the next call finishes, in completion order
                        return true; // stragglers cancelled below, best-effort
                    }
                } catch (ExecutionException | CancellationException e) {
                    // same reasoning as step 4
                }
            }
            return false;
        } finally {
            submission.cancel(true); // stop the submitter loop if it's still admitting more indices
            submitter.shutdownNow();
            futures.forEach(future -> future.cancel(true)); // best-effort interrupt of anything still running
            watchdog.shutdownNow();
            pool.shutdownNow();
        }
    }

    /**
     * Step 5b — the exact same design as step 5, just without {@link CompletionService}: each submitted
     * task pushes its own outcome directly into a hand-rolled {@code BlockingQueue<Boolean>} instead of
     * {@code return}ing it for {@code take()} to pick up. That's the entire diff — a primitive swap, not a
     * different guarantee. The same "never hangs" assumption, the same single-release-in-{@code finally}
     * reasoning, and the same dedicated-submitter-thread justification from step 5's javadoc all apply
     * here unchanged.
     *
     * <p>One genuine asymmetry worth documenting: {@link CompletionService#take()} is guaranteed by the
     * JDK to surface exactly one entry per submitted task no matter how it ends — normal return, thrown
     * exception, or cancellation. A hand-rolled queue gets no such guarantee for free — it's only as safe
     * as the submitted task's own code, which must push exactly once on every exit path itself. That holds
     * here only because {@link SlowApi#shot} never throws — it catches its own {@code InterruptedException}
     * internally and returns {@code false} normally (see {@code SlowApi}'s javadoc) — so there is no
     * exception path capable of skipping the push below. A real API that could throw would need an
     * explicit try/catch pushing {@code false} on the exception path too; without it, {@code outcomes}
     * would silently receive one fewer entry than {@code indices.size()} for that call, hanging the
     * consumer loop below forever on its last {@code take()}.
     */
    boolean step5bManualOutcomeQueue(List<Integer> indices) throws InterruptedException {
        if (indices.isEmpty()) {
            return false;
        }

        int poolSize = Math.min(indices.size(), MAX_POOL_SIZE);
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor();
        BlockingQueue<Boolean> outcomes = new LinkedBlockingQueue<>();
        Queue<Future<?>> futures = new ConcurrentLinkedQueue<>();
        Semaphore admission = new Semaphore(poolSize);

        ExecutorService submitter = Executors.newSingleThreadExecutor();
        Future<?> submission = submitter.submit(() -> {
            try {
                for (int index : indices) {
                    admission.acquire(); // blocks only the submitter thread, never the consumer below
                    Future<?> future = pool.submit(() -> {
                        boolean result;
                        try {
                            result = api.shot(index);
                        } finally {
                            admission.release(); // single release path, same reasoning as step 5
                        }
                        outcomes.add(result); // single push -- see javadoc's asymmetry note
                    });
                    futures.add(future);
                    watchdog.schedule(() -> future.cancel(true), PER_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // told to stop early — a true was already found below
            }
        });

        try {
            for (int i = 0; i < indices.size(); i++) {
                if (outcomes.take()) {
                    return true; // stragglers cancelled below, best-effort
                }
            }
            return false;
        } finally {
            submission.cancel(true);
            submitter.shutdownNow();
            futures.forEach(future -> future.cancel(true)); // best-effort interrupt of anything still running
            watchdog.shutdownNow();
            pool.shutdownNow();
        }
    }

    /**
     * Step 6 — pure producer/consumer: replace the {@link Semaphore} + {@link ExecutorCompletionService} with
     * two explicit queues, the textbook producer/consumer shape.
     * <ul>
     *     <li>Work queue ({@code BlockingQueue<Optional<Integer>>}, bounded to poolSize): a dedicated producer
     *     thread feeds indices into it. {@code put()} blocking once the queue is full IS the admission control
     *     now — the {@link Semaphore} from step 5 is gone; bounded-queue backpressure does the same job.
     *     <li>A fixed pool of worker threads loop {@code take()}-ing from the work queue, calling {@code shot},
     *     and {@code put()}-ing the boolean result onto a results queue.
     *     <li>The main thread is the final consumer: {@code take()} from the results queue, return true on the
     *     first true, false once every result has arrived.
     * </ul>
     *
     * <p>Poison pills ({@code Optional.empty()}, one per worker) tell workers to stop once every index has been
     * enqueued — plain {@code null} isn't usable here since {@link BlockingQueue} rejects null elements.
     *
     * <p>PER-CALL TIMEOUT: a worker's own thread must never be the thing a watchdog/timeout interrupts — that
     * would conflate two different lifetimes onto one thread identity: the worker (must survive across many
     * calls) and one call's cancellation (must be scoped to exactly that call). Interrupting the worker
     * directly risks a stray interrupt flag surviving into its NEXT loop iteration and killing the worker
     * outright. The fix does NOT require a disposable executor per call, though: {@link ThreadPoolExecutor}'s
     * own dispatch loop already clears any stray interrupt flag before starting the next DISTINCT submitted
     * task (the same reason step 4/5 never had this problem despite reusing pool threads across many
     * {@code Callable}s). So each worker submits {@code shot(index)} as its own task to one shared, reused
     * {@code callPool} (created once, outside the loop) and bounds it with {@code future.get(timeout)} on the
     * one future it owns — no watchdog thread needed here, since (unlike step 4's {@code take()}-whichever's
     * -done-next) each worker already holds the specific future it's waiting on. {@code get(timeout)} throwing
     * is what frees the worker, unconditionally; the {@code cancel(true)} that follows is a separate,
     * best-effort attempt to actually stop the call. Mirrors {@code asyncio.wait_for}'s shape, not its
     * cooperative-cancellation guarantee — unlike step 4/5's {@code take()}-whichever's-done-next (which
     * relies on a separate watchdog racing the call's own completion), each worker here already holds the
     * specific future it's waiting on directly, so no watchdog thread is needed at all.
     *
     * <p>TRADE-OFF vs step 5, not a strict improvement: this needs a SECOND thread pool ({@code callPool})
     * specifically because workers are long-lived and reused across many calls, so {@code shot(index)}
     * can't run inline on a worker's own thread (see the interrupt-bleed reasoning above). Total cost is
     * ~2×{@code poolSize} threads (workers + callPool) plus one producer thread, vs step 5's ~{@code
     * poolSize} threads (one pool) plus one submitter thread and one shared watchdog thread — step 6 is
     * consistently the more expensive shape here. {@code vollyShot} still delegates to step 5; this step is
     * presented as an alternative model for the "build it from raw queues" discussion, not a replacement.
     */
    boolean step6PureProducerConsumer(List<Integer> indices) throws InterruptedException {
        if (indices.isEmpty()) {
            return false;
        }

        int poolSize = Math.min(indices.size(), MAX_POOL_SIZE);
        // Capacity IS the admission control: put() blocks the producer once poolSize items are queued/in
        // flight, without a separate Semaphore.
        BlockingQueue<Optional<Integer>> workQueue = new ArrayBlockingQueue<>(poolSize);
        BlockingQueue<Boolean> resultsQueue = new LinkedBlockingQueue<>();

        ExecutorService workers = Executors.newFixedThreadPool(poolSize);
        // Shared for the whole run — NOT recreated per call. Submitting each shot(index) here as its own
        // distinct task (rather than calling it inline in the worker's loop) is what lets a timeout's
        // cancel(true) target this pool's thread safely: ThreadPoolExecutor clears any stray interrupt left
        // over from a cancelled task before dispatching its next task, so a timeout on call N can never
        // bleed into call N+1 the way it would if we interrupted a worker's own persistent loop thread.
        ExecutorService callPool = Executors.newFixedThreadPool(poolSize);
        for (int i = 0; i < poolSize; i++) {
            workers.submit(() -> {
                try {
                    while (true) {
                        Optional<Integer> item = workQueue.take();
                        if (item.isEmpty()) {
                            return; // poison pill: producer has no more work coming
                        }

                        int index = item.get();
                        boolean result;
                        Future<Boolean> callFuture = callPool.submit(() -> api.shot(index));
                        try {
                            result = callFuture.get(PER_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        } catch (TimeoutException e) {
                            // get(timeout) throwing here is what frees this worker, unconditionally —
                            // independent of whether cancel(true) below actually stops the real call.
                            callFuture.cancel(true); // best-effort; may leak this callPool thread if ignored
                            result = false;
                        } catch (ExecutionException e) {
                            // same reasoning as step 2/4: a single failed call must not sink the whole volley
                            result = false;
                        }
                        resultsQueue.put(result);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // told to stop early, e.g. a true was already found
                }
            });
        }

        // Producer: on its own thread so blocking on workQueue.put() (admission control) never delays the
        // consumer loop below from reacting to a result that's already in the results queue.
        ExecutorService producer = Executors.newSingleThreadExecutor();
        Future<?> production = producer.submit(() -> {
            try {
                for (int index : indices) {
                    workQueue.put(Optional.of(index));
                }
                for (int i = 0; i < poolSize; i++) {
                    workQueue.put(Optional.empty()); // one pill per worker so every worker can observe one and exit
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        try {
            for (int i = 0; i < indices.size(); i++) {
                if (resultsQueue.take()) {
                    return true; // remaining workers/producer stopped in finally below, best-effort
                }
            }
            return false;
        } finally {
            production.cancel(true);
            producer.shutdownNow();
            workers.shutdownNow(); // best-effort interrupt of any worker still blocked/running; same caveat as step 3+
            callPool.shutdownNow(); // best-effort interrupt of any in-flight shot() call; same caveat as step 3+
        }
    }

    /**
     * The canonical entry point matching the prompt's required signature ({@code def vollyShot(indices) -> bool}).
     * Delegates to {@link #step5AdmissionControl}, the most refined version in the progression above.
     */
    boolean vollyShot(List<Integer> indices) throws InterruptedException {
        return step5AdmissionControl(indices);
    }

    /**
     * Tracing harness: runs the same indices through every step and prints elapsed time + result, so the
     * step-1 vs step-2+ difference (wait-for-all vs early-exit) is directly observable instead of inferred.
     * The per-shot() log lines (see {@link SlowApi}) additionally show, for each step, how many calls were
     * still running when the method returned vs. cancelled.
     */
    public static void main(String[] args) throws Exception {
        ConcurrentSlowApi solver = new ConcurrentSlowApi();
        List<Integer> indices = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());

        runStep("step1NaiveWaitForAll", () -> solver.step1NaiveWaitForAll(indices));
        runStep("step2EarlyExitCompletionService", () -> solver.step2EarlyExitCompletionService(indices));
        runStep("step3BoundedPool", () -> solver.step3BoundedPool(indices));
        runStep("step4TimeoutAndFaultTolerance", () -> solver.step4TimeoutAndFaultTolerance(indices));
        runStep("step5AdmissionControl", () -> solver.step5AdmissionControl(indices));
        runStep("step5bManualOutcomeQueue", () -> solver.step5bManualOutcomeQueue(indices));
        runStep("step6PureProducerConsumer", () -> solver.step6PureProducerConsumer(indices));
        runStep("vollyShot (delegates to step5)", () -> solver.vollyShot(indices));
    }

    private static void runStep(String label, Callable<Boolean> step) throws Exception {
        System.out.println("--- " + label + " ---");
        long startNanos = System.nanoTime();
        boolean result = step.call();
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
        System.out.printf("%-32s result=%-5s elapsed=%dms%n%n", label, result, elapsedMillis);
    }
}
