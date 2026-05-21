package com.pdfcorba.controller;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PDFController {

    private ResponseEntity<byte[]> pdfResponse(byte[] data, String filename) {
        if (data == null || data.length == 0)
            return ResponseEntity.internalServerError().build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(data.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");
        headers.set("Access-Control-Expose-Headers", "Content-Disposition");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @PostMapping("/merge")
    public ResponseEntity<byte[]> merge(
            @RequestParam("files") MultipartFile[] files) throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        merger.setDestinationStream(out);
        for (MultipartFile f : files)
            merger.addSource(new ByteArrayInputStream(f.getBytes()));
        merger.mergeDocuments(null);
        return pdfResponse(out.toByteArray(), "fusion.pdf");
    }

    @PostMapping("/split")
    public ResponseEntity<String> split(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getBytes());
        Splitter splitter = new Splitter();
        List<PDDocument> pages = splitter.split(doc);
        int count = pages.size();
        for (PDDocument p : pages) p.close();
        doc.close();
        return ResponseEntity.ok("{\"pages\":" + count + "}");
    }

    @PostMapping("/extract-pages")
    public ResponseEntity<byte[]> extractPages(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pages") String pages) throws Exception {
        PDDocument source = PDDocument.load(file.getBytes());
        PDDocument result = new PDDocument();
        for (String p : pages.split(",")) {
            int idx = Integer.parseInt(p.trim()) - 1;
            result.importPage(source.getPage(idx));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        result.save(out);
        result.close();
        source.close();
        return pdfResponse(out.toByteArray(), "extrait.pdf");
    }

    @PostMapping("/delete-page")
    public ResponseEntity<byte[]> deletePage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pageIndex") int pageIndex) throws Exception {
        PDDocument doc = PDDocument.load(file.getBytes());
        doc.removePage(pageIndex);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();
        return pdfResponse(out.toByteArray(), "modifie.pdf");
    }

    @PostMapping("/add-password")
    public ResponseEntity<byte[]> addPassword(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws Exception {
        PDDocument doc = PDDocument.load(file.getBytes());
        AccessPermission ap = new AccessPermission();
        StandardProtectionPolicy policy =
                new StandardProtectionPolicy(password, password, ap);
        policy.setEncryptionKeyLength(128);
        doc.protect(policy);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();
        return pdfResponse(out.toByteArray(), "protege.pdf");
    }

    @PostMapping("/to-images")
    public ResponseEntity<byte[]> toImages(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getBytes());
        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage image = renderer.renderImageWithDPI(0, 150);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        doc.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(out.size());
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"page_1.png\"");
        return ResponseEntity.ok().headers(headers).body(out.toByteArray());
    }

    @PostMapping("/extract-text")
    public ResponseEntity<String> extractText(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDDocument doc = PDDocument.load(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);
        doc.close();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(text);
    }

    @PostMapping("/create")
    public ResponseEntity<byte[]> createPDF(
            @RequestParam("content") String content) throws Exception {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(doc, page);
        stream.setFont(PDType1Font.HELVETICA, 12);
        stream.beginText();
        stream.newLineAtOffset(50, 750);
        stream.setLeading(16f);
        for (String line : content.split("\n")) {
            String safe = line.replaceAll("[^\\x20-\\x7E]", "");
            stream.showText(safe);
            stream.newLine();
        }
        stream.endText();
        stream.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        doc.close();
        return pdfResponse(out.toByteArray(), "nouveau.pdf");
    }
}
