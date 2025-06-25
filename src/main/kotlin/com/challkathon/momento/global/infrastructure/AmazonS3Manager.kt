package com.challkathon.momento.global.infrastructure

import com.amazonaws.SdkClientException
import com.amazonaws.services.s3.AmazonS3
import com.challkathon.momento.domain.s3.exception.S3DeleteException
import com.challkathon.momento.domain.s3.exception.S3FileNotFoundException
import com.challkathon.momento.domain.s3.exception.S3UploadException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Component
class AmazonS3Manager(
    private val amazonS3: AmazonS3
) {
    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucketName: String

    @Value("\${cloud.aws.s3.path.momento}")
    private lateinit var momentoPath: String

    private val log: Logger = LoggerFactory.getLogger(AmazonS3Manager::class.java)

    fun uploadFile(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: throw S3UploadException("파일 이름이 존재하지 않습니다.")
        val keyName = "$momentoPath/$originalFilename"

        return try {
            amazonS3.putObject(bucketName, keyName, file.inputStream, null)
            amazonS3.getUrl(bucketName, keyName).toString()
        } catch (e: IOException) {
            log.error("S3 업로드 실패: {}", e.stackTraceToString())
            throw S3UploadException("S3 업로드 중 오류가 발생했습니다.")
        }
    }

    fun deleteFile(keyName: String) {
        try {
            if (!amazonS3.doesObjectExist(bucketName, keyName)) {
                log.warn("삭제 대상 파일이 존재하지 않음: $keyName")
                throw S3FileNotFoundException("해당 S3 객체를 찾을 수 없습니다.")
            }

            amazonS3.deleteObject(bucketName, keyName)
            log.info("S3에서 파일 삭제 완료: $keyName")
        } catch (e: SdkClientException) {
            log.error("S3 삭제 실패: {}", e.stackTraceToString())
            throw S3DeleteException("S3 객체 삭제 중 오류가 발생했습니다.")
        }
    }
}
