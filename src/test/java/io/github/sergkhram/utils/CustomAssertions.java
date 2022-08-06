package io.github.sergkhram.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.List;

import static io.github.sergkhram.utils.json.JsonTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CustomAssertions {

    public static <T> void assertWithAllure(T expected, T actual, Boolean isProto) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected", checkNull(expected, isProto));
                Allure.addAttachment("actual", checkNull(actual, isProto));
                Assertions.assertEquals(expected, actual);
            }
        );
    }

    public static <T> void assertWithAllure(T expected, T actual) {
        assertWithAllure(expected, actual, false);
    }

    public static void assertTrueWithAllure(Boolean actual) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected", "true");
                Allure.addAttachment("actual", String.valueOf(actual));
                Assertions.assertTrue(actual);
            }
        );
    }

    public static void assertFalseWithAllure(Boolean actual) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected", "false");
                Allure.addAttachment("actual", String.valueOf(actual));
                Assertions.assertFalse(actual);
            }
        );
    }

    public static <T> void assertContainsAllWithAllure(List<T> expected, List<T> actual, Boolean isProto) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected", checkNull(expected, isProto));
                Allure.addAttachment("actual", checkNull(actual, isProto));
                assertThat(actual).containsAll(expected);
            }
        );
    }

    public static <T> void assertContainsAllWithAllure(List<T> expected, List<T> actual) {
        assertContainsAllWithAllure(expected, actual, false);
    }

    public static void assertWithAllure(JsonNode expected, JsonNode actual) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected json", expected.toPrettyString());
                Allure.addAttachment("actual json", actual.toPrettyString());
                compareJson(expected, actual);
            }
        );
    }

    public static void assertWithAllure(JsonNode expected, JsonNode actual, JSONCompareMode compareMode) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected json", expected.toPrettyString());
                Allure.addAttachment("actual json", actual.toPrettyString());
                compareJson(expected, actual, compareMode);
            }
        );
    }

    public static void assertWithAllureWRegex(JsonNode expected, JsonNode actual) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected json", expected.toPrettyString());
                Allure.addAttachment("actual json", actual.toPrettyString());
                compareJsonWRegex(expected, actual);
            }
        );
    }

    public static void assertWithAllureWRegex(JsonNode expected, JsonNode actual, JSONCompareMode mode) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected json", expected.toPrettyString());
                Allure.addAttachment("actual json", actual.toPrettyString());
                compareJsonWRegex(expected, actual, mode);
            }
        );
    }

    public static void assertJsonWithAllure(JsonNode expected, JsonNode actual, int expectedStatusCode, int actualStatusCode) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected", expected.toPrettyString());
                Allure.addAttachment("actual", actual.toPrettyString());
                Allure.addAttachment("expectedStatusCode", String.valueOf(expectedStatusCode));
                Allure.addAttachment("actualStatusCode", String.valueOf(actualStatusCode));
                Assertions.assertAll(
                    () -> Assertions.assertEquals(expectedStatusCode, actualStatusCode, "statusCode not equals")
                );
                compareJson(expected, actual);
            }
        );
    }

    public static void assertWithAllure(boolean isSuccess, String message) {
        Allure.step(message);
        Assertions.assertTrue(isSuccess);
    }

    public static String checkNull(Object o, Boolean isProto) {
        if(o != null) {
            return isProto
                ? o.toString()
                : convertModelToString(o);
        } else {
            return "null";
        }
    }

    public static <T> void assertContainsOnlyWithAllure(T expected, List<T> actual, Boolean isProto) {
        Allure.step(
            "Check expected result",
            () -> {
                Allure.addAttachment("expected", checkNull(expected, isProto));
                Allure.addAttachment("actual", checkNull(actual, isProto));
                SoftAssertions softAssertions = new SoftAssertions();
                for(T item : actual) {
                    softAssertions.assertThat(item).isEqualTo(expected);
                }
                softAssertions.assertAll();
            }
        );
    }

    public static <T> void assertContainsOnlyWithAllure(T expected, List<T> actual) {
        assertContainsOnlyWithAllure(expected, actual, false);
    }

    public static <T> Executable prepareAssertion(T expected, T actual, Boolean isProto) {
        return () -> {
            Allure.addAttachment("expected " + expected.getClass().getName(), checkNull(expected, isProto));
            Allure.addAttachment("actual " + actual.getClass().getName(), checkNull(actual, isProto));
            Assertions.assertEquals(expected, actual);
        };
    }

    public static <T> void assertAllWithAllure(List<Executable> finalAssertions) {
        Allure.step(
            "Check expected result",
            () -> {
                Assertions.assertAll(finalAssertions);
            }
        );
    }
}