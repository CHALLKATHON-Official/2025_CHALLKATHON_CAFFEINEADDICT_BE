package com.challkathon.momento.domain.family.entity

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
    @Column(name = "family_id", nullable = false)
    val id: Long = 0
}
