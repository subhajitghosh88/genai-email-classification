package com.subhajit.email.classification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedEmail {

    private String from;
    private String subject;
    private String body;
    private List<File> attachments;
}
