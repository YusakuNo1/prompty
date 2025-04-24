using Microsoft.Extensions.AI;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace Prompty.Core
{
    public partial class PromptTemplate
    {
        private Prompty prompty;

        private PromptTemplate(Prompty prompty) {
            this.prompty = prompty;
        }

        public static PromptTemplate fromPrompty(string path)
        {
            if (path == null)
            {
                throw new ArgumentNullException("Please provide file paht.");
            }

            var prompty = Prompty.Load(path);
            var promptTemplate = new PromptTemplate(prompty);
            if (promptTemplate.prompty != null && promptTemplate.prompty.Template != null)
            {
                // For Azure, default to mustache
                promptTemplate.prompty.Template.Format = "mustache";
            }
            return promptTemplate;
        }

        public static PromptTemplate fromString(string promptyString, string api = "chat")
        {
            promptyString = RemoveLeadingEmptySpace(promptyString);
            var prompty = new Prompty();
            if (prompty.Model != null)
            {
                prompty.Model.Api = api;
            }
            prompty.Content = promptyString;

            var promptTemplate = new PromptTemplate(prompty);
            if (promptTemplate.prompty != null && promptTemplate.prompty.Template != null)
            {
                // For Azure, default to mustache
                promptTemplate.prompty.Template.Format = "mustache";
            }
            return promptTemplate;
        }

        public ChatMessage[] createMessages(Dictionary<string, object> data)
        {
            var prepared = this.prompty.Prepare(data, mergeSample: true);
            if (prepared == null)
            {
                throw new Exception("Please provide a valid prompt template");
            }
            return prepared as ChatMessage[];
        }

        private static string RemoveLeadingEmptySpace(string multilineStr)
        {
            string[] lines = multilineStr.Split(new[] { "\r\n", "\r", "\n" }, StringSplitOptions.None);
            int startIndex = 0;
            while (startIndex < lines.Length && string.IsNullOrWhiteSpace(lines[startIndex]))
            {
                startIndex++;
            }

            // Find the minimum number of leading spaces
            int minSpaces = int.MaxValue;
            for (int i = startIndex; i < lines.Length; i++)
            {
                string line = lines[i];
                if (string.IsNullOrWhiteSpace(line))
                {
                    continue;
                }
                int spaces = line.Length - line.TrimStart().Length;
                spaces += line.TrimStart().Count(c => c == '\t') * 2; // Count tabs as 2 spaces
                minSpaces = Math.Min(minSpaces, spaces);
            }

            // Remove leading spaces and indent to the minimum level
            StringBuilder processedLines = new StringBuilder();
            for (int i = startIndex; i < lines.Length; i++)
            {
                string line = lines[i];
                if (line.Length >= minSpaces)
                {
                    processedLines.AppendLine(line.Substring(minSpaces));
                }
                else
                {
                    processedLines.AppendLine(line); // Handle cases where line is shorter than minSpaces
                }
            }

            // Remove the trailing newline if any
            if (processedLines.Length > 0)
            {
                processedLines.Length--;
            }

            return processedLines.ToString();
        }
    }
}
