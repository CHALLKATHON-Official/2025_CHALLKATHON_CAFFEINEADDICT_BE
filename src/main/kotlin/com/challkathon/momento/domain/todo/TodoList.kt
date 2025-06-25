package com.challkathon.momento.domain.todo

import com.challkathon.momento.domain.todo.entity.enums.TodoCategory
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "todo_list")
class TodoList(

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: TodoCategory = TodoCategory.ACTIVITY,

    @Column(name = "is_ai_generated", nullable = false)
    var isAIGenerated: Boolean = false,

    @Column(name = "generated_date")
    var generatedDate: LocalDate? = null,

    @Column(name = "usage_count")
    var usageCount: Int = 0
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_list_id", nullable = false)
    val id: Long = 0

    fun incrementUsageCount() {
        this.usageCount++
    }
}
