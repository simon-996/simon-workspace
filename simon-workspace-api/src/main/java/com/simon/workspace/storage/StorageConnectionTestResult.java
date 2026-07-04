package com.simon.workspace.storage;

public record StorageConnectionTestResult(
        boolean success,
        String message
) {

    public static StorageConnectionTestResult ok(String message) {
        return new StorageConnectionTestResult(true, message);
    }

    public static StorageConnectionTestResult fail(String message) {
        return new StorageConnectionTestResult(false, message);
    }
}
