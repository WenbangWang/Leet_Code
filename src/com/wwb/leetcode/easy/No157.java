package com.wwb.leetcode.easy;

/**
 * The API: int read4(char *buf) reads 4 characters at a time from a file.
 *
 * The return value is the actual number of characters read.
 * For example, it returns 3 if there is only 3 characters left in the file.
 *
 * By using the read4 API, implement the function int read(char *buf, int n) that reads n characters from the file.
 *
 * Note:
 * The read function will only be called once for each test case.
 */
public class No157 {
    /**
     * @param buf Destination buffer
     * @param n   Maximum number of characters to read
     * @return    The number of characters read
     */
    public int read(char[] buf, int n) {
        char[] buffer = new char[4];
        boolean isEOF = false;
        int pointer = 0;

        while(!isEOF && pointer < n) {
            int numberOfCharRead = read4(buffer);

            isEOF = numberOfCharRead < 4;

            int charToBeConsumed = Math.min(n - pointer, numberOfCharRead);

            for(int i = 0; i < charToBeConsumed; i++) {
                buf[pointer++] = buffer[i];
            }
        }

        return pointer;
    }

    private int read4(char[] buf) {
        return 0;
    }
}
