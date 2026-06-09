package com.studyforge.voice.service;

public interface VoiceService {
    String textToSpeech(String text, String language);

    String speechToText(String audioFilePath, String language);

    String detectSpeechLanguage(String audioFilePath);
}
