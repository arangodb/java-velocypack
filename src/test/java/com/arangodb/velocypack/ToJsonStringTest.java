package com.arangodb.velocypack;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ToJsonStringTest {
    private static Context context;
    private static Value jsStringify;
    private static Value jsParse;
    private static final String stringifyFn = "(function stringify(x){return JSON.stringify(x);})";
    private static final String parseFn = "(function parse(x){return JSON.parse(x);})";

    @BeforeClass
    public static void init() {
        context = Context.create();
        jsStringify = context.eval("js", stringifyFn);
        jsParse = context.eval("js", parseFn);
    }

    @AfterClass
    public static void close() {
        context.close();
    }

    @Test
    public void stringToJsonRoundTrip() {
        VPack vpack = new VPack.Builder().build();
        VPackParser parser = new VPackParser.Builder().build();
        generateInput().forEach(value -> {
            String expected = jsStringify.execute(value).as(String.class);
            assertThat(jsParse.execute(expected).as(String.class), is(value));
            assertThat(VPackParser.toJSONString(value), is(expected));
            assertThat(parser.toJson(vpack.serialize(value)), is(expected));
            assertThat(parser.fromJson(expected).getAsString(), is(value));
        });
    }

    private Stream<String> generateInput() {
        return IntStream
                .range(0, Character.MAX_CODE_POINT + 1)
                .filter(codePoint -> {
                    int type = Character.getType(codePoint);
                    return type != Character.PRIVATE_USE &&
                            type != Character.CONTROL &&
                            type != Character.UNASSIGNED;
                })
                .mapToObj(codePoint -> new String(Character.toChars(codePoint)))
                .filter(s -> !Character.isLowSurrogate(s.charAt(0)) &&
                        (!Character.isHighSurrogate(s.charAt(0)) || s.length() != 1));
    }
}
