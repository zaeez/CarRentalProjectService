package ch.unil.softarch.luxurycarrental.rest;

import jakarta.json.bind.annotation.JsonbProperty;

public class ErrorResponse {
    @JsonbProperty("error")
    private String error;

    public ErrorResponse() {}
    public ErrorResponse(String error) { this.error = error; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
