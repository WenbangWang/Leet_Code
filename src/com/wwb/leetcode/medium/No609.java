package com.wwb.leetcode.medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a list paths of directory info, including the directory path, and all the files with contents in this directory, return all the duplicate files in the file system in terms of their paths. You may return the answer in any order.
 * <p>
 * A group of duplicate files consists of at least two files that have the same content.
 * <p>
 * A single directory info string in the input list has the following format:
 * <p>
 * "root/d1/d2/.../dm f1.txt(f1_content) f2.txt(f2_content) ... fn.txt(fn_content)"
 * It means there are n files (f1.txt, f2.txt ... fn.txt) with content (f1_content, f2_content ... fn_content) respectively in the directory "root/d1/d2/.../dm". Note that n >= 1 and m >= 0. If m = 0, it means the directory is just the root directory.
 * <p>
 * The output is a list of groups of duplicate file paths. For each group, it contains all the file paths of the files that have the same content. A file path is a string that has the following format:
 * <p>
 * "directory_path/file_name.txt"
 *
 *
 * <pre>
 * Example 1:
 *
 * Input: paths = ["root/a 1.txt(abcd) 2.txt(efgh)","root/c 3.txt(abcd)","root/c/d 4.txt(efgh)","root 4.txt(efgh)"]
 * Output: [["root/a/2.txt","root/c/d/4.txt","root/4.txt"],["root/a/1.txt","root/c/3.txt"]]
 * Example 2:
 *
 * Input: paths = ["root/a 1.txt(abcd) 2.txt(efgh)","root/c 3.txt(abcd)","root/c/d 4.txt(efgh)"]
 * Output: [["root/a/2.txt","root/c/d/4.txt"],["root/a/1.txt","root/c/3.txt"]]
 *
 *
 * Constraints:
 *
 * 1 <= paths.length <= 2 * 104
 * 1 <= paths[i].length <= 3000
 * 1 <= sum(paths[i].length) <= 5 * 105
 * paths[i] consist of English letters, digits, '/', '.', '(', ')', and ' '.
 * You may assume no files or directories share the same name in the same directory.
 * You may assume each given directory info represents a unique directory. A single blank space separates the directory path and file info.
 * </pre>
 *
 * <pre>
 * Follow up:
 *
 * Imagine you are given a real file system, how will you search files? DFS or BFS?
 * If the file content is very large (GB level), how will you modify your solution?
 * If you can only read the file by 1kb each time, how will you modify your solution?
 * What is the time complexity of your modified solution? What is the most time-consuming part and memory-consuming part of it? How to optimize?
 * How to make sure the duplicated files you find are not false positive?
 * </pre>
 */
public class No609 {
    public List<List<String>> findDuplicate(String[] paths) {
        List<File> dirs = Arrays.stream(paths).map(this::parse).toList();
        Map<String, List<File>> contentToFile = new HashMap<>();

        for (File dir : dirs) {
            for (File file : dir.listFiles()) {
                contentToFile.putIfAbsent(file.getContent(), new ArrayList<>());
                contentToFile.get(file.getContent()).add(file);
            }
        }

        return contentToFile
            .values()
            .stream()
            .filter(files -> files.size() != 1)
            .map(
                files -> files.stream().map(File::getPath).toList()
            )
            .toList();
    }

    private File parse(String s) {
        int index = 0;

        while (index < s.length() && s.charAt(index) != ' ') {
            index++;
        }

        String dirName = s.substring(0, index);
        List<File> files = new ArrayList<>();
        File dir = new Directory(files, dirName);

        while (index < s.length()) {
            if (s.charAt(index) == ' ') {
                index++;
                continue;
            }

            int start = index;

            while (index < s.length() && s.charAt(index) != '(') {
                index++;
            }

            String fileName = s.substring(start, index);

            // skip (
            index++;
            start = index;

            while (index < s.length() && s.charAt(index) != ')') {
                index++;
            }

            files.add(new File(s.substring(start, index), fileName, dirName));
            // skip )
            index++;
        }

        return dir;
    }

    private static class File {
        String content;
        String name;
        String parent;

        File(String content, String name, String parent) {
            this.content = content;
            this.name = name;
            this.parent = parent;
        }

        String getName() {
            return this.name;
        }

        String getPath() {
            return this.parent == null ? this.getName() : this.parent + "/" + this.getName();
        }

        boolean isDirectory() {
            return false;
        }

        List<File> listFiles() {
            return Collections.emptyList();
        }

        String getContent() {
            return this.content;
        }
    }

    private static class Directory extends File {
        List<File> files;

        Directory(List<File> files, String name) {
            super(null, name, null);
            this.files = files;
        }

        List<File> listFiles() {
            return files;
        }

        @Override
        boolean isDirectory() {
            return true;
        }
    }
}
