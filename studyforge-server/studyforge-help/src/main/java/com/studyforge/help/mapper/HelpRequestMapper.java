package com.studyforge.help.mapper;

import com.studyforge.help.entity.HelpRequest;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface HelpRequestMapper {
    HelpRequest selectById(@Param("helpId") Long helpId);

    List<HelpRequest> selectList(@Param("status") String status, @Param("limit") int limit);

    int insert(HelpRequest helpRequest);

    int updateStatus(@Param("helpId") Long helpId, @Param("status") String status);
}
