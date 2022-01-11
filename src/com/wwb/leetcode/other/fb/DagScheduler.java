package com.wwb.leetcode.other.fb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description
 * Given a DAG (Directed acyclic graph) and write a scheduler to decide the order of scheduling DAG.
 * <p>
 * Question Statement
 * A DAG is used to represent a workflow or a query plan. The node in the DAG is a stage of a computation. The edge in the DAG represents the dependency between stages. If there is a directed edge from stage x to stage y, it means the stage y can be scheduled only after the stage x is done. In a DAG, each stage is marked by a unique integer.
 * This question is to implement a scheduler class to do DAG scheduling. It consists of 3 sub-questions:
 * The Constructor for Scheduler class: The DAG is represented as a hash map. The key of the hash map is the id of a stage and the value is a list of stages depending on this stage. Input DAG is immutable.
 * List<Integer> getStagesToRun(int cap): return a list of runnable stages and the size of the list is capped by cap. The requirement of this function is to return a runnable stage ONLY ONCE. By runnable, it means the stage doesn't depend on any stage or all the stages depended by this stage have been marked as done stage.
 * void markStageDone(int stageId): use this function to notify the scheduler a returned stage is done, so the stages depending on this stage may become runnable.
 *       1
 *      / \
 *     2   5
 *    / \ /
 *   3   4
 *   Input: {1: [2, 5], 2: [3, 4], 5: [4]} (Didn't include 3 and 4 since they don't have any dependents)
 *
 *   One Potential order of execution: 1 -> 2 -> 5 -> 3 -> 4
 *
 *   Manager:
 *    Create a DagScheduler
 *    for each stageId in getStagesToRun()
 *      schedule stage with stageId
 *
 *    for each stageId scheduled AND completed
 *      markStageDone(stageId)
 * <p>
 * A couple of points we need to pay attention to when asking this question:
 * Time and space complexity analysis of the solution.
 * Ensure a runnable stage only got returned once.
 * Some corner cases like if the markStageDone is called multiple times for the same stage or for a non-existing stage.
 * If a candidate gave a brute force solution like scanning the DAG every time in getStagesToRun, he/she needs able to optimize his solution with hints.
 * We also can let the candidate decide what data structure can be used to represent a DAG, but we need to make sure the directed edge means the end point depends on the source point.
 * <p>
 * A preferred solution:
 * Constructor. Record the input DAG as a data member. Scan the input DAG once and build the in-degree map for each stage. Having a candidate list of runnable stage. Populate the list by adding stages with in-degree 0.
 * getStagesToRun. Return whatever in the candidate list and capped by the cap. Once a stage is returned, we can remove from this list, so a stage will be returned only once.
 * markStageDone. Use the id to get the list of stages depending on it by querying the input DAG, and then minus the in-degree of each stage by 1. If the in-degree becomes 0, add the stage to the candidate list.
 */
public class DagScheduler {
  private Map<Integer, List<Integer>> dag;
  private Map<Integer, Integer> inDegrees;
  private Set<Integer> candidates;

  public DagScheduler(Map<Integer, List<Integer>> dag) {
    this.dag = dag;
    inDegrees = new HashMap<>();
    candidates = new HashSet<>();

    populateInDegrees();
    populateStageCandidates();
  }

  public List<Integer> getStagesToRun(int parallelism) {
    List<Integer> stagesToRun = new ArrayList<>();

    // Note min here
    for (int i = 0; i < Math.min(parallelism, candidates.size()); i++) {
      // how to iterate and remove element from existing set
      int stageToRun = candidates.iterator().next();
      stagesToRun.add(stageToRun);
      candidates.remove(stageToRun);
    }

    return stagesToRun;
  }

  public void markStageDone(int stageId) {
    if (dag.containsKey(stageId)) {
      List<Integer> dependents = dag.get(stageId);

      dependents
        .stream()
        // check if the stage is already marked as done
        .filter(dependent -> inDegrees.getOrDefault(dependent, 0) != 0)
        .forEach(dependent -> {
          // minus in degree
          inDegrees.computeIfPresent(dependent, (dep, inDegree) -> inDegree - 1);

          // inline edit candidates or re-populate with populateStageCandidates
          if (inDegrees.containsKey(dependent) && inDegrees.get(dependent) == 0) {
            candidates.add(dependent);
          }
        });
    }
  }

  private void populateInDegrees() {
    dag.forEach((k, v) -> {
      // what if the last node does not have any dependents?
      // need to ensure all nodes are initialized in inDegrees map
      inDegrees.putIfAbsent(k, 0);
      v.forEach(edge -> {
        inDegrees.putIfAbsent(edge, 0);
        inDegrees.computeIfPresent(edge, (e, inDegree) -> inDegree + 1);
      });
    });
  }

  private void populateStageCandidates() {
    candidates = inDegrees
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue() == 0)
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());
  }
}
