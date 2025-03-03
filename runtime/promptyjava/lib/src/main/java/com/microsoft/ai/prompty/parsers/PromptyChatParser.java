package com.microsoft.ai.prompty.parsers;

import com.microsoft.ai.prompty.models.Prompty;
import com.microsoft.ai.extensions.AIContent;
import com.microsoft.ai.extensions.ChatMessage;
import com.microsoft.ai.extensions.ChatRole;
import com.microsoft.ai.extensions.ImageContent;
import com.microsoft.ai.extensions.TextContent;
import com.microsoft.ai.prompty.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

enum ContentType {
    Text,
    LocalImage,
    RemoteImage
}

class RawMessage {
    private ChatRole role;
    private String content;
    private List<RawContent> contents;

    public ChatRole getRole() { return role; }
    public void setRole(ChatRole role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<RawContent> getContents() { return contents; }
    public void setContents(List<RawContent> contents) { this.contents = contents; }
}

class RawContent {
    private ContentType contentType;
    private String content;
    private String media;

    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMedia() { return media; }
    public void setMedia(String media) { this.media = media; }
}

@Parser("prompty.chat")
public class PromptyChatParser extends Invoker {
    private static final String[] ROLES = {"assistant", "function", "tool", "system", "user"};
    private static final String MESSAGE_REGEX = "^\\s*#?\\s*(" + String.join("|", ROLES) + ")\\s*:\\s*$";
    private static final String IMAGE_REGEX = "(?<alt>!\\[[^\\]]*\\])\\((?<filename>.*?)(?=\"|\\)\\))";

    private final Prompty prompty;

    public PromptyChatParser(Prompty prompty) {
        super(prompty);
        this.prompty = prompty;
    }

    @Override
    public Object invoke(Object args) throws Exception {
        if (!(args instanceof String)) {
            throw new IllegalArgumentException("Invalid args type for prompty.chat");
        }

        return parse((String) args).stream()
            .map(m -> {
                if (m.getContent() == null && m.getContents() != null) {
                    List<AIContent> contents = m.getContents().stream()
                        .map(c -> {
                            switch (c.getContentType()) {
                                case Text:
                                    return new TextContent(c.getContent());
                                case LocalImage:
                                    byte[] image = getImageContent(c.getContent(), c.getMedia());
                                    return new ImageContent(image, c.getMedia());
                                case RemoteImage:
                                    return new ImageContent(c.getContent(), c.getMedia());
                                default:
                                    throw new IllegalStateException("Invalid content type!");
                            }
                        })
                        .collect(Collectors.toList());

                    return new ChatMessage(m.getRole(), contents);
                } else {
                    return new ChatMessage(m.getRole(), m.getContent());
                }
            })
            .toArray(ChatMessage[]::new);
    }

    @Override
    public CompletableFuture<Object> invokeAsync(Object args) {
        if (!(args instanceof String)) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Invalid args type for prompty.chat"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return invoke(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private ChatRole toChatRole(String role) {
        switch (role) {
            case "assistant": return ChatRole.ASSISTANT;
            case "function":
            case "tool": return ChatRole.TOOL;
            case "system": return ChatRole.SYSTEM;
            case "user": return ChatRole.USER;
            default: throw new IllegalArgumentException("Invalid role!");
        }
    }

    private List<RawMessage> parse(String template) {
        List<RawMessage> messages = new ArrayList<>();
        Pattern pattern = Pattern.compile(MESSAGE_REGEX, Pattern.MULTILINE);
        String[] chunks = pattern.split(template);
        chunks = Arrays.stream(chunks)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);

        // if no starter role, assume system
        if (!chunks[0].trim().toLowerCase().equals("system")) {
            List<String> chunkList = new ArrayList<>(Arrays.asList(chunks));
            chunkList.add(0, "system");
            chunks = chunkList.toArray(new String[0]);
        }

        // if last chunk is role then content is empty
        if (Arrays.asList(ROLES).contains(chunks[chunks.length - 1].trim().toLowerCase())) {
            chunks = Arrays.copyOf(chunks, chunks.length - 1);
        }

        if (chunks.length % 2 != 0) {
            throw new IllegalStateException("Invalid prompt format!");
        }

        Pattern imagePattern = Pattern.compile(IMAGE_REGEX, Pattern.MULTILINE);
        for (int i = 0; i < chunks.length; i += 2) {
            Matcher matcher = imagePattern.matcher(chunks[i + 1]);
            RawMessage message = new RawMessage();
            message.setRole(toChatRole(chunks[i]));
            
            if (matcher.find()) {
                message.setContents(processContent(matcher, chunks[i + 1]));
            } else {
                message.setContent(chunks[i + 1]);
            }
            messages.add(message);
        }

        return messages;
    }

    private List<RawContent> processContent(Matcher matches, String content) {
        List<RawContent> contents = new ArrayList<>();
        Pattern pattern = Pattern.compile(IMAGE_REGEX, Pattern.MULTILINE);
        String[] chunks = pattern.split(content);
        
        int currentMatch = 0;
        for (String chunk : chunks) {
            chunk = chunk.trim();
            if (chunk.isEmpty()) continue;

            if (matches.find()) {
                String alt = matches.group("alt");
                String filename = matches.group("filename");

                if (chunk.equals(alt)) {
                    continue;
                } else if (chunk.equals(filename)) {
                    String img = filename.split(" ")[0].trim();
                    String media = img.substring(img.lastIndexOf('.') + 1).toLowerCase();
                    
                    if (!media.matches("jpg|jpeg|png")) {
                        throw new IllegalArgumentException("Invalid image media type (jpg, jpeg, or png are allowed)");
                    }

                    RawContent rawContent = new RawContent();
                    if (img.startsWith("http://") || img.startsWith("https://")) {
                        rawContent.setContentType(ContentType.RemoteImage);
                    } else {
                        rawContent.setContentType(ContentType.LocalImage);
                    }
                    rawContent.setContent(img);
                    rawContent.setMedia("image/" + media);
                    contents.add(rawContent);
                    currentMatch++;
                }
            } else {
                RawContent textContent = new RawContent();
                textContent.setContentType(ContentType.Text);
                textContent.setContent(chunk);
                contents.add(textContent);
            }
        }
        return contents;
    }

    private byte[] getImageContent(String image, String media) {
        try {
            Path basePath = prompty.getPath() != null ? 
                Paths.get(prompty.getPath()).getParent() : null;
            Path path = basePath != null ? 
                FileUtils.getFullPath(image, basePath.toString()) : 
                Paths.get(image).toAbsolutePath();
            return FileUtils.readAllBytes(path.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image file", e);
        }
    }
}