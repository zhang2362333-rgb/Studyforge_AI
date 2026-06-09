package com.studyforge.system.mapper;

import com.studyforge.system.entity.User;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    User selectById(@Param("userId") Long userId);

    User selectByAccount(@Param("account") String account);

    List<User> selectMentionCandidates(@Param("handles") List<String> handles);

    int insert(User user);

    int updateById(User user);

    int updatePassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);

    int rewardDailyLogin(@Param("userId") Long userId,
                         @Param("rewardDate") LocalDate rewardDate,
                         @Param("experienceDelta") int experienceDelta);
}
