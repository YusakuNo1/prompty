package com.microsoft.ai.prompty.models;

import java.util.Map;
import java.util.HashMap;

public class Settings {
    private Map<String, Object> items = new HashMap<>();

    public Settings() {
    }

    public Settings(Map<String, Object> items) {
        this.items = items != null ? items : new HashMap<>();
    }

    public Map<String, Object> getItems() {
        return items;
    }

    public void setItems(Map<String, Object> items) {
        this.items = items;
    }
}