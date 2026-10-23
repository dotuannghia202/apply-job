package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.InterviewQuestion;
import com.dtn.apply_job.domain.InterviewSession;
import com.dtn.apply_job.domain.response.interview.ResInterviewQuestionDTO;
import com.dtn.apply_job.domain.response.interview.ResInterviewSessionDTO;
import com.dtn.apply_job.util.LanguageUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", imports = {LanguageUtil.class})
public interface InterviewMapper {

    @Mapping(target = "aiOverallFeedback", expression = "java(LanguageUtil.resolve(session.getAiOverallFeedbackVi(), session.getAiOverallFeedbackEn()))")
    @Mapping(target = "questions", source = "questions")
    ResInterviewSessionDTO toResInterviewSessionDTO(InterviewSession session);

    @Mapping(target = "questionText", expression = "java(LanguageUtil.resolve(question.getQuestionTextVi(), question.getQuestionTextEn()))")
    @Mapping(target = "transcribedText", expression = "java(LanguageUtil.resolve(question.getTranscribedTextVi(), question.getTranscribedTextEn()))")
    @Mapping(target = "feedback", expression = "java(LanguageUtil.resolve(question.getFeedbackVi(), question.getFeedbackEn()))")
    ResInterviewQuestionDTO toResInterviewQuestionDTO(InterviewQuestion question);

    List<ResInterviewQuestionDTO> toResInterviewQuestionDTOList(List<InterviewQuestion> questions);
}
