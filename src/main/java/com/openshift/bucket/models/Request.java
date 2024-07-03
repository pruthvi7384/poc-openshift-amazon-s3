package com.openshift.bucket.models;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Request {
    private String fileData;

    private String fileName;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
