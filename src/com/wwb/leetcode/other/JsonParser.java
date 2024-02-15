package com.wwb.leetcode.other;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JsonParser {

    public static void main(String[] args) {
        Tokenizer t = new Tokenizer("{\"browsers\":{\"firefox\":{\"name\":\"Firefox\",\"pref_url\":\"about:config\",\"releases\":{\"1\":{\"release_date\":\"2004-11-09\",\"status\":\"retired\",\"engine\":\"Gecko\",\"engine_version\":1,\"test_null\":null,\"test_true\":true,\"test_false\":false}}}}}");

        System.out.println(t.getTokens());
    }

    private static class Tokenizer {
        private String jsonString;
        private int index;
        private Set<CharBuffer> consumableSingleChars;
        private Set<CharBuffer> numericChars;

        public Tokenizer(String jsonString) {
            this.jsonString = jsonString;
            this.index = 0;
            this.consumableSingleChars = Set.of(
                Constants.LEFT_CURL_BRACKET.value,
                Constants.RIGHT_CURL_BRACKET.value,
                Constants.LEFT_SQUARE_BRACKET.value,
                Constants.RIGHT_SQUARE_BRACKET.value,
                Constants.COLON.value,
                Constants.COMMA.value
            );
            this.numericChars = IntStream.range(0, 10)
                .mapToObj(i -> CharBuffer.wrap(i + ""))
                .collect(Collectors.toSet());
        }

        public List<Object> getTokens() {
            List<Object> result = new ArrayList<>();

            while (index != this.jsonString.length()) {
                CharBuffer c = this.read();
                if (c.equals(Constants.WHITE_SPACE.value)) {
                    this.consume();
                    continue;
                }

                if (this.consumableSingleChars.contains(c)) {
                    result.add(this.consume().toString());
                    continue;
                }

                if (c.equals(Constants.QUOTE.value)) {
                    // skip quote
                    this.consume();
                    result.add(this.consume(this.string()).toString());
                    // skip quote
                    this.consume();
                    continue;
                }

                if (this.numericChars.contains(c)) {
                    result.add(Integer.parseInt(this.consume(this.number()).toString()));
                    continue;
                }

                if (c.equals(Constants.T.value) || c.equals(Constants.F.value)) {
                    int l = this.bool();

                    if (l == 4 || l == 5) {
                        result.add(Boolean.parseBoolean(this.consume(l).toString()));
                        continue;
                    }
                }

                if (c.equals(Constants.N.value)) {
                    int l = this.nil();

                    if (l == 4) {
                        this.consume(l);
                        result.add(null);
                        continue;
                    }
                }

                throw new RuntimeException("Unknown char " + c);
            }


            return result;
        }

        private CharBuffer read() {
            return this.read(1);
        }

        private CharBuffer read(int length) {
            return CharBuffer.wrap(this.jsonString, this.index, this.index +  length);
        }

        private CharBuffer read(int start, int offset) {
            return CharBuffer.wrap(this.jsonString, start, start + offset);
        }

        private CharBuffer consume() {
            return this.consume(1);
        }

        private CharBuffer consume(int length) {
            this.index += length;
            return CharBuffer.wrap(this.jsonString, this.index - length, this.index);
        }

        private int string() {
            for (int i = this.index; i < jsonString.length(); i++) {
                if (this.read(i, 1).equals(Constants.QUOTE.value)) {
                    return i - this.index;
                }
            }
            return -1;
        }

        private int number() {
            for (int i = this.index; i < jsonString.length(); i++) {
                if (!this.numericChars.contains(this.read(i, 1))) {
                    return i - this.index;
                }
            }
            return -1;
        }

        private int bool() {
            if (this.read(this.index, 4).equals(Constants.TRUE.value)) {
                return 4;
            }

            if (this.read(this.index, 5).equals(Constants.FALSE.value)) {
                return 5;
            }

            return -1;
        }

        private int nil() {
            if (this.read(this.index, 4).equals(Constants.NULL.value)) {
                return 4;
            }

            return -1;
        }
    }

    public enum Constants {
        LEFT_CURL_BRACKET("{"),
        RIGHT_CURL_BRACKET("}"),
        LEFT_SQUARE_BRACKET("["),
        RIGHT_SQUARE_BRACKET("]"),
        QUOTE("\""),
        COMMA(","),
        COLON(":"),
        TRUE("true"),
        FALSE("false"),
        NULL("null"),
        WHITE_SPACE(" "),
        T("t"),
        F("f"),
        N("n");

        public final CharBuffer value;

        Constants(String value) {
            this.value = CharBuffer.wrap(value);
        }
    }
}
