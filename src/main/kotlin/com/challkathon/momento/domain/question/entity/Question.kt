package com.challkathon.momento.domain.question.entity

import jakarta.persistence.*

@Entity
@Table(name = "question")
class Question(

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id", nullable = false)
    val id: Long = 0
}
