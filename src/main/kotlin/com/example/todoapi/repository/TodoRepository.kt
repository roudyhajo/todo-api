package com.example.todoapi.repository

import com.example.todoapi.entity.Todo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TodoRepository : JpaRepository<Todo, Int> {
    fun findByColor(color: String): List<Todo>
}