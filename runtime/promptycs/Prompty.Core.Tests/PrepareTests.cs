using Microsoft.Extensions.AI;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Prompty.Core.Tests
{
    class MyObject
    {
        public string question { get; set; } = string.Empty;
    }

    public class PrepareTests
    {
        public PrepareTests()
        {
            InvokerFactory.AutoDiscovery();
            Environment.SetEnvironmentVariable("AZURE_OPENAI_ENDPOINT", "ENDPOINT_VALUE");
        }

        [Theory]
        [InlineData("prompty/basic.prompty")]
        [InlineData("prompty/context.prompty")]
        [InlineData("prompty/functions.prompty")]
        public void Prepare(string path)
        {
            var prompty = Prompty.Load(path);
            var prepared = prompty.Prepare(mergeSample: true);
        }

        [Theory]
        [InlineData("prompty/basic.prompty")]
        [InlineData("prompty/context.prompty")]
        [InlineData("prompty/functions.prompty")]
        public void PrepareWithInput(string path)
        {
            var replacementText = "OTHER_TEXT_OTHER_TEXT";
            var prompty = Prompty.Load(path);
            var prepared = prompty.Prepare(new Dictionary<string, object>
            {
                { "question", replacementText }
            }, true);



            Assert.IsType<ChatMessage[]>(prepared);
            var messages = (ChatMessage[])prepared;

            Assert.Equal(2, messages.Length);
            Assert.Equal(replacementText, messages[1].Text);
        }

        [Theory]
        [InlineData("prompty/basic.prompty")]
        [InlineData("prompty/context.prompty")]
        [InlineData("prompty/functions.prompty")]
        public void PrepareWithObjectInput(string path)
        {
            var replacementText = "OTHER_TEXT_OTHER_TEXT";
            var prompty = Prompty.Load(path);
            var prepared = prompty.Prepare(new { question = replacementText }, true);



            Assert.IsType<ChatMessage[]>(prepared);
            var messages = (ChatMessage[])prepared;

            Assert.Equal(2, messages.Length);
            Assert.Equal(replacementText, messages[1].Text);
        }

        [Theory]
        [InlineData("prompty/basic.prompty")]
        [InlineData("prompty/context.prompty")]
        [InlineData("prompty/functions.prompty")]
        public void PrepareWithStrongObjectInput(string path)
        {

            var replacementText = new MyObject { question = "OTHER_TEXT_OTHER_TEXT" };
            var prompty = Prompty.Load(path);
            var prepared = prompty.Prepare(replacementText, true);



            Assert.IsType<ChatMessage[]>(prepared);
            var messages = (ChatMessage[])prepared;
            Console.WriteLine("******************************************");
            Console.WriteLine(messages[0].Role);
            Console.WriteLine(messages[1].Role);

            Assert.Equal(2, messages.Length);
            Assert.Equal(replacementText.question, messages[1].Text);
        }

        class MyInputs
        {
            public string firstName { get; set; } = string.Empty;
            public string lastName { get; set; } = string.Empty;
            public string input { get; set; } = string.Empty;
            public List<ChatMessage> chat_history { get; set; } = new List<ChatMessage>();
        }
        [Theory]
        [InlineData("prompty/chat.prompty")]
        public void PrepareWithDavidTest(string path)
        {
            var imageUrl = "https://www.citypng.com/public/uploads/preview/hd-starbucks-circle-woman-logo-png-701751694778942nj9szlwtvw.png";
            var chat_history = new List<ChatMessage>
            {
                // new ChatMessage
                // {
                //     Role = ChatRole.User,
                //     Contents = new List<AIContent>
                //     {
                //         new UriContent(imageUrl, "image/png")
                //         // {
                //         //     AdditionalProperties = new AdditionalPropertiesDictionary
                //         //     {
                //         //         { "caption", "This is a logo of Starbucks." }
                //         //     }
                //         // }
                //         // imageUrlContent
                //     }
                // },
                new ChatMessage
                {
                    Role = ChatRole.Assistant,
                    Contents = new List<AIContent>
                    {
                        new TextContent("This is a logo of Starbucks.")
                    }
                }
            };
            // var chat_history = new List<object>
            // {
            //     new 
            //     {
            //         role = "user",
            //         content = new List<object>
            //         {
            //             new { type = "image_url", image_url = new { url = imageUrl } }
            //         }
            //     },
            //     new 
            //     {
            //         role = "assistant",
            //         content = new List<object>
            //         {
            //             new { type = "text", text = "This is a logo of Starbucks." },
            //         }
            //     }
            // };
            var inputs = new MyInputs
            {
                firstName = "Jack",
                lastName = "Brown",
                input = "OTHER_TEXT_OTHER_TEXT",
                chat_history = chat_history,
            };

            // var inputs = new
            // {
            //     firstName = "Jack",
            //     lastName = "Brown",
            //     input = "OTHER_TEXT_OTHER_TEXT",
            //     chat_history = new List<object>
            //     {
            //         new
            //         {
            //             role = "user",
            //             content = new List<object>
            //             {
            //                 new
            //                 {
            //                     type = "image_url",
            //                     image_url = new { url = imageUrl }
            //                 }
            //             }
            //         },
            //         // new
            //         // {
            //         //     role = "assistant",
            //         //     content = new List<object>
            //         //     {
            //         //         new { type = "text", text = "This is a logo of Starbucks." }
            //         //     }
            //         // }
            //     }
            // };
            var prompty = Prompty.Load(path);
            var prepared = prompty.Prepare(inputs, true);



            Assert.IsType<ChatMessage[]>(prepared);
            var messages = (ChatMessage[])prepared;

            // Assert.Equal(3, messages.Length);
            // Assert.Equal(inputs.question, messages[1].Text);
        }


    }
}
