package com.euni.backend.controller;

import com.euni.backend.dto.request.AiGenerateRequest;
import com.euni.backend.service.GeminiAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final GeminiAiService geminiAiService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(@RequestBody AiGenerateRequest request) {
        SseEmitter emitter = new SseEmitter(180000L);

        SecurityContext context = SecurityContextHolder.getContext();
        executor.execute(DelegatingSecurityContextRunnable.create(() -> {
            geminiAiService.streamGenerate(request, emitter);
        }, context));

        return emitter;
    }
}

