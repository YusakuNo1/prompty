namespace Prompty.Core
{
    /// <summary>
    /// PromptTemplate class to define the prompt template
    /// </summary>
    class PromptTemplate
    {
        public static Prompty FromPrompty(string filePath)
        {
            Prompty prompty = Prompty.Load(filePath);
            // Azure defaults to mustache
            prompty.Template.Format = "mustache";
            return prompty;
        }

        public static Prompty FromString(string template)
        {
            Prompty prompty = new Prompty();
            prompty.Content = template;
            // Azure defaults to mustache
            prompty.Template.Format = "mustache";
            return prompty;
        }
    }
}
