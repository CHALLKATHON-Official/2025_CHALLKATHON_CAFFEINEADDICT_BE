package com.challkathon.momento.domain.todo

import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "todo_list")
class TodoList(

    @Column(nullable = false, length = 50)
    var content: String
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_list_id", nullable = false)
    val id: Long = 0
}
