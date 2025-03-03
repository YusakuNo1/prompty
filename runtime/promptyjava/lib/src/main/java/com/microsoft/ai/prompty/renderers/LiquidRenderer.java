package com.microsoft.ai.prompty.renderers;

import com.microsoft.ai.prompty.models.Prompty;
import com.microsoft.ai.prompty.core.Invoker;
import com.microsoft.ai.prompty.annotations.Renderer;
import com.hubspot.jinjava.Jinjava;
import java.util.concurrent.CompletableFuture;

@Renderer("jinja2")
@Renderer("liquid")
public class LiquidRenderer extends Invoker {
    private final Jinjava jinjava;

    public LiquidRenderer(Prompty prompty) {
        super(prompty);
        this.jinjava = new Jinjava();
    }

    @Override
    public Object invoke(Object args) {
        // TODO - figure out base templating using liquid
        String templateContent = getPrompty().getContent().toString();
        return jinjava.render(templateContent, args);
    }

    @Override
    public CompletableFuture<Object> invokeAsync(Object args) {
        return CompletableFuture.supplyAsync(() -> invoke(args));
    }
}