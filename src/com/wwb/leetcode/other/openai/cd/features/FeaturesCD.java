package com.wwb.leetcode.other.openai.cd.features;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Features-Focused CD Implementation
 * 
 * Phase 1 (15 min): Basic path navigation
 * Phase 2 (15 min): History tracking and environment variables
 * Phase 3 (15 min): Wildcard pattern matching
 * 
 * Focus: Shell features, user experience, practical functionality
 */
public class FeaturesCD {
    
    // ========================================
    // PHASE 1: Basic Path Navigation
    // ========================================
    
    public String phase1(String currentDir, String targetDir) {
        String fullPath = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
        
        Stack<String> stack = new Stack<>();
        for (String segment : fullPath.split("/")) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            } else if (segment.equals("..")) {
                if (stack.isEmpty()) {
                    return null;
                }
                stack.pop();
            } else {
                stack.push(segment);
            }
        }
        
        return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
    }
    
    
    // ========================================
    // PHASE 2: History & Environment Variables
    // ========================================
    
    /**
     * CD with history tracking and environment variable expansion.
     * 
     * Features:
     * - cd -         : go to previous directory
     * - pushd/popd   : directory stack
     * - $VAR, ${VAR} : environment variable expansion
     * - ~            : home directory expansion
     * - History tracking for analytics
     */
    public static class CDWithHistory {
        private String currentDir = "/";
        private String previousDir = null;
        private final Stack<String> dirStack = new Stack<>();
        private final Deque<String> history = new ArrayDeque<>(100);
        private final Map<String, String> envVars;
        private final String homeDir;
        
        public CDWithHistory(String homeDir, Map<String, String> envVars) {
            this.homeDir = homeDir;
            this.envVars = new HashMap<>(envVars);
            this.envVars.putIfAbsent("HOME", homeDir);
        }
        
        /**
         * Main cd command with all features.
         */
        public String cd(String targetDir) {
            // Handle special cases
            if (targetDir.equals("-")) {
                return cdToPrevious();
            }
            
            // Expand environment variables
            targetDir = expandEnvVars(targetDir);
            
            // Expand tilde
            if (targetDir.equals("~")) {
                targetDir = homeDir;
            } else if (targetDir.startsWith("~/")) {
                targetDir = homeDir + targetDir.substring(1);
            }
            
            // Normalize path
            String newDir = normalize(currentDir, targetDir);
            if (newDir == null) {
                throw new IllegalArgumentException("Cannot navigate above root");
            }
            
            // Update state
            previousDir = currentDir;
            currentDir = newDir;
            recordHistory(newDir);
            
            return currentDir;
        }
        
        /**
         * cd - : Go to previous directory
         */
        private String cdToPrevious() {
            if (previousDir == null) {
                throw new IllegalStateException("No previous directory");
            }
            
            String temp = currentDir;
            currentDir = previousDir;
            previousDir = temp;
            recordHistory(currentDir);
            
            return currentDir;
        }
        
        /**
         * pushd: Push current directory and change to new directory
         */
        public String pushd(String targetDir) {
            dirStack.push(currentDir);
            return cd(targetDir);
        }
        
        /**
         * popd: Pop directory from stack and return to it
         */
        public String popd() {
            if (dirStack.isEmpty()) {
                throw new IllegalStateException("Directory stack empty");
            }
            
            String dir = dirStack.pop();
            previousDir = currentDir;
            currentDir = dir;
            recordHistory(currentDir);
            
            return currentDir;
        }
        
        /**
         * dirs: Show directory stack
         */
        public List<String> dirs() {
            List<String> result = new ArrayList<>();
            result.add(currentDir);
            result.addAll(dirStack);
            return result;
        }
        
        /**
         * Expand environment variables: $VAR or ${VAR}
         */
        private String expandEnvVars(String path) {
            String result = path;
            
            // ${VAR} format
            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                result = result.replace("${" + entry.getKey() + "}", entry.getValue());
            }
            
            // $VAR format (simple word boundary)
            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                result = result.replaceAll("\\$" + entry.getKey() + "(?![a-zA-Z0-9_])", 
                                         entry.getValue());
            }
            
            return result;
        }
        
        /**
         * Record visit in history.
         */
        private void recordHistory(String dir) {
            history.addFirst(dir);
            if (history.size() > 100) {
                history.removeLast();
            }
        }
        
        /**
         * Get history of visited directories.
         */
        public List<String> getHistory() {
            return new ArrayList<>(history);
        }
        
        /**
         * Get most frequently visited directories.
         */
        public List<String> getFrequentDirs(int limit) {
            return history.stream()
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
        
        private String normalize(String currentDir, String targetDir) {
            String fullPath = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
            
            Stack<String> stack = new Stack<>();
            for (String segment : fullPath.split("/")) {
                if (segment.isEmpty() || segment.equals(".")) {
                    continue;
                } else if (segment.equals("..")) {
                    if (stack.isEmpty()) {
                        return null;
                    }
                    stack.pop();
                } else {
                    stack.push(segment);
                }
            }
            
            return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
        }
    }
    
    
    // ========================================
    // PHASE 3: Wildcard Pattern Matching
    // ========================================
    
    /**
     * CD with wildcard support.
     * 
     * Wildcards:
     * - * : matches any sequence of characters
     * - ? : matches single character
     * - [abc] : matches one of a, b, or c
     * - {a,b} : matches a or b (brace expansion)
     */
    public static class CDWithWildcards {
        private final Set<String> validPaths;
        
        public CDWithWildcards(Set<String> validPaths) {
            this.validPaths = validPaths;
        }
        
        /**
         * CD with wildcard expansion.
         * Returns list of matching paths.
         */
        public List<String> cd(String currentDir, String pattern) {
            // No wildcards - single result
            if (!containsWildcard(pattern)) {
                String result = normalize(currentDir, pattern);
                if (result != null && validPaths.contains(result)) {
                    return Collections.singletonList(result);
                }
                throw new IllegalArgumentException("No such directory: " + pattern);
            }
            
            // Expand wildcards
            String fullPattern = pattern.startsWith("/") ? pattern : currentDir + "/" + pattern;
            fullPattern = normalize("/", fullPattern);  // Normalize the pattern
            
            if (fullPattern == null) {
                return Collections.emptyList();
            }
            
            // Find matching paths
            Pattern regex = wildcardToRegex(fullPattern);
            return validPaths.stream()
                .filter(path -> regex.matcher(path).matches())
                .sorted()
                .collect(Collectors.toList());
        }
        
        /**
         * Check if pattern contains wildcards.
         */
        private boolean containsWildcard(String pattern) {
            return pattern.contains("*") || pattern.contains("?") || 
                   pattern.contains("[") || pattern.contains("{");
        }
        
        /**
         * Convert wildcard pattern to regex.
         */
        private Pattern wildcardToRegex(String wildcard) {
            StringBuilder regex = new StringBuilder("^");
            
            for (int i = 0; i < wildcard.length(); i++) {
                char c = wildcard.charAt(i);
                
                switch (c) {
                    case '*':
                        regex.append("[^/]*");  // Match anything except /
                        break;
                    case '?':
                        regex.append("[^/]");   // Match single char except /
                        break;
                    case '.':
                    case '+':
                    case '(':
                    case ')':
                    case '$':
                    case '^':
                    case '|':
                    case '\\':
                        regex.append('\\').append(c);  // Escape regex special chars
                        break;
                    case '[':
                        // Character class - copy until ]
                        int j = wildcard.indexOf(']', i);
                        if (j != -1) {
                            regex.append(wildcard, i, j + 1);
                            i = j;
                        } else {
                            regex.append("\\[");
                        }
                        break;
                    case '{':
                        // Brace expansion {a,b,c} → (a|b|c)
                        int close = findMatchingBrace(wildcard, i);
                        if (close != -1) {
                            String options = wildcard.substring(i + 1, close);
                            String[] parts = options.split(",");
                            regex.append("(");
                            for (int k = 0; k < parts.length; k++) {
                                if (k > 0) regex.append("|");
                                regex.append(Pattern.quote(parts[k]));
                            }
                            regex.append(")");
                            i = close;
                        } else {
                            regex.append("\\{");
                        }
                        break;
                    default:
                        regex.append(c);
                }
            }
            
            regex.append("$");
            return Pattern.compile(regex.toString());
        }
        
        private int findMatchingBrace(String s, int openIndex) {
            int depth = 1;
            for (int i = openIndex + 1; i < s.length(); i++) {
                if (s.charAt(i) == '{') depth++;
                else if (s.charAt(i) == '}') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
            return -1;
        }
        
        private String normalize(String currentDir, String targetDir) {
            String fullPath = targetDir.startsWith("/") ? targetDir : currentDir + "/" + targetDir;
            
            Stack<String> stack = new Stack<>();
            for (String segment : fullPath.split("/")) {
                if (segment.isEmpty() || segment.equals(".")) {
                    continue;
                } else if (segment.equals("..")) {
                    if (stack.isEmpty()) {
                        return null;
                    }
                    stack.pop();
                } else {
                    stack.push(segment);
                }
            }
            
            return stack.isEmpty() ? "/" : "/" + String.join("/", stack);
        }
    }
    
    
    // ========================================
    // TESTS
    // ========================================
    
    public static void main(String[] args) {
        System.out.println("=== PHASE 1: Basic Navigation ===");
        FeaturesCD cd = new FeaturesCD();
        assert "/home/user/docs".equals(cd.phase1("/home/user", "docs"));
        System.out.println("✓ Phase 1 tests passed!\n");
        
        System.out.println("=== PHASE 2: History & Environment Variables ===");
        Map<String, String> envVars = new HashMap<>();
        envVars.put("HOME", "/home/user");
        envVars.put("PROJECT", "/workspace/myproject");
        
        CDWithHistory historyCD = new CDWithHistory("/home/user", envVars);
        
        // Basic cd
        assert "/home/user/docs".equals(historyCD.cd("docs"));
        System.out.println("✓ Basic cd: " + historyCD.currentDir);
        
        // cd - (previous directory)
        historyCD.cd("/etc");
        assert "/home/user/docs".equals(historyCD.cd("-"));
        System.out.println("✓ cd - to previous: " + historyCD.currentDir);
        
        // Environment variable expansion
        assert "/workspace/myproject".equals(historyCD.cd("$PROJECT"));
        System.out.println("✓ $PROJECT expanded to: " + historyCD.currentDir);
        
        assert "/workspace/myproject/src".equals(historyCD.cd("${PROJECT}/src"));
        System.out.println("✓ ${PROJECT}/src expanded to: " + historyCD.currentDir);
        
        // pushd/popd
        historyCD.cd("/home/user");
        historyCD.pushd("/etc");
        historyCD.pushd("/var");
        System.out.println("✓ Directory stack: " + historyCD.dirs());
        assert "/etc".equals(historyCD.popd());
        System.out.println("✓ After popd: " + historyCD.currentDir);
        
        // Frequent directories
        historyCD.cd("/home/user");
        historyCD.cd("/etc");
        historyCD.cd("/home/user");
        historyCD.cd("/etc");
        List<String> frequent = historyCD.getFrequentDirs(3);
        System.out.println("✓ Most frequent dirs: " + frequent);
        
        System.out.println("✓ Phase 2 tests passed!\n");
        
        System.out.println("=== PHASE 3: Wildcard Matching ===");
        Set<String> validPaths = new HashSet<>(Arrays.asList(
            "/home/user1",
            "/home/user2",
            "/home/user123",
            "/home/alice",
            "/home/bob",
            "/home/user1/docs",
            "/home/user1/documents",
            "/home/user2/pics"
        ));
        
        CDWithWildcards wildcardCD = new CDWithWildcards(validPaths);
        
        // * wildcard
        List<String> matches = wildcardCD.cd("/home", "user*");
        System.out.println("✓ /home/user* matches: " + matches);
        assert matches.size() == 3;  // user1, user2, user123
        
        // ? wildcard
        matches = wildcardCD.cd("/home", "use??");
        System.out.println("✓ /home/use?? matches: " + matches);
        assert matches.size() == 0;  // No 5-char names starting with "use"
        
        // Character class [abc]
        matches = wildcardCD.cd("/home", "[ab]*");
        System.out.println("✓ /home/[ab]* matches: " + matches);
        assert matches.size() == 2;  // alice, bob
        
        // Brace expansion {a,b}
        matches = wildcardCD.cd("/home", "{alice,bob}");
        System.out.println("✓ /home/{alice,bob} matches: " + matches);
        assert matches.size() == 2;
        
        System.out.println("✓ Phase 3 tests passed!");
        System.out.println("\n🎉 All feature tests passed!");
    }
}

