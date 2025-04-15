package com.subhajit.email.classification.configuration;

import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.lang3.StringUtils;

public class TesseractConfiguration {

    private static volatile Tesseract tesseractInstance;

    private TesseractConfiguration() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns a singleton instance of Tesseract.
     *
     * @return Tesseract instance
     */
    public static Tesseract tesseract() {
        if (tesseractInstance == null) {
            synchronized (TesseractConfiguration.class) {
                if (tesseractInstance == null) {
                    tesseractInstance = createTesseract();
                }
            }
        }
        return tesseractInstance;
    }

    /**
     * Creates a new Tesseract instance and sets the data path based on the OS.
     *
     * @return Tesseract instance
     */
    private static Tesseract createTesseract() {
        Tesseract tesseract = new Tesseract();

        // Try to use the environment variable first
        String tessDataPath = System.getenv("TESSDATA_PREFIX");

        if (StringUtils.isNoneEmpty(tessDataPath)) {
            tesseract.setDatapath(tessDataPath);
        } else {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                // Windows Default path
                tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
            } else if (osName.contains("mac")) {
                // Mac Default path
                tesseract.setDatapath("/usr/local/share/tessdata");
            } else if (osName.contains("nix") || osName.contains("nux")) {
                // Linux Default path
                tesseract.setDatapath("/usr/share/tessdata");
            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + osName);
            }
        }
        tesseract.setLanguage("eng");
        return tesseract;
    }

    /**
     * Returns the singleton instance of Tesseract.
     *
     * @return Tesseract instance
     */
    public static Tesseract getTesseractInstance() {
        return tesseract();
    }
}
