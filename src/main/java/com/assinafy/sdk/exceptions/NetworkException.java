package com.assinafy.sdk.exceptions;

import java.util.Collections;

/** Indicates that an SDK operation could not complete because of a transport failure. */
public class NetworkException extends AssinafyException {

    /**
     * Create a network exception without an underlying cause.
     *
     * @param message error message
     */
    public NetworkException(String message) {
        super(message, Collections.emptyMap());
    }

    /**
     * Create a network exception with the transport failure that caused it.
     *
     * @param message error message
     * @param cause underlying transport failure
     */
    public NetworkException(String message, Throwable cause) {
        super(message, Collections.emptyMap(), cause);
    }
}
