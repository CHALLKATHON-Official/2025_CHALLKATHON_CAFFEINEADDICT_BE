package com.challkathon.momento.domain.character.entity

import com.challkathon.momento.domain.user.entity.User
import jakarta.persistence.*

@Entity
@Table(name = "user_character")
class UserCharacter(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    val characters: Characters
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_character_id", nullable = false)
    val id: Long = 0
}
