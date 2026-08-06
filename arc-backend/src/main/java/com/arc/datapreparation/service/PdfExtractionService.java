package com.arc.datapreparation.service;

import com.arc.datapreparation.entity.SourceDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PdfExtractionService {

    public SourceDocument extractAndParsePdf(MultipartFile file) throws IOException {
        String text;
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        }

        log.info("Extracted Raw PDF Text Stream:\n{}", text);

        return SourceDocument.builder()
                .clientName(extractFlexibleValue(text, "Client Name", "Client", "Customer", "Company"))
                .plant(extractFlexibleValue(text, "Plant", "Facility", "Location", "Site"))
                .product(extractFlexibleValue(text, "Product", "Part", "Item", "Model"))
                .vacuumSetpoint(extractFlexibleValue(text, "Vacuum Setpoint", "Vacuum Set Point", "Vacuum Set", "Setpoint"))
                .maximumVacuum(extractFlexibleValue(text, "Maximum Vacuum", "Max Vacuum", "Maximum"))
                .minimumVacuum(extractFlexibleValue(text, "Minimum Vacuum", "Min Vacuum", "Minimum"))
                .warningThreshold(extractFlexibleValue(text, "Warning Threshold", "Warning Level", "Warning"))
                .alarmThreshold(extractFlexibleValue(text, "Alarm Threshold", "Alarm Level", "Alarm"))
                .vacuumHoldTime(extractFlexibleValue(text, "Vacuum Hold Time", "Hold Time", "Vacuum Hold"))
                .motorCurrent(extractFlexibleValue(text, "Motor Current", "Current", "Motor Amps", "Amps"))
                .motorTemperature(extractFlexibleValue(text, "Motor Temperature", "Motor Temp", "Temperature", "Temp"))
                .operatingPressure(extractFlexibleValue(text, "Operating Pressure", "Op Pressure", "Pressure"))
                .cycleTime(extractFlexibleValue(text, "Cycle Time", "Cycle"))
                .batchId(extractBatchId(text))
                .build();
    }

    private String extractFlexibleValue(String text, String... keywords) {
        if (text == null || text.isBlank()) return null;

        for (String keyword : keywords) {
            // Strategy 1: Line starting with Keyword followed by space or delimiter (e.g., "Client Name ABC Automotive" or "Client Name: ABC")
            String prefixRegex = "(?i)^" + Pattern.quote(keyword) + "\\s*[:=\\-\\t]*\\s+(.+)";
            Pattern patternPrefix = Pattern.compile(prefixRegex, Pattern.MULTILINE);
            Matcher matcherPrefix = patternPrefix.matcher(text);
            if (matcherPrefix.find()) {
                String val = matcherPrefix.group(1).trim();
                if (!val.isEmpty()) return val;
            }

            // Strategy 2: Match Key [:\-=\s]+ Value anywhere on the line
            String lineRegex = "(?i)" + Pattern.quote(keyword) + "\\s*[:=\\-\\t]+\\s*([^\\r\\n]+)";
            Pattern pattern = Pattern.compile(lineRegex);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String val = matcher.group(1).trim();
                if (!val.isEmpty()) return val;
            }

            // Strategy 3: Search lines for keyword and extract right-hand part or adjacent line
            String[] lines = text.split("\r?\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.toLowerCase().contains(keyword.toLowerCase())) {
                    int colonIdx = line.indexOf(':');
                    if (colonIdx == -1) colonIdx = line.indexOf('=');
                    if (colonIdx == -1) colonIdx = line.indexOf('-');

                    if (colonIdx != -1 && colonIdx < line.length() - 1) {
                        String val = line.substring(colonIdx + 1).trim();
                        if (!val.isEmpty()) return val;
                    }

                    // If line starts with keyword, strip keyword and take remaining
                    String lowerLine = line.toLowerCase();
                    String lowerKw = keyword.toLowerCase();
                    if (lowerLine.startsWith(lowerKw)) {
                        String val = line.substring(keyword.length()).trim();
                        if (!val.isEmpty()) return val;
                    }

                    // If key is alone on the line, check next line
                    if (i + 1 < lines.length) {
                        String nextLine = lines[i + 1].trim();
                        if (!nextLine.isEmpty() && !containsAnyKey(nextLine, keywords)) {
                            return nextLine;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean containsAnyKey(String line, String... keywords) {
        for (String kw : keywords) {
            if (line.toLowerCase().contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private String extractBatchId(String text) {
        String batch = extractFlexibleValue(text, "Batch ID", "Batch Id", "BatchNo", "Batch No", "Batch");
        return batch != null ? batch : "Batch_1";
    }
}
