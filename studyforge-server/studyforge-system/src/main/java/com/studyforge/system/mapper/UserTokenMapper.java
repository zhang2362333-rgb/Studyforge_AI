package com.studyforge.system.mapper;

import com.studyforge.system.entity.UserToken;
import org.apache.ibatis.annotations.Param;

public interface UserTokenMapper {
    UserToken selectByToken(@Param("accessToken") String accessToken);

    int insert(UserToken userToken);

    int updateStatusByToken(@Param("accessToken") String accessToken, @Param("status") String status);
}
