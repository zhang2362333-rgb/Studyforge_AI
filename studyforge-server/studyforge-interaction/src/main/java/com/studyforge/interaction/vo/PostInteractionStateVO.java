package com.studyforge.interaction.vo;

public record PostInteractionStateVO(boolean liked,
                                     boolean favorited,
                                     int likeCount,
                                     int favoriteCount,
                                     int commentCount,
                                     int viewCount) {
}
