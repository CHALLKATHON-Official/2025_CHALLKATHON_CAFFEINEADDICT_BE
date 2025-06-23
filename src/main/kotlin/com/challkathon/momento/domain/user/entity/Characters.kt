package com.challkathon.momento.domain.character.entity

import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import jakarta.persistence.*

@Entity
@Table(name = "characters")
class Characters(
    @Column(name = "image_url", nullable = false, length = 1000)
    var imageUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: FamilyRole
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "characters_id", nullable = false)
    val id: Long = 0
}
