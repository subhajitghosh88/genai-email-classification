package com.subhajit.email.classification.util;

import com.subhajit.email.classification.model.ParsedEmail;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static jakarta.mail.Part.ATTACHMENT;

public class EmailParser {

    private EmailParser() {
        // Private constructor to prevent instantiation
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailParser.class);

    public static ParsedEmail parseEmail(MultipartFile multipartFile) {
        try {
            String fileName = multipartFile.getOriginalFilename();
            if (StringUtils.isNoneEmpty(fileName) && fileName.endsWith(".eml")) {
                return parseEml(multipartFile);
            } else if (StringUtils.isNoneEmpty(fileName) && fileName.endsWith(".msg")) {
                return parseMsg(multipartFile);
            } else {
                LOGGER.error("Unsupported file type: {}", fileName);
                throw new IllegalArgumentException("Unsupported file type: " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing email", ex);
        }
    }

    private static ParsedEmail parseEml(MultipartFile multipartFile) throws Exception {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session, multipartFile.getInputStream());

        String from = message.getFrom()[0].toString();
        String subject = message.getSubject();
        String body = extractTextFromMime(message);
        List<File> attachments = extractAttachmentsFromMime(message);
        return new ParsedEmail(from, subject, body, attachments);
    }


    private static String extractTextFromMime(Part part) throws Exception {
        if (part.isMimeType("text/*")) {
            return part.getContent().toString();
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                result.append(extractTextFromMime(multipart.getBodyPart(i)));
            }
            return result.toString();
        } else {
            return "";
        }
    }

    private static List<File> extractAttachmentsFromMime(Part part) throws Exception {
        List<File> attachments = new ArrayList<>();
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) || bodyPart.getFileName() != null) {
                    File file = File.createTempFile("att-", bodyPart.getFileName());
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        InputStream inputStream = bodyPart.getInputStream();
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    attachments.add(file);
                }
            }
        }
        return attachments;
    }

    private static ParsedEmail parseMsg(MultipartFile multipartFile) throws Exception {
        File file = File.createTempFile("email", ".msg");
        multipartFile.transferTo(file);

        MAPIMessage mapiMessage = new MAPIMessage(file.getAbsolutePath());
        String from = mapiMessage.getDisplayFrom();
        String subject = mapiMessage.getSubject();
        String body = mapiMessage.getTextBody();

        List<File> attachments = new ArrayList<>();
        for (AttachmentChunks attachment : mapiMessage.getAttachmentFiles()) {
            File attachmentFile = File.createTempFile("att-", attachment.getAttachFileName().getValue());
            try (FileOutputStream fos = new FileOutputStream(attachmentFile)) {
                fos.write(attachment.getAttachData().getValue());
            }
            attachments.add(attachmentFile);
        }
        return new ParsedEmail(from, subject, body, attachments);
    }
}
