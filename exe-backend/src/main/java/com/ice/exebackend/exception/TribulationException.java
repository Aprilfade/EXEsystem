package com.ice.exebackend.exception;

import com.ice.exebackend.entity.BizQuestion;

// 自定义异常：天劫心魔
public class TribulationException extends RuntimeException {
    private BizQuestion question;

    public TribulationException(String message, BizQuestion question) {
        super(message);
        this.question = question;
    }

    public BizQuestion getQuestion() {
        return question;
    }
}