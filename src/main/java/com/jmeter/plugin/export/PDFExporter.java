package com.jmeter.plugin.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PDFExporter {
    private static final Logger log = LoggerFactory.getLogger(PDFExporter.class);

    public void exportHTMLToPDF(String htmlFilePath, String pdfOutputPath) throws IOException {
        String htmlContent = Files.readString(Paths.get(htmlFilePath));
        
        // Use JSoup to parse and clean the HTML to handle malformed AI-generated content
        try {
            Document doc = Jsoup.parse(htmlContent, "", Parser.htmlParser());
            doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
            
            htmlContent = doc.html();
        } catch (Exception e) {
            log.warn("JSoup parsing failed, falling back to simple sanitization: {}", e.getMessage());
        }
        
        // Sanitize HTML for XML parser - escape unescaped ampersands
        String sanitizedHtml = sanitizeHtmlForXml(htmlContent);
        
        String printFriendlyHtml = sanitizedHtml.replaceAll(
            "<style>",
            "<style>@page { size: A4; margin: 1cm; } "
        );

        try (OutputStream os = new FileOutputStream(pdfOutputPath)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(printFriendlyHtml, "file://" + htmlFilePath);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            log.error("Error exporting PDF: {}", e.getMessage(), e);
            throw new IOException("Failed to export PDF", e);
        }
    }
    
    private String sanitizeHtmlForXml(String html) {
        // Replace unescaped & with &amp; but preserve already escaped entities
        String sanitized = html.replaceAll("&(?!(amp|lt|gt|quot|apos|#\\d+|#x[0-9a-fA-F]+);)", "&amp;");
        
        // Convert HTML5 void elements to XHTML self-closing tags
        sanitized = sanitized.replaceAll("<meta([^>]*?)(?<!/)>", "<meta$1 />");
        sanitized = sanitized.replaceAll("<link([^>]*?)(?<!/)>", "<link$1 />");
        sanitized = sanitized.replaceAll("<img([^>]*?)(?<!/)>", "<img$1 />");
        sanitized = sanitized.replaceAll("<br([^>]*?)(?<!/)>", "<br$1 />");
        sanitized = sanitized.replaceAll("<hr([^>]*?)(?<!/)>", "<hr$1 />");
        sanitized = sanitized.replaceAll("<input([^>]*?)(?<!/)>", "<input$1 />");
        sanitized = sanitized.replaceAll("<area([^>]*?)(?<!/)>", "<area$1 />");
        sanitized = sanitized.replaceAll("<base([^>]*?)(?<!/)>", "<base$1 />");
        sanitized = sanitized.replaceAll("<col([^>]*?)(?<!/)>", "<col$1 />");
        sanitized = sanitized.replaceAll("<embed([^>]*?)(?<!/)>", "<embed$1 />");
        sanitized = sanitized.replaceAll("<param([^>]*?)(?<!/)>", "<param$1 />");
        sanitized = sanitized.replaceAll("<source([^>]*?)(?<!/)>", "<source$1 />");
        sanitized = sanitized.replaceAll("<track([^>]*?)(?<!/)>", "<track$1 />");
        sanitized = sanitized.replaceAll("<wbr([^>]*?)(?<!/)>", "<wbr$1 />");
        
        // Normalize already self-closed tags to have space before />
        sanitized = sanitized.replaceAll("([^\\s])/>", "$1 />");
        
        return sanitized;
    }
}
