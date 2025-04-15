package com.subhajit.email.classification.controller;

import com.subhajit.email.classification.model.ParsedEmailResponse;
import com.subhajit.email.classification.service.EmailProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("/api/email/classification")
@Tag(name = "Email Classification API", description = "Endpoints for interacting with Gemini AI for email classification")
public class EmailClassificationController {

    private EmailProcessingService emailProcessingService;

    public EmailClassificationController(EmailProcessingService emailProcessingService) {
        this.emailProcessingService = emailProcessingService;
    }

    @PostMapping("/parse-email")
    @Operation(
            summary = "Classify Email",
            description = "Classifies an email based on its content and parses its attachments if available",
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Parsed and classified email",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ParsedEmailResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid file format"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    public ResponseEntity<ParsedEmailResponse> classifyEmail(@RequestParam("file") MultipartFile multipartFile) {
        if (!Objects.requireNonNull(multipartFile.getOriginalFilename()).endsWith(".eml") && !multipartFile.getOriginalFilename().endsWith(".msg")) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(emailProcessingService.processEmail(multipartFile));
    }
}
