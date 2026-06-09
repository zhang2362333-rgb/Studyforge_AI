package com.studyforge.voice.dto;

public record TextToSpeechRequest(Long postId, String text, String languageCode) {
}
