package com.challkathon.momento.domain.s3.service

import com.amazonaws.services.s3.AmazonS3
import com.challkathon.momento.domain.s3.entity.Uuid
import com.challkathon.momento.global.infrastructure.repository.UuidRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Component
class AmazonS3Manager(
    private val amazonS3: AmazonS3,
    private val uuidRepository: UuidRepository
) {
    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucketName: String

    @Value("\${cloud.aws.s3.path.momento}")
    private lateinit var momentoPath: String

    private val log: Logger = LoggerFactory.getLogger(AmazonS3Manager::class.java)

    fun uploadFile(keyName: String, file: MultipartFile): String? {
        return try {
            amazonS3.putObject(bucketName, keyName, file.inputStream, null)
            amazonS3.getUrl(bucketName, keyName).toString()
        } catch (e: IOException) {
            log.error("Error at AmazonS3Manager.uploadFile: {}", e.stackTraceToString())
            null
        }
    }

    fun deleteFile(keyName: String): Boolean {
        return try {
            if (amazonS3.doesObjectExist(bucketName, keyName)) {
                amazonS3.deleteObject(bucketName, keyName)
                log.info("Deleted file from S3: $keyName")
                true
            } else {
                log.warn("File not found in S3: $keyName")
                false
            }
        } catch (e: Exception) {
            log.error("Error at AmazonS3Manager.deleteFile: {}", e.stackTraceToString())
            false
        }
    }

    fun generateMomentoKeyName(uuid: Uuid): String {
        return "$momentoPath/${uuid.uuid}"
    }
}
