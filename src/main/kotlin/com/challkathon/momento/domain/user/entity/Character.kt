package com.challkathon.momento.domain.character.entity

import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "characters")
class Character(
    @Column(name = "image_url", nullable = false, length = 1000)
    var imageUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: FamilyRole
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "characters_id", nullable = false)
    val id: Long = 0
}
