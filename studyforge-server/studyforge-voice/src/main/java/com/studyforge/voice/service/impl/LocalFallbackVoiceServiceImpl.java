package com.studyforge.voice.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.voice.service.VoiceService;

public class LocalFallbackVoiceServiceImpl implements VoiceService {
    @Override
    public String textToSpeech(String text, String language) {
        throw new BizException(ErrorCode.INTERNAL_ERROR, "voice service is unavailable; check the voice provider settings in the admin console");
    }

    @Override
    public String speechToText(String audioFilePath, String language) {
        throw new BizException(ErrorCode.INTERNAL_ERROR, "speech recognition is unavailable; check the voice provider settings in the admin console");
    }

    @Override
    public String detectSpeechLanguage(String audioFilePath) {
        throw new BizException(ErrorCode.INTERNAL_ERROR, "speech language detection is unavailable; check the voice provider settings in the admin console");
    }
}
