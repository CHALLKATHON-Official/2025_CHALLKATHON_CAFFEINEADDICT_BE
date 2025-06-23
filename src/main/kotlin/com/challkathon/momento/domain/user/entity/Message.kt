package com.challkathon.momento.domain.message.entity

import com.challkathon.momento.domain.user.entity.User
import jakarta.persistence.*
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
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0
}
