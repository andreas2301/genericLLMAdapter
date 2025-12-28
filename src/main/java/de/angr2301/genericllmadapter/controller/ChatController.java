package de.angr2301.genericllmadapter.controller;


import de.angr2301.genericllmadapter.dto.ChatReply;
import de.angr2301.genericllmadapter.dto.ChatRequest;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatModel chatModel;

    @Autowired
    public ChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @PostMapping("/chat")
    public ChatReply chat(@RequestBody ChatRequest request) {
        UserMessage userMessage = new UserMessage(request.prompt());
        Prompt prompt = new Prompt(userMessage);

        ChatResponse response = chatModel.call(prompt);
        AssistantMessage assistantMessage = (AssistantMessage) response.getResult().getOutput();

        String content = assistantMessage.getContent();

        return new ChatReply(request.prompt(), content);
    }
}
