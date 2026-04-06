package com.irenewhub.backend.controller;

import com.irenewhub.backend.dto.ContactRequest;
import com.irenewhub.backend.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<Map<String, String>> sendMessage(
            @Valid @RequestBody ContactRequest request) {
        contactService.saveMessage(request);
        return ResponseEntity.ok(Map.of(
                "message", "Mensaje recibido correctamente. Te contactaremos pronto."
        ));
    }
}