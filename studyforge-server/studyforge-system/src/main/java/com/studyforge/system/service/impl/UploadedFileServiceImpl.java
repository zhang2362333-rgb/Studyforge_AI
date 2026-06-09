package com.studyforge.system.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.system.entity.UploadedFile;
import com.studyforge.system.mapper.UploadedFileMapper;
import com.studyforge.system.service.UploadedFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UploadedFileServiceImpl implements UploadedFileService {
    private static final String BIZ_TYPE_POST_IMAGE = "POST_IMAGE";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UploadedFileMapper uploadedFileMapper;

    public UploadedFileServiceImpl(UploadedFileMapper uploadedFileMapper) {
        this.uploadedFileMapper = uploadedFileMapper;
    }

    @Override
    @Transactional
    public UploadedFile recordImage(Long userId,
                                    String originalFilename,
                                    String storedFilename,
                                    String fileUrl,
                                    String contentType,
                                    long fileSize) {
        if (userId == null || isBlank(storedFilename) || isBlank(fileUrl)) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "uploaded file metadata is incomplete");
        }

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setUserId(userId);
        uploadedFile.setBizType(BIZ_TYPE_POST_IMAGE);
        uploadedFile.setOriginalFilename(trimToLength(originalFilename, 255, storedFilename));
        uploadedFile.setStoredFilename(trimToLength(storedFilename, 255, storedFilename));
        uploadedFile.setFileUrl(trimToLength(fileUrl, 512, fileUrl));
        uploadedFile.setContentType(trimToLength(contentType, 100, null));
        uploadedFile.setFileSize(Math.max(0L, fileSize));
        uploadedFile.setStatus(STATUS_ACTIVE);
        uploadedFileMapper.insert(uploadedFile);
        return uploadedFile;
    }

    @Override
    public UploadedFile getByStoredFilename(String storedFilename) {
        if (isBlank(storedFilename)) {
            return null;
        }
        return uploadedFileMapper.selectByStoredFilename(storedFilename.trim());
    }

    private String trimToLength(String value, int maxLength, String fallback) {
        String normalized = isBlank(value) ? fallback : value.trim();
        if (normalized == null) {
            return null;
        }
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
