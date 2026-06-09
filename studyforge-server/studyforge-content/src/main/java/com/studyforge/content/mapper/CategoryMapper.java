package com.studyforge.content.mapper;

import com.studyforge.content.entity.Category;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CategoryMapper {
    Category selectById(@Param("categoryId") Long categoryId);

    List<Category> selectAll();
}
