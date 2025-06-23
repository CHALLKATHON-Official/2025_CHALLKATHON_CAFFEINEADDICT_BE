package com.challkathon.momento.domain.todo

import jakarta.persistence.*

@Entity
@Table(name = "todo_list")
class TodoList(

    @Column(nullable = false, length = 50)
    var content: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_list_id", nullable = false)
    val id: Long = 0
}
