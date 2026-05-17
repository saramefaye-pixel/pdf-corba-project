package com.pdfcorba.controller;

import PDFServiceModule.PDFService;
import com.pdfcorba.service.CORBAClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PDFController {

    @Autowired
    private CORBAClientService corbaService;

    private ResponseEntity<byte[]> pdfResponse(byte[] data, String filename) {
        if (data == null || data.length == 0) {
            return ResponseEntity.internalServerError().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(data.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");
        headers.set("Access-Control-Expose-Headers",
                "Content-Disposition");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private ResponseEntity<byte[]> imageResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(data.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @PostMapping("/merge")
    public ResponseEntity<byte[]> merge(
            @RequestParam("files") MultipartFile[] files) throws Exception {
        byte[][] pdfs = new byte[files.length][];
        for (int i = 0; i < files.length; i++) {
            pdfs[i] = files[i].getBytes();
        }
        PDFService service = corbaService.getPdfService();
        byte[] result = service.mergePDFs(pdfs);
        return pdfResponse(result, "fusion.pdf");
    }

    @PostMapping("/split")
    public ResponseEntity<byte[][]> split(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[][] result = service.splitPDF(file.getBytes());
        return ResponseEntity.ok()
                .header("Access-Control-Expose-Headers", "Content-Type")
                .body(result);
    }

    @PostMapping("/extract-pages")
    public ResponseEntity<byte[]> extractPages(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pages") String pages) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[] result = service.extractPages(file.getBytes(), pages);
        return pdfResponse(result, "extrait.pdf");
    }

    @PostMapping("/delete-page")
    public ResponseEntity<byte[]> deletePage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pageIndex") int pageIndex) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[] result = service.deletePage(file.getBytes(), pageIndex);
        return pdfResponse(result, "modifie.pdf");
    }

    @PostMapping("/add-password")
    public ResponseEntity<byte[]> addPassword(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[] result = service.addPassword(file.getBytes(), password);
        return pdfResponse(result, "protege.pdf");
    }

    @PostMapping("/to-images")
    public ResponseEntity<byte[]> toImages(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[][] result = service.convertToImages(file.getBytes());
        if (result.length > 0) {
            return imageResponse(result[0], "page_1.png");
        }
        return ResponseEntity.internalServerError().build();
    }

    @PostMapping("/extract-text")
    public ResponseEntity<String> extractText(
            @RequestParam("file") MultipartFile file) throws Exception {
        PDFService service = corbaService.getPdfService();
        String result = service.extractText(file.getBytes());
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(result);
    }

    @PostMapping("/create")
    public ResponseEntity<byte[]> createPDF(
            @RequestParam("content") String content) throws Exception {
        PDFService service = corbaService.getPdfService();
        byte[] result = service.createPDF(content);
        return pdfResponse(result, "nouveau.pdf");
    }
}
