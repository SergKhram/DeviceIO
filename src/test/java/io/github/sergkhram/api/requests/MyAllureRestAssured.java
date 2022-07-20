package io.github.sergkhram.api.requests;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.LogDetail;
import io.restassured.internal.print.RequestPrinter;
import io.restassured.internal.print.ResponsePrinter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.UUID;

public class MyAllureRestAssured extends AllureRestAssured {
    private final Logger log;

    public MyAllureRestAssured(Logger logger) {
        this.log = logger;
    }

    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext filterContext) {
        try {
            processRequest(requestSpec);
        } catch (UnsupportedEncodingException e) {
            log.debug("Unsupported encoding of request");
        }
        AllureLifecycle lifecycle = Allure.getLifecycle();
        lifecycle.startStep(UUID.randomUUID().toString(),
            (new StepResult().setStatus(Status.PASSED).setName(String.format("%s: %s", requestSpec.getMethod(), requestSpec.getURI()))));

        Response response;
        try {
            response = super.filter(requestSpec, responseSpec, filterContext);
        } finally {
            lifecycle.stopStep();
        }
        if (response != null) {
            try {
                processResponse(requestSpec.getURI(), response);
            } catch (UnsupportedEncodingException e) {
                log.debug("Unsupported encoding of response");
            }
        }

        return response;
    }

    public byte[] processRequest(FilterableRequestSpecification request) throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out, true, "UTF-8");
        RequestPrinter.print(request, request.getMethod(), request.getURI(), LogDetail.ALL, Collections.emptySet(), stream, true);

        log.info(out.toString("UTF-8"));

        return out.toByteArray();
    }

    public byte[] processResponse(String requestUri, Response response) throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out, true, "UTF-8");
        stream.printf("Response(%s) ", requestUri);
        ResponsePrinter.print(response, response.getBody(), stream, LogDetail.ALL, true, Collections.emptySet());

        log.info(out.toString("UTF-8"));

        return out.toByteArray();
    }
}
