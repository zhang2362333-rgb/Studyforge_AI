package com.studyforge.system.mapper;

import com.studyforge.system.entity.UploadedFile;
import org.apache.ibatis.annotations.Param;

public interface UploadedFileMapper {
    UploadedFile selectByStoredFilename(@Param("storedFilename") String storedFilename);

    int insert(UploadedFile uploadedFile);
}
