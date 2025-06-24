package com.challkathon.momento.domain.s3.controller

import com.challkathon.momento.domain.s3.service.AmazonS3Manager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/test")
class S3TestController(
    private val amazonS3Manager: AmazonS3Manager
) {
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val url = amazonS3Manager.uploadFile(file)
        return ResponseEntity.ok(url ?: "업로드 실패")
    }
}

