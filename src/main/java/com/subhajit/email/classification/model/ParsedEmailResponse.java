package com.subhajit.email.classification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedEmailResponse {
    private String from;
    private String subject;
    private String body;
    private String requestType;
    private String subRequestType;
    private List<KeyValue> extractedFields;
    private boolean isDuplicate;
    private String reasoning;
}
