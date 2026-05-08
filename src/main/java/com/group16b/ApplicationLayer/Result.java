package com.group16b.ApplicationLayer;

public class Result<T> {
    private final boolean success;
    private final T value;
    private final String error;

    private Result(boolean success, T value, String error) {
        this.success = success;
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> makeOk(T value) {
        return new Result<T>(true, value, null);
    }

    public static <T> Result<T> makeFail(String error) {
        return new Result<T>(false, null, error);
    }
}
