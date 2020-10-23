package com.bdv.exceptions;

public class InvalidInstance extends Exception {
    public InvalidInstance(String extend) {
        super("[BDV] - Received script instance is not a valid instance. Please extend " + extend + ".");
    }
}
