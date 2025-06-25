package com.challkathon.momento.domain.todo.mapping

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.todo.TodoList
import com.challkathon.momento.domain.todo.entity.enums.FamilyTodoStatus
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "family_todo_list")
class FamilyTodoList(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    val todoList: TodoList,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    val family: Family,

    @Column(name = "assigned_at", nullable = false)
    val assignedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "due_date")
    var dueDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FamilyTodoStatus = FamilyTodoStatus.ASSIGNED,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @Column(length = 100)
    var memo: String? = null,

    @Column(name = "image_url", length = 1000)
    var imageUrl: String? = null
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_todo_list_id", nullable = false)
    val id: Long = 0

    fun isOverdue(): Boolean {
        return dueDate?.let { it < LocalDateTime.now() } ?: false
    }

    fun complete() {
        this.status = FamilyTodoStatus.COMPLETED
        this.completedAt = LocalDateTime.now()
    }

    fun updateStatus() {
        status = when {
            isOverdue() -> FamilyTodoStatus.EXPIRED
            completedAt != null -> FamilyTodoStatus.COMPLETED
            else -> FamilyTodoStatus.ASSIGNED
        }
    }
}
