package com.pdfcorba.controller;

import PDFServiceModule.PDFService;
import com.pdfcorba.service.CORBAClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*") // Autorise le frontend à appeler l'API
public class PDFController {

    @Autowired
    private CORBAClientService corbaService;

    // Réponse PDF utilitaire
    private ResponseEntity<byte[]> pdfResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_PDF)
            .body(data);
    }

    // 1. Fusion
    @PostMapping("/merge")
    public ResponseEntity<byte[]> merge(
            @RequestParam("files") MultipartFile[] files) throws Exception {
        byte[][] pdfs = new byte[files.length][];
        for (int i = 0; i < files.length; i++) {
            pdfs[i] = files[i].getBytes();
        }
        PDFService service = corbaService.getPdfService();
        byte[] result = service.mergePDFs(pdfs);
        return pdfResponse(result, "merged.pdf");
    }

    // 2. Découpage
    @PostMapping("/split")
    public ResponseEntity<byte[][]> split(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[][] result = service.splitPDF(file.getBytes());
        return ResponseEntity.ok(result);
    }

    // 3. Extraction de pages
    @PostMapping("/extract-pages")
    public ResponseEntity<byte[]> extractPages(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pages") String pages) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[] result = service.extractPages(file.getBytes(), pages);
        return pdfResponse(result, "extracted.pdf");
    }

    // 4. Suppression de page
    @PostMapping("/delete-page")
    public ResponseEntity<byte[]> deletePage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pageIndex") int pageIndex) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[] result = service.deletePage(file.getBytes(), pageIndex);
        return pdfResponse(result, "modified.pdf");
    }

    // 5. Ajout mot de passe
    @PostMapping("/add-password")
    public ResponseEntity<byte[]> addPassword(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[] result = service.addPassword(file.getBytes(), password);
        return pdfResponse(result, "protected.pdf");
    }

    // 6. Conversion PDF -> Images
    @PostMapping("/to-images")
    public ResponseEntity<byte[][]> toImages(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[][] result = service.convertToImages(file.getBytes());
        return ResponseEntity.ok(result);
    }

    // 7. Extraction de texte
    @PostMapping("/extract-text")
    public ResponseEntity<String> extractText(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDFService service = corbaService.getPdfService();
        String result = service.extractText(file.getBytes());
        return ResponseEntity.ok(result);
    }

    // 8. Création PDF
    @PostMapping("/create")
    public ResponseEntity<byte[]> createPDF(
            @RequestParam("content") String content) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[] result = service.createPDF(content);
        return pdfResponse(result, "created.pdf");
    }
}
