package com.studyforge.help.vo;

import java.time.LocalDateTime;

public record HelpRequestVO(Long helpId,
                            Long userId,
                            String title,
                            String description,
                            Long categoryId,
                            String status,
                            Integer rewardPoints,
                            LocalDateTime createdTime) {
}
