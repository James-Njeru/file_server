package com.example.FileServer.controller;

import com.example.FileServer.model.FileMetadata;
import com.example.FileServer.model.FileSummary;
import com.example.FileServer.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        return ResponseEntity.ok("File uploaded successfully: " + fileName);
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully: " + fileName);
    }

    @GetMapping("/metadata")
    public List<FileMetadata> listAllFiles() {
        return fileStorageService.getAllFilesMetadata();
    }

    @GetMapping("/summary")
    public FileSummary getSummary() {
        return fileStorageService.getSummary();
    }
}