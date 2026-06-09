package com.studyforge.ai.mapper;

import com.studyforge.ai.entity.AiLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AiLogMapper {
    int insert(AiLog aiLog);

    List<AiLog> selectByUserAndType(@Param("userId") Long userId,
                                    @Param("aiType") String aiType,
                                    @Param("limit") int limit);
}
