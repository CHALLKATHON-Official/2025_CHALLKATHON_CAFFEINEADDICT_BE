package com.challkathon.momento.domain.user.entity

import jakarta.persistence.*

@Entity
@Table(name = "question")
class Question(

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
