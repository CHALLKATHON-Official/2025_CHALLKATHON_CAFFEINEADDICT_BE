package com.challkathon.momento.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import java.lang.reflect.Type

@Component
class MultipartJackson2HttpMessageConverter(
    objectMapper: ObjectMapper
) : AbstractJackson2HttpMessageConverter(objectMapper, MediaType.APPLICATION_OCTET_STREAM) {


    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean = false

    override fun canWrite(mediaType: MediaType?): Boolean = false
}
