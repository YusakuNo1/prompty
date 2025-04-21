import { Prompty } from "./core";

export class PromptTemplate {
    private prompty?: Prompty;

    constructor(prompty?: Prompty) {
        this.prompty = prompty;
    }

    public static async fromPrompty(filePath: string): Promise<PromptTemplate> {
        if (!filePath) {
            throw new Error("Please provide filePath");
        }

        const prompty = await Prompty.load(filePath);
        prompty.template.type = "mustache"; // For Azure, default to mustache instead of Jinja2
        return new PromptTemplate(prompty);
    }

    public static async fromString(promptTemplateString: string): Promise<PromptTemplate> {
        promptTemplateString = this.removeLeadingEmptySpace(promptTemplateString);
        const prompty = new Prompty(promptTemplateString);
        prompty.template.type = "mustache"; // For Azure, default to mustache instead of Jinja2
        return new PromptTemplate(prompty);
    }

    public async createMessages(data: Record<string, any> = {}, ...args: any[]): Promise<Array<Record<string, any>>> {
        if (this.prompty) {
            const parsed = await Prompty.prepare(this.prompty, data);
            return parsed as any;
        } else {
            throw new Error("Please provide a valid prompt template");
        }
    }

    private static removeLeadingEmptySpace(multilineStr: string): string {
        let lines = multilineStr.split(/\r?\n/);
        let startIndex = 0;
        while (startIndex < lines.length && lines[startIndex].trim() === "") {
            startIndex++;
        }
        lines = lines.slice(startIndex);

        // Find the minimum number of leading spaces
        let minSpaces = Number.MAX_SAFE_INTEGER;
        for (const line of lines) {
            if (line.trim().length === 0) {
                continue;
            }

            const spaces = line.length - line.trimStart().length;
            minSpaces = Math.min(minSpaces, spaces);
        }

        // Remove leading spaces and indent to the minimum level
        const processedLines = lines.slice(startIndex).map(line => line.slice(minSpaces));
        return processedLines.join("\n");
    }
}
