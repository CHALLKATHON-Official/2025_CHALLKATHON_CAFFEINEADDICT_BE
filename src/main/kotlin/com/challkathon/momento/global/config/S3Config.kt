package com.challkathon.momento.global.config

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config {

    @Value("\${cloud.aws.credentials.accessKey}")
    lateinit var accessKey: String

    @Value("\${cloud.aws.credentials.secretKey}")
    lateinit var secretKey: String

    @Value("\${cloud.aws.region.static}")
    lateinit var region: String

    @Value("\${cloud.aws.s3.bucket}")
    lateinit var bucketName: String

    private lateinit var awsCredentials: AWSCredentials

    @PostConstruct
    fun init() {
        awsCredentials = BasicAWSCredentials(accessKey, secretKey)
    }

    @Bean
    fun amazonS3(): AmazonS3 {
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .build()
    }

    @Bean
    fun awsCredentialsProvider(): AWSCredentialsProvider {
        return AWSStaticCredentialsProvider(awsCredentials)
    }
}
