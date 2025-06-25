package com.challkathon.momento.domain.s3.controller

import com.challkathon.momento.domain.s3.service.AmazonS3Manager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/test/s3")
class S3TestController(
    private val amazonS3Manager: AmazonS3Manager
) {

    @PostMapping("/upload")
    fun uploadFile(@RequestParam file: MultipartFile): ResponseEntity<String> {
        val url = amazonS3Manager.uploadFile(file)
        return ResponseEntity.ok(url ?: "업로드 실패")
    }

    @DeleteMapping("/delete")
    fun deleteFile(@RequestParam keyName: String): ResponseEntity<String> {
        amazonS3Manager.deleteFile(keyName)
        return ResponseEntity.ok("삭제 요청 완료")
    }
}


