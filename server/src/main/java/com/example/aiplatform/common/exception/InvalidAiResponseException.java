package com.example.aiplatform.common.exception;

public class InvalidAiResponseException extends RuntimeException {

    public InvalidAiResponseException(String message) {
        super(message);
    }
}
