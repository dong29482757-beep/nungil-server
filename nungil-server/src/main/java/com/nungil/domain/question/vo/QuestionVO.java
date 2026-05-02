package com.nungil.domain.question.vo;

import java.time.LocalDateTime;

public class QuestionVO {

    private Long questionId;
    private String id;       // 보호자 id
    private int idx;         // 사용자 순번
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime successAt;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getIdx() { return idx; }
    public void setIdx(int idx) { this.idx = idx; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSuccessAt() { return successAt; }
    public void setSuccessAt(LocalDateTime successAt) { this.successAt = successAt; }
}
