package com.challkathon.momento.domain.family.entity

import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import com.challkathon.momento.domain.todo.mapping.FamilyTodoList
import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "family")
class Family(

    @Column(name = "invite_code", nullable = false, length = 100)
    var inviteCode: String,

    @Column(nullable = false)
    var count: Int = 0
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_id", nullable = false)
    val id: Long = 0

    @OneToMany(mappedBy = "family", cascade = [CascadeType.ALL], orphanRemoval = true)
    val questions: MutableList<FamilyQuestion> = mutableListOf()

    @OneToMany(mappedBy = "family", cascade = [CascadeType.ALL], orphanRemoval = true)
    val todos: MutableList<FamilyTodoList> = mutableListOf()

    @OneToMany(mappedBy = "family", cascade = [CascadeType.ALL])
    val users: MutableList<User> = mutableListOf()

    fun incrementCount() {
        this.count += 1
    }

    fun decrementCount() {
        if (this.count > 0) {
            this.count -= 1
        }
    }
}
