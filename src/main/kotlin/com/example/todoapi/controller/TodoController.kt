package com.example.todoapi.controller

import com.example.todoapi.entity.TodoEntity
import com.example.todoapi.repository.TodoEntityRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * REST controller for managing Todo items.
 * Provides endpoints for CRUD operations and bulk actions.
 */
@CrossOrigin(
    originPatterns = ["http://localhost:*", "http://127.0.0.1:*"],
    allowedHeaders = ["*"],
    methods = [
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.PATCH,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS
    ]
)
@RestController
@RequestMapping("/api/v1/todos")
class TodoController(private val todoEntityRepository: TodoEntityRepository) {

    /**
     * Retrieve all todos, optionally filtered by completion status.
     *
     * @param completed Filter by status (true for completed, false for pending). If omitted, returns all todos.
     * @return List of matching [TodoEntity] records.
     */
    @GetMapping
    fun getAllTodos(@RequestParam(required = false) completed: Boolean?): List<TodoEntity> {
        return if (completed != null) {
            todoEntityRepository.findAll().filter { it.isCompleted == completed }
        } else {
            todoEntityRepository.findAll()
        }
    }

    /**
     * Retrieve a single todo by its unique identifier.
     *
     * @param id The ID of the todo to retrieve.
     * @return The requested [TodoEntity].
     * @throws ResponseStatusException 404 NOT_FOUND if no todo matches the given ID.
     */
    @GetMapping("/{id}")
    fun getTodoById(@PathVariable id: Long): TodoEntity {
        return todoEntityRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID $id not found") }
    }

    /**
     * Create and persist a new todo item.
     *
     * @param todo The payload containing todo attributes.
     * @return The created [TodoEntity] including its generated ID.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTodo(@RequestBody todo: TodoEntity): TodoEntity {
        return todoEntityRepository.save(todo)
    }

    /**
     * Fully update an existing todo item by replacing its core attributes.
     *
     * @param id The ID of the target todo.
     * @param todo The payload containing updated values for text, status, and color.
     * @return The updated [TodoEntity].
     * @throws ResponseStatusException 404 NOT_FOUND if the target todo does not exist.
     */
    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: Long,
        @RequestBody todo: TodoEntity
    ): TodoEntity {
        val existingTodo = todoEntityRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID $id not found") }

        existingTodo.text = todo.text
        existingTodo.isCompleted = todo.isCompleted
        existingTodo.color = todo.color

        return todoEntityRepository.save(existingTodo)
    }

    /**
     * Toggle the completion status (isCompleted) of a specific todo.
     *
     * @param id The ID of the target todo.
     * @return The updated [TodoEntity] with its inverted status.
     * @throws ResponseStatusException 404 NOT_FOUND if the target todo does not exist.
     */
    @PatchMapping("/{id}/toggle")
    fun toggleTodoStatus(@PathVariable id: Long): TodoEntity {
        val existingTodo = todoEntityRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID $id not found") }

        existingTodo.isCompleted = !existingTodo.isCompleted
        return todoEntityRepository.save(existingTodo)
    }

    /**
     * Delete a single todo by its unique identifier.
     *
     * @param id The ID of the todo to delete.
     * @throws ResponseStatusException 404 NOT_FOUND if the target todo does not exist.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTodo(@PathVariable id: Long) {
        if (!todoEntityRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID $id not found")
        }
        todoEntityRepository.deleteById(id)
    }

    /**
     * Delete all completed todos from the database in bulk.
     */
    @DeleteMapping("/completed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCompletedTodos() {
        val completed = todoEntityRepository.findAll().filter { it.isCompleted }
        todoEntityRepository.deleteAll(completed)
    }
}