package com.example.todoapi.controller

import com.example.todoapi.entity.Todo
import com.example.todoapi.repository.TodoRepository
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
// Prefix: /api/v1/
@RequestMapping("/api/v1")
class TodoController(private val todoRepository: TodoRepository) {
    @GetMapping("/todo")
    fun getTodos() : List<Todo> {
        return todoRepository.findAll()
    }

    @PostMapping("/todo")
    fun createTodo(@RequestBody todo: Todo) : Todo {
        todoRepository.save(todo)
        return todoRepository.findById(todo.id!!).get()
    }
}