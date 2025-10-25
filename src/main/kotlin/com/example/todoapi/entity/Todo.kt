package com.example.todoapi.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "todo", schema = "todo_db")
open class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Int? = null

    @Column(name = "text", nullable = false)
    open var text: String? = null

    @Column(name = "date", nullable = false)
    open var date: Instant? = null

    @Column(name = "color", nullable = false, length = 10)
    open var color: String? = null
}