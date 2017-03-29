package com.wwb.leetcode.hard;

/**
 * The API: int read4(char *buf) reads 4 characters at a time from a file.
 *
 * The return value is the actual number of characters read.
 * For example, it returns 3 if there is only 3 characters left in the file.
 *
 * By using the read4 API, implement the function int read(char *buf, int n) that reads n characters from the file.
 *
 * Note:
 * The read function may be called multiple times.
 */
public class No158 {
    private char[] tempBuffer = new char[4];
    private int tempBufferPointer = 0;
    private int numberOfCharactersRead = 0;
    /**
     * @param buf Destination buffer
     * @param n   Maximum number of characters to read
     * @return    The number of characters read
     */
    public int read(char[] buf, int n) {
        int pointer = 0;

        while(pointer < n) {
            if(tempBufferPointer == 0) {
                numberOfCharactersRead = read4(tempBuffer);
            }

            while(pointer < n && tempBufferPointer < numberOfCharactersRead) {
                buf[pointer++] = tempBuffer[tempBufferPointer++];
            }

            // consumed all chars read.
            if(tempBufferPointer == numberOfCharactersRead) {
                tempBufferPointer = 0;
            }

            // end of file.
            if(numberOfCharactersRead < 4) {
                break;
            }
        }

        return pointer;
    }

    private int read4(char[] buf) {
        return 0;
    }
}
