package com.microsoft.ai.prompty.models;

import java.util.Map;

public class Model extends Settings {
    private String api = "";
    private Configuration configuration = new Configuration();
    private Settings parameters = new Settings();
    private Settings response = new Settings();

    public Model() {
    }

    public Model(Map<String, Object> config) {
        this.api = DictionaryUtils.getAndRemove(config, "api", String.class, "");
        this.configuration = new Configuration(DictionaryUtils.getAndRemoveConfig(config, "configuration"));
        this.parameters = new Settings(DictionaryUtils.getAndRemoveConfig(config, "parameters"));
        this.response = new Settings(DictionaryUtils.getAndRemoveConfig(config, "response"));
        this.setItems(config);
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Settings getParameters() {
        return parameters;
    }

    public void setParameters(Settings parameters) {
        this.parameters = parameters;
    }

    public Settings getResponse() {
        return response;
    }

    public void setResponse(Settings response) {
        this.response = response;
    }
}