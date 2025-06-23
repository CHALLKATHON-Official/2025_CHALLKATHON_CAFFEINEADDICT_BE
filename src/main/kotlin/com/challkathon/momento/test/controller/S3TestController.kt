package com.challkathon.momento.domain.s3.controller

import com.challkathon.momento.domain.s3.entity.Uuid
import com.challkathon.momento.domain.s3.service.AmazonS3Manager
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "S3 테스트 API", description = "S3 업로드 테스트용 API")
@RestController
@RequestMapping("/api/v1/test")
class S3TestController(
    private val amazonS3Manager: AmazonS3Manager
) {

    @Operation(summary = "S3 업로드", description = "파일을 S3에 업로드합니다.")
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val uuid = Uuid.generate()
        val key = amazonS3Manager.generateMomentoKeyName(uuid)
        val url = amazonS3Manager.uploadFile(key, file)
        return ResponseEntity.ok(url ?: "업로드 실패")
    }
}
