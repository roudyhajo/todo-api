package com.example.todoapi.repository;

import com.example.todoapi.entity.TodoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TodoEntityRepository : JpaRepository<TodoEntity, Long> {
}