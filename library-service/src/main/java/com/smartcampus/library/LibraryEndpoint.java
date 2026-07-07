package com.smartcampus.library;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

@Endpoint
public class LibraryEndpoint {

    private static final String NAMESPACE_URI = "http://smartcampus.com/library";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "BookLoanRequest")
    @ResponsePayload
    public BookLoanResponse handleLoanRequest(@RequestPayload BookLoanRequest request) {
        // R8: Trigger a deliberate SOAP Fault if an empty or invalid student ID is sent
        if (request.getStudentId() == null || request.getStudentId().isEmpty() || "FAIL".equals(request.getStudentId())) {
            throw new IllegalArgumentException("SOAP System Exception: Empty or invalid student reference registration payload.");
        }

        BookLoanResponse response = new BookLoanResponse();
        response.setLoanId("LN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        response.setStatus("CONFIRMED");
        response.setDueDate("2026-12-31");
        return response;
    }
}