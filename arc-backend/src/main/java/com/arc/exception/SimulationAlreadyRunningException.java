package com.arc.exception;

public class SimulationAlreadyRunningException extends RuntimeException {

    public SimulationAlreadyRunningException(String message) {
        super(message);
    }
}
