package com.studyforge.webapi.voice;

import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.system.service.AuthService;
import com.studyforge.voice.entity.VoiceRecord;
import com.studyforge.voice.mapper.VoiceRecordMapper;
import com.studyforge.voice.service.VoiceService;
import com.studyforge.voice.vo.VoiceResultVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/voice")
public class VoiceController {
    private final VoiceService voiceService;
    private final VoiceRecordMapper voiceRecordMapper;
    private final AuthService authService;

    public VoiceController(VoiceService voiceService, VoiceRecordMapper voiceRecordMapper, AuthService authService) {
        this.voiceService = voiceService;
        this.voiceRecordMapper = voiceRecordMapper;
        this.authService = authService;
    }

    @PostMapping("/tts")
    public ApiResponse<VoiceResultVO> tts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                          @RequestBody TextToSpeechRequest request) {
        Long userId = authService.requireUserId(authorization);
        String audio = voiceService.textToSpeech(request.text(), request.languageCode());
        VoiceRecord record = new VoiceRecord();
        record.setUserId(userId);
        record.setPostId(request.postId());
        record.setVoiceType("TTS");
        record.setAudioUrl(audio.length() > 500 ? audio.substring(0, 500) : audio);
        record.setRecognizedText(request.text());
        voiceRecordMapper.insert(record);
        return ApiResponse.success(new VoiceResultVO(audio, "mp3"));
    }

    public record TextToSpeechRequest(Long postId, String text, String languageCode) {
    }
}
