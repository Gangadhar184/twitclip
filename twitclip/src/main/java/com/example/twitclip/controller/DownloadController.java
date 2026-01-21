package com.example.twitclip.controller;

import com.example.twitclip.security.SignedUrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

@RestController
@RequestMapping("/download")
public class DownloadController {

    private final SignedUrlService signer;

    public DownloadController(SignedUrlService signer) {
        this.signer = signer;
    }


    @GetMapping("/{filename}")
    public void download(
            @PathVariable String filename,
            @RequestParam long expires,
            @RequestParam String sig,
            HttpServletResponse response
    ) throws IOException {

        if (!signer.isValid(filename, expires, sig)) {
            response.sendError(403, "Invalid or expired link");
            return;
        }

        File file = new File("download", filename);
        if (!file.exists()) {
            response.sendError(404, "File not found");
            return;
        }

        response.setContentType("video/mp4");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\""
        );

        Files.copy(file.toPath(), response.getOutputStream());
        response.flushBuffer();

        // DELETE AFTER DOWNLOAD
        boolean deleted = file.delete();
        if (deleted) {
            System.out.println("deleted after download: " + filename);
        }
    }

}
