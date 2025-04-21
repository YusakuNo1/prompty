import exp from "constants";
import { PromptTemplate } from "../src/PromptTemplate";
import { Prompty } from "../src/core";
// import { InvokerFactory } from "../src/invokerFactory";
// import * as fs from 'fs/promises';

describe("PromptTemplate", () => {
  it.each`
    prompty
    ${"tests/prompts/basic.prompty"}
  `("should load $prompty", async ({prompty}) => {
    const p = await PromptTemplate.fromPrompty(prompty)
    const messages = await p.createMessages({ firstName: "John", context: "test-context", question: "test-question" });
    expect(messages.length).toEqual(2);
    expect(messages[0]["role"]).toEqual("system");
    expect(messages[0]["content"]).toContain("John");
    expect(messages[1]["role"]).toEqual("user");
    expect(messages[1]["content"]).toEqual("test-question");
  });

  it("should load $prompt string", async () => {
    const prompt_string = `
         system:
        You are an AI assistant in a hotel. You help guests with their requests and provide information about the hotel and its services.

        # context
        {{#rules}}
        {{rule}}
        {{/rules}}

        {{#chat_history}}
        {{role}}:
        {{content}}
        {{/chat_history}}

        user:
        {{input}}`;
    const p = await PromptTemplate.fromString(prompt_string)
    const messages = await p.createMessages({ input: "test-input" });
    expect(messages.length).toEqual(2);
    expect(messages[0]["role"]).toEqual("system");
    expect(messages[0]["content"]).toContain("You are an AI assistant in a hotel. You help guests with their requests and provide information about the hotel and its services.");
    expect(messages[1]["role"]).toEqual("user");
    expect(messages[1]["content"]).toEqual("test-input");
  });
});
