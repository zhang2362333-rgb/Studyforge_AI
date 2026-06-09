package com.studyforge.help.service;

import com.studyforge.help.dto.CreateHelpAnswerRequest;
import com.studyforge.help.dto.CreateHelpRequest;
import com.studyforge.help.vo.HelpAnswerVO;
import com.studyforge.help.vo.HelpRequestVO;
import java.util.List;

public interface HelpRequestService {
    Long create(Long userId, String title, String description);

    Long create(Long userId, CreateHelpRequest request);

    List<HelpRequestVO> list(String status, int limit);

    HelpRequestVO detail(Long helpId);

    HelpAnswerVO answer(Long helpId, Long userId, CreateHelpAnswerRequest request);

    List<HelpAnswerVO> answers(Long helpId, Long viewerId);

    HelpAnswerVO likeAnswer(Long helpId, Long answerId, Long userId);

    void deleteAnswer(Long helpId, Long answerId, Long userId);

    void accept(Long helpId, Long answerId, Long userId);
}
