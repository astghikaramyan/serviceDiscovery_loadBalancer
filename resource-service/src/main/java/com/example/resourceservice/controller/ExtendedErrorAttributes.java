package com.example.resourceservice.controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExtendedErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(final WebRequest webRequest, final ErrorAttributeOptions options) {
        final Map<String, Object> customErrorAttributes = new HashMap<>();
        final Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        final Integer status = (Integer) errorAttributes.get("status");
        if(status == 415){
            customErrorAttributes.put("errorCode", "400");
            customErrorAttributes.put("errorMessage", "Invalid file format: " + webRequest.getHeader("Content-type") + ". Only MP3 files are allowed");
            return customErrorAttributes;
        }
        return errorAttributes;
    }
}
