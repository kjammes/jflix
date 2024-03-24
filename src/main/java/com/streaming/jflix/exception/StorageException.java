package com.streaming.jflix.exception;

public class StorageException extends  RuntimeException {
    public StorageException(Exception ex) {
        super(ex);
    }
}
