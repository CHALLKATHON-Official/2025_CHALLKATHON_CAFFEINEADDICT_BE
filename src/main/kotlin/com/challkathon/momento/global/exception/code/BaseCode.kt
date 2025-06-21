package com.challkathon.momento.global.exception.code

import org.springframework.http.HttpStatus

data class BaseCode(
    val httpStatus: HttpStatus,
    val isSuccess: Boolean,
    val code: String,
    val message: String
)