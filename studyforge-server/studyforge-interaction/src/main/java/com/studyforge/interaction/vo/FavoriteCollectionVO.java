package com.studyforge.interaction.vo;

import java.time.LocalDateTime;

public record FavoriteCollectionVO(Long collectionId,
                                   Long userId,
                                   String name,
                                   String description,
                                   String visibility,
                                   int itemCount,
                                   LocalDateTime createdTime) {
}
