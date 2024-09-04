package com.example.FileServer.service;

import com.example.FileServer.config.FileStorageProperties;
import com.example.FileServer.exception.FileStorageException;
import com.example.FileServer.model.FileMetadata;
import com.example.FileServer.model.FileSummary;
import com.example.FileServer.repository.FileMetadataRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"fileMetadata"})
public class FileStorageService {

    private final Path fileStorageLocation;
    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties, FileMetadataRepository fileMetadataRepository) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        this.fileMetadataRepository = fileMetadataRepository;
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Image compression
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new FileStorageException("Failed to read image from input stream.");
            }

            BufferedImage compressedImage = Scalr.resize(originalImage, 800);

            // Save compressed image to storage
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            try (OutputStream outputStream = Files.newOutputStream(targetLocation)) {
                ImageIO.write(compressedImage, "jpg", outputStream);
            }

            // Save metadata to database
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(fileName);
            metadata.setUrl(targetLocation.toString());
            metadata.setFileSize(Files.size(targetLocation));
            metadata.setUploadDate(LocalDateTime.now());

            // Save metadata entity
            fileMetadataRepository.save(metadata);

            // Clear the cache after saving new metadata
            clearCache();

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            // Find the file metadata
            Optional<FileMetadata> metadataOptional = fileMetadataRepository.findByFileName(fileName);

            if (metadataOptional.isPresent()) {
                // Delete the file from storage
                Path filePath = fileStorageLocation.resolve(fileName).normalize();
                Files.deleteIfExists(filePath);

                // Delete the file metadata from the database
                fileMetadataRepository.delete(metadataOptional.get());

                // Clear the cache after deleting metadata
                clearCache();
            } else {
                throw new FileStorageException("File not found: " + fileName);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName + ". Please try again!", ex);
        }
    }

    public byte[] retrieveFile(String fileName) {
        try {
            // Find the file metadata
            Optional<FileMetadata> metadataOptional = fileMetadataRepository.findByFileName(fileName);

            if (metadataOptional.isPresent()) {
                // Read the file from storage
                Path filePath = fileStorageLocation.resolve(fileName).normalize();
                return Files.readAllBytes(filePath);
            } else {
                throw new FileStorageException("File not found: " + fileName);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not retrieve file " + fileName + ". Please try again!", ex);
        }
    }

    @Cacheable("fileMetadataList")
    public List<FileMetadata> getAllFilesMetadata() {
        return fileMetadataRepository.findAll();
    }

    @Cacheable("fileSummary")
    public FileSummary getSummary() {
        List<FileMetadata> files = fileMetadataRepository.findAll();
        long totalFiles = files.size();
        long totalStorage = files.stream().mapToLong(FileMetadata::getFileSize).sum();
        return new FileSummary(totalFiles, totalStorage);
    }

    // Clear the cache
    @CacheEvict(value = {"fileMetadataList", "fileSummary"}, allEntries = true)
    public void clearCache() {
    }
}
