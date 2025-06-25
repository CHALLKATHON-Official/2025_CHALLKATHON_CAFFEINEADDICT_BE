package com.challkathon.momento.auth.dto

abstract class OAuth2UserInfo(
    protected val attributes: Map<String, Any>
) {
    abstract fun getId(): String
    abstract fun getName(): String
    abstract fun getEmail(): String
    abstract fun getImageUrl(): String?
}

class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {

    override fun getId(): String {
        return attributes["id"].toString()
    }

    override fun getName(): String {
        val profile = attributes["kakao_account"] as? Map<*, *>
        val profileInfo = profile?.get("profile") as? Map<*, *>
        return profileInfo?.get("nickname") as? String ?: "Unknown"
    }

    override fun getEmail(): String {
        val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
        return kakaoAccount?.get("email") as? String ?: ""
    }

    override fun getImageUrl(): String? {
        val profile = attributes["kakao_account"] as? Map<*, *>
        val profileInfo = profile?.get("profile") as? Map<*, *>
        return profileInfo?.get("profile_image_url") as? String
    }
}