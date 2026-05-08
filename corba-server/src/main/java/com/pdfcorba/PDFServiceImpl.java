package com.pdfcorba;

import PDFServiceModule.PDFServicePOA;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PDFServiceImpl extends PDFServicePOA {

    // 1. Fusion de plusieurs PDFs
    @Override
    public byte[] mergePDFs(byte[][] pdfFiles) {
        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            merger.setDestinationStream(out);
            for (byte[] pdf : pdfFiles) {
                merger.addSource(new ByteArrayInputStream(pdf));
            }
            merger.mergeDocuments(null);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // 2. Découpage : 1 PDF par page
    @Override
    public byte[][] splitPDF(byte[] pdfFile) {
        try {
            PDDocument doc = PDDocument.load(pdfFile);
            Splitter splitter = new Splitter();
            List<PDDocument> pages = splitter.split(doc);
            byte[][] result = new byte[pages.size()][];
            for (int i = 0; i < pages.size(); i++) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                pages.get(i).save(out);
                pages.get(i).close();
                result[i] = out.toByteArray();
            }
            doc.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0][];
        }
    }

    // 3. Extraction de pages spécifiques (ex: "1,3,5")
    @Override
    public byte[] extractPages(byte[] pdfFile, String pageNumbers) {
        try {
            PDDocument source = PDDocument.load(pdfFile);
            PDDocument result = new PDDocument();
            String[] parts = pageNumbers.split(",");
            for (String part : parts) {
                int pageIndex = Integer.parseInt(part.trim()) - 1; // converti en index 0
                result.importPage(source.getPage(pageIndex));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            result.save(out);
            result.close();
            source.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // 4. Suppression d'une page
    @Override
    public byte[] deletePage(byte[] pdfFile, int pageIndex) {
        try {
            PDDocument doc = PDDocument.load(pdfFile);
            doc.removePage(pageIndex);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // 5. Ajout d'un mot de passe
    @Override
    public byte[] addPassword(byte[] pdfFile, String password) {
        try {
            PDDocument doc = PDDocument.load(pdfFile);
            AccessPermission ap = new AccessPermission();
            StandardProtectionPolicy policy =
                new StandardProtectionPolicy(password, password, ap);
            policy.setEncryptionKeyLength(128);
            doc.protect(policy);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // 6. Conversion PDF -> Images PNG
    @Override
    public byte[][] convertToImages(byte[] pdfFile) {
        try {
            PDDocument doc = PDDocument.load(pdfFile);
            PDFRenderer renderer = new PDFRenderer(doc);
            List<byte[]> images = new ArrayList<>();
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 150);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", out);
                images.add(out.toByteArray());
            }
            doc.close();
            return images.toArray(new byte[0][]);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0][];
        }
    }

    // 7. Extraction de texte
    @Override
    public String extractText(byte[] pdfFile) {
        try {
            PDDocument doc = PDDocument.load(pdfFile);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            doc.close();
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur extraction texte : " + e.getMessage();
        }
    }

    // 8. Création d'un PDF à partir d'un texte
    @Override
    public byte[] createPDF(String content) {
        try {
            PDDocument doc = new PDDocument();
            // Découpe le contenu en lignes pour gérer les longs textes
            String[] lines = content.split("\n");
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream stream =
                new PDPageContentStream(doc, page);
            stream.setFont(PDType1Font.HELVETICA, 12);
            stream.beginText();
            stream.newLineAtOffset(50, 750);
            stream.setLeading(16f);
            for (String line : lines) {
                // Sécurité : évite les caractères non supportés
                String safeLine = line.replaceAll("[^\\x20-\\x7E]", "");
                stream.showText(safeLine);
                stream.newLine();
            }
            stream.endText();
            stream.close();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
