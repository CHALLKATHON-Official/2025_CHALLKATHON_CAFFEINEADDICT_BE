package com.challkathon.momento.domain.todo.mapping

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.todo.TodoList
import jakarta.persistence.*

@Entity
@Table(name = "family_todo_list")
class FamilyTodoList(

    @Column(length = 100)
    var memo: String? = null,

    @Column(name = "image_url", nullable = false, length = 1000)
    var imageUrl: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    val todoList: TodoList,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    val family: Family
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_todo_list_id", nullable = false)
    val id: Long = 0
}
