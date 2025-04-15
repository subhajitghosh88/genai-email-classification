package com.subhajit.email.classification.util;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static com.subhajit.email.classification.configuration.TesseractConfiguration.getTesseractInstance;

public class AttachmentParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentParser.class);
    private static final Tika TIKA = new Tika();

    private AttachmentParser() {
        // Private constructor to prevent instantiation
    }

    /**
     * Parses the attachments of an email and extracts text from them.
     *
     * @param attachments List of files representing the attachments
     * @return List of strings containing the extracted text from each attachment
     */
    public static List<String> parseAttachments(List<File> attachments) {
        List<String> results = new ArrayList<>();

        for (File attachment : attachments) {
            try {
                String mimeType = TIKA.detect(attachment);
                String content;

                if (isImageMimeType(mimeType)) {
                    content = parseImageWithOCR(attachment);
                } else if (mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        || mimeType.equals("application/vnd.ms-excel")) {
                    content = parseExcel(attachment);
                } else if (mimeType.equals("text/csv") || mimeType.equals("application/csv")) {
                    content = parseCSV(attachment);
                } else if (mimeType.equals("application/pdf")) {
                    content = parsePDF(attachment);
                } else if (mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    content = parseDOCX(attachment);
                } else {
                    content = TIKA.parseToString(attachment);
                }
                results.add(content);
            } catch (Exception ex) {
                LOGGER.error("Error parsing attachments: {}", ex.getMessage());
                results.add("Error extracting attachment from " + attachment.getName() + ": " + ex.getMessage());
            }
        }
        return results;
    }

    /**
     * Checks if the given MIME type is an image type.
     *
     * @param mimeType The MIME type to check
     * @return true if the MIME type is an image type, false otherwise
     */
    private static boolean isImageMimeType(String mimeType) {
        return mimeType.startsWith("image/");
    }

    /**
     * Parses an image file using OCR to extract text.
     *
     * @param imageFile The image file to parse
     * @return The extracted text from the image
     */
    private static String parseImageWithOCR(File imageFile) {
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            return getTesseractInstance().doOCR(bufferedImage);
        } catch (Exception ex) {
            LOGGER.error("Error reading image file: {}", ex.getMessage());
            return "Error reading image file: " + ex.getMessage();
        }
    }

    /**
     * Parses a CSV file and extracts its content.
     *
     * @param csvFile The CSV file to parse
     * @return The extracted content from the CSV file
     */
    private static String parseCSV(File csvFile) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception ex) {
            LOGGER.error("Error reading CSV file: {}", ex.getMessage());
            return "Error reading CSV file: " + ex.getMessage();
        }
        return sb.toString();
    }

    /**
     * Parses an Excel file (both .xls and .xlsx formats) and extracts its content.
     *
     * @param excelFile The Excel file to parse
     * @return The extracted content from the Excel file
     */
    private static String parseExcel(File excelFile) {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            Workbook workbook = excelFile.getName().endsWith("xlsx") ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis);
            for (Sheet sheet : workbook) {
                sb.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        sb.append(getCellValue(cell)).append("\t");
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error reading Excel file: {}", ex.getMessage());
            return "Error reading Excel file: " + ex.getMessage();
        }
        return sb.toString();
    }

    /**
     * Gets the value of a cell based on its type.
     *
     * @param cell The cell to get the value from
     * @return The string representation of the cell value
     */
    private static String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }


    /**
     * Parses a DOCX file and extracts its content.
     *
     * @param file The DOCX file to parse
     * @return The extracted content from the DOCX file
     */
    private static String parseDOCX(File file) {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            XWPFDocument document = new XWPFDocument(fis);
            document.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
        } catch (Exception ex) {
            LOGGER.error("Error reading DOCX file: {}", ex.getMessage());
            return "Error reading DOCX file: " + ex.getMessage();
        }
        return sb.toString();
    }

    /**
     * Parses a PDF file and extracts its content.
     * pdfFile
     *
     * @param pdfFile The PDF file to parse
     * @return The extracted content from the PDF file
     */
    private static String parsePDF(File pdfFile) {
        StringBuilder sb = new StringBuilder();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            sb.append(stripper.getText(document));
        } catch (Exception ex) {
            LOGGER.error("Error reading PDF file: {}", ex.getMessage());
            return "Error reading PDF file: " + ex.getMessage();
        }
        return sb.toString();
    }
}
