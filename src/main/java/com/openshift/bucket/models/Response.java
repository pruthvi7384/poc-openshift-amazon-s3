package com.openshift.bucket.models;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    private String errorCode;
    private String errorDescription;
    private String exceptionMessage;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
