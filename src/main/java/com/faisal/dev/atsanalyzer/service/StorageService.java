package com.faisal.dev.atsanalyzer.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StoredFileReference storeFile(MultipartFile file);
}
