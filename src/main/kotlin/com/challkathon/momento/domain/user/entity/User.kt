package com.challkathon.momento.domain.user.entity

import com.challkathon.momento.domain.character.entity.UserCharacter
import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.message.entity.Message
import com.challkathon.momento.domain.question.entity.Answer
import com.challkathon.momento.domain.story.Story
import com.challkathon.momento.domain.user.entity.enums.AuthProvider
import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import com.challkathon.momento.domain.user.entity.enums.Role
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "`user`",
    indexes = [
        Index(name = "idx_user_email", columnList = "email"),
        Index(name = "idx_user_email_provider", columnList = "email, auth_provider")
    ],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["email", "auth_provider"])
    ]
)
class User(
    @Column(nullable = false, length = 100)
    var email: String,

    @Column(nullable = false, length = 50)
    var username: String,

    @Column(nullable = true)
    var password: String? = null,

    @Column(name = "provider_id", length = 100)
    var providerId: String? = null,

    @Column(name = "profile_image_url", length = 500)
    var profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    var authProvider: AuthProvider = AuthProvider.LOCAL,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role = Role.USER,

    @Enumerated(EnumType.STRING)
    @Column(name = "family_role", nullable = true, length = 20)
    var familyRole: FamilyRole? = null,

    @Column(name = "family_role_selected", nullable = false)
    var familyRoleSelected: Boolean = false,

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,

    @Column(name = "email_verified")
    var emailVerified: Boolean = false,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "refresh_token_version", nullable = false)
    var refreshTokenVersion: Long = 0,

    @Column(name = "refresh_token", length = 1000)
    var refreshToken: String? = null,
    
    @Column(name = "last_active_at")
    var lastActiveAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    var family: Family? = null,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    val id: Long = 0

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val messages: MutableList<Message> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userCharacters: MutableList<UserCharacter> = mutableListOf()

    // User.kt
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val answers: MutableList<Answer> = mutableListOf()

    // User.kt
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val stories: MutableList<Story> = mutableListOf()


    fun assignFamily(family: Family) {
        this.family = family
    }

}
