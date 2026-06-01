package com.gestione.ristoranti.gestione_ristoranti.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResponse<T> {

    private final int status;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private RestResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> RestResponse<T> ok(T data) {
        return new RestResponse<>(200, "OK", data);
    }

    public static <T> RestResponse<T> ok(String message, T data) {
        return new RestResponse<>(200, message, data);
    }

    public static <T> RestResponse<T> created(T data) {
        return new RestResponse<>(201, "Creato con successo", data);
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
