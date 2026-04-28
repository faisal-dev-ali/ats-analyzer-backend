package com.faisal.dev.atsanalyzer.service.impl;

import com.faisal.dev.atsanalyzer.config.StorageProperties;
import com.faisal.dev.atsanalyzer.exception.StorageException;
import com.faisal.dev.atsanalyzer.service.StorageService;
import com.faisal.dev.atsanalyzer.service.StoredFileReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties storageProperties;

    @Override
    public StoredFileReference storeFile(MultipartFile file) {

        try {

            Path uploadPath = Path.of(
                    storageProperties.getUploadDir()
            ).toAbsolutePath().normalize();

            Files.createDirectories(uploadPath);

            String extension = FilenameUtils.getExtension(
                    file.getOriginalFilename()
            ).toLowerCase(Locale.ROOT);

            String storedFileName =
                    UUID.randomUUID() + "." + extension;

            Path targetLocation = uploadPath.resolve(
                    storedFileName
            );

            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );

            log.info(
                    "File stored successfully. fileName={}, path={}",
                    storedFileName,
                    targetLocation
            );

            return new StoredFileReference(
                    storedFileName,
                    targetLocation.toString()
            );

        } catch (IOException ex) {

            log.error("Failed to store uploaded file", ex);

            throw new StorageException(
                    "Failed to store uploaded file",
                    ex
            );
        }
    }
}
