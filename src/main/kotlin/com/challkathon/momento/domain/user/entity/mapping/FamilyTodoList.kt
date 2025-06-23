package com.challkathon.momento.domain.user.entity.mapping

import com.challkathon.momento.domain.user.entity.Family
import com.challkathon.momento.domain.user.entity.TodoList
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
    val id: Long = 0
}
