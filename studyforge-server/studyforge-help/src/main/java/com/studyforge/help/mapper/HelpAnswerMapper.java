package com.studyforge.help.mapper;

import com.studyforge.help.entity.HelpAnswer;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface HelpAnswerMapper {
    HelpAnswer selectById(@Param("answerId") Long answerId);

    HelpAnswer selectByIdForViewer(@Param("answerId") Long answerId, @Param("viewerId") Long viewerId);

    List<HelpAnswer> selectByHelpId(@Param("helpId") Long helpId, @Param("viewerId") Long viewerId);

    int nextFloorNo(@Param("helpId") Long helpId);

    int insert(HelpAnswer helpAnswer);

    int markDeleted(@Param("answerId") Long answerId);

    int incrementLikeCount(@Param("answerId") Long answerId, @Param("delta") int delta);

    int countLike(@Param("answerId") Long answerId, @Param("userId") Long userId);

    int insertLike(@Param("answerId") Long answerId, @Param("userId") Long userId);

    int deleteLike(@Param("answerId") Long answerId, @Param("userId") Long userId);

    int clearAccepted(@Param("helpId") Long helpId);

    int accept(@Param("answerId") Long answerId);
}
