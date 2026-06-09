package com.studyforge.system.service;

import com.studyforge.system.entity.UploadedFile;

public interface UploadedFileService {
    UploadedFile recordImage(Long userId,
                             String originalFilename,
                             String storedFilename,
                             String fileUrl,
                             String contentType,
                             long fileSize);

    UploadedFile getByStoredFilename(String storedFilename);
}
