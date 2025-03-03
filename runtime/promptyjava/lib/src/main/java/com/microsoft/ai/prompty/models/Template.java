package com.microsoft.ai.prompty.models;

import java.util.Map;

public class Template {
    private String type = "";
    private String parser = "";

    public Template() {
    }

    Template(Map<String, Object> property) {
        this.type = property != null ? MapUtils.getValue(property, "type", String.class, "liquid") : "liquid";
        this.parser = property != null ? MapUtils.getValue(property, "parser", String.class, "prompty") : "prompty";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
    }
}