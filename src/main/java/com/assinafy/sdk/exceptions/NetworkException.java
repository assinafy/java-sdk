package com.assinafy.sdk.exceptions;

import java.util.Collections;

public class NetworkException extends AssinafyException {

    public NetworkException(String message) {
        super(message, Collections.emptyMap());
    }

    public NetworkException(String message, Throwable cause) {
        super(message, Collections.emptyMap(), cause);
    }
}
