package com.microsoft.ai.prompty;

import java.util.Map;
import java.util.HashMap;

public class Configuration extends Settings {
    private String type = "";

    public Configuration() {
    }

    public Configuration(Map<String, Object> config) {
        this.type = config != null ? DictionaryUtils.getAndRemove(config, "type", String.class, "") : "";
        setItems(config != null ? config : new HashMap<>());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}