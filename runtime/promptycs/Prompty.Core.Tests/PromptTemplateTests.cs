namespace Prompty.Core.Tests
{

    public class PromptTemplateTests
    {
        public PromptTemplateTests()
        {
            InvokerFactory.AutoDiscovery();
            Environment.SetEnvironmentVariable("AZURE_OPENAI_ENDPOINT", "ENDPOINT_VALUE");
        }

        [Theory]
        [InlineData("prompty/basic.prompty", "Jane Doe")]
        [InlineData("prompty/basic_mustache.prompty", "Jane Doe")]
        [InlineData("prompty/context.prompty", "Sally Davis")]
        public void TestFromPrompty(string path, string expected)
        {
            var promptTemplate = PromptTemplate.fromPrompty(path);
            var replacementText = "OTHER_TEXT_OTHER_TEXT";
            var messages = promptTemplate.createMessages(new Dictionary<string, object>
            {
                { "question", replacementText }
            });
            Assert.NotNull(promptTemplate);
            Assert.True(messages.Length == 2);
            Assert.Contains(expected, messages[0].Text);
            Assert.Contains(replacementText, messages[1].Text);
        }

        public void TestFromString(string path, string expected)
        {
            Assert.True(true);
        }
    }
}

