package com.challkathon.momento.global.exception

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface

open class BaseException(
    private val errorCode: BaseCodeInterface
) : RuntimeException() {

    fun getErrorCode(): BaseCode {
        return errorCode.getCode()
    }
}