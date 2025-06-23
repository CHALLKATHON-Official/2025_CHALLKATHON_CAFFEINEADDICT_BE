package com.challkathon.momento.domain.s3.entity

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.util.*

@Entity
@Table(name = "uuid_table")
class Uuid(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uuid_id", nullable = false)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val uuid: String
) {
    companion object {
        fun generate(): Uuid {
            return Uuid(uuid = UUID.randomUUID().toString())
        }
    }
}
