package com.challkathon.momento.domain.message.entity

import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "message")
class Message(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(name = "image_url", length = 1000, nullable = false)
    var imageUrl: String,

    @Column(name = "reserved_at", nullable = false)
    var reservedAt: LocalDateTime
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false)
    val id: Long = 0
}
