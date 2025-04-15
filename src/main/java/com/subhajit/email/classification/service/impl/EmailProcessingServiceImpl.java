package com.subhajit.email.classification.service.impl;

import com.subhajit.email.classification.model.KeyValue;
import com.subhajit.email.classification.model.ParsedEmail;
import com.subhajit.email.classification.model.ParsedEmailResponse;
import com.subhajit.email.classification.service.EmailProcessingService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.subhajit.email.classification.util.EmailParser.parseEmail;
import static com.subhajit.email.classification.util.AttachmentParser.parseAttachments;

@Service
public class EmailProcessingServiceImpl implements EmailProcessingService {

    /**
     * Processes the email file, extracts its content, classifies it, and checks for duplicates.
     *
     * @param file the email file to process
     * @return ParsedEmailResponse containing the parsed email details
     */
    @Override
    public ParsedEmailResponse processEmail(MultipartFile file) {

        // Step 1: Parse the email
        ParsedEmail email = parseEmail(file);

        // Step 2: Extract attachments and parse them
        List<String> attachmentTexts = parseAttachments(email.getAttachments());

/*        // Step 3: Classify email type
        EmailClassification classification = EmailClassifier.classify(email.getBody(), attachmentTexts);

        // Step 4: Extract key fields
        List<KeyValue> fields = FieldExtractor.extract(email.getBody(), attachmentTexts);

        // Step 5: Check for duplication
        boolean isDuplicate = DuplicateDetector.isDuplicate(email);

        // Step 6: Build response
        ParsedEmailResponse response = new ParsedEmailResponse();
        response.setFrom(email.getFrom());
        response.setSubject(email.getSubject());
        response.setBody(email.getBody());
        response.setRequestType(classification.getRequestType());
        response.setSubRequestType(classification.getSubRequestType());
        response.setExtractedFields(fields);
        response.setDuplicate(isDuplicate);
        response.setReasoning(classification.getReasoning());*/

        return null;
    }
}
