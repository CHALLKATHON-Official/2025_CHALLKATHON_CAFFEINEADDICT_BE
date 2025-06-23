package com.challkathon.momento.domain.user.entity

import jakarta.persistence.*

@Entity
@Table(name = "family")
class Family(

    @Column(name = "invite_code", nullable = false, length = 100)
    var inviteCode: String,

    @Column(nullable = false)
    var count: Int = 0
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
