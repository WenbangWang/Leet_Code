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
        int[] pointer = new int[1];

        while(pointer[0] < n) {
            this.ensureBuffer();

            this.copyInto(buf, pointer, n);

            this.resetBufferPointerIfAllConsumed();

            if(this.isEOF()) {
                break;
            }
        }

        return pointer[0];
    }

    private void ensureBuffer() {
        if (this.tempBufferPointer == 0) {
            this.numberOfCharactersRead = this.read4(this.tempBuffer);
        }
    }

    private boolean isEOF () {
        return this.numberOfCharactersRead < 4;
    }

    private void resetBufferPointerIfAllConsumed () {
        if(this.tempBufferPointer == this.numberOfCharactersRead) {
            this.tempBufferPointer = 0;
        }
    }

    private void copyInto(char[] buf, int[] pointer, int numberOfCharsNeeded) {
        while (pointer[0] < numberOfCharsNeeded && this.tempBufferPointer < this.numberOfCharactersRead) {
            buf[pointer[0]++] = this.tempBuffer[this.tempBufferPointer++];
        }
    }

    private int read4(char[] buf) {
        return 0;
    }
}
