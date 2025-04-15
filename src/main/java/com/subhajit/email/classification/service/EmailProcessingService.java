package com.subhajit.email.classification.service;

import com.subhajit.email.classification.model.ParsedEmailResponse;
import org.springframework.web.multipart.MultipartFile;

public interface EmailProcessingService {
    ParsedEmailResponse processEmail(MultipartFile file);
}
