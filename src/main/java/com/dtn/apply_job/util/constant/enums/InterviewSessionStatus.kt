package com.dtn.apply_job.util.constant.enums

enum class InterviewSessionStatus {
    NOT_STARTED,   // Đã gen xong câu hỏi, ứng viên chưa vào phòng
    IN_PROGRESS,   // Ứng viên đang trả lời các câu hỏi
    EVALUATING,    // Đã nộp bài, AI đang phân tích và chấm điểm
    COMPLETED,     // Đã có điểm và đánh giá chi tiết
    EXPIRED        // Quá hạn (nếu không tham gia)
}