package com.studyforge.help.dto;

public record CreateHelpRequest(String title, String description, Long categoryId, Integer rewardPoints) {
}
