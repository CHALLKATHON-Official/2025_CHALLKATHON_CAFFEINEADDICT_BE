package com.challkathon.momento.domain.family.entity

import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import com.challkathon.momento.domain.todo.mapping.FamilyTodoList
import com.challkathon.momento.domain.user.entity.User
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

    // Family.kt
    @OneToMany(mappedBy = "family", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questions: MutableList<FamilyQuestion> = mutableListOf()

    @OneToMany(mappedBy = "family", cascade = [CascadeType.ALL], orphanRemoval = true)
    val todos: MutableList<FamilyTodoList> = mutableListOf()

    @OneToMany(mappedBy = "family", cascade = [CascadeType.ALL])
    val users: MutableList<User> = mutableListOf()

}
