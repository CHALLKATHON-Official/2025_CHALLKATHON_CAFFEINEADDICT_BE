package com.challkathon.momento.domain.s3.exception

import com.challkathon.momento.domain.s3.exception.code.S3ErrorStatus
import com.challkathon.momento.global.exception.BaseException

class S3UploadException(
    message: String = "S3 업로드 중 오류가 발생했습니다."
) : BaseException(S3ErrorStatus.S3_UPLOAD_FAILED)

class S3DeleteException(
    message: String = "S3 삭제 중 오류가 발생했습니다."
) : BaseException(S3ErrorStatus.S3_DELETE_FAILED)

class S3FileNotFoundException(
    message: String = "S3에서 파일을 찾을 수 없습니다."
) : BaseException(S3ErrorStatus.S3_FILE_NOT_FOUND)
