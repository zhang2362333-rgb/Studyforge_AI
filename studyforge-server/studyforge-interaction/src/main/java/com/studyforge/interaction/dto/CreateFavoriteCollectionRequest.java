package com.studyforge.interaction.dto;

public record CreateFavoriteCollectionRequest(String name,
                                              String description,
                                              String visibility) {
}
