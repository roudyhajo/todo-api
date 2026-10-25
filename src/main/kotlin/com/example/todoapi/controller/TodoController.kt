package com.example.todoapi.controller

import com.example.todoapi.entity.Todo
import com.example.todoapi.repository.TodoRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

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
class TodoController(private val todoRepository: TodoRepository) {

    /**
     * Retrieve all todos, optionally filtered by completion status.
     *
     * @param completed Filter by status (true for completed, false for pending). If omitted, returns all todos.
     * @return List of matching [Todo] records.
     */
    @GetMapping
    fun getAllTodos(@RequestParam(required = false) completed: Boolean?): List<Todo> {
        return if (completed != null) {
            todoRepository.findAll().filter { it.completed == completed }
        } else {
            todoRepository.findAll()
        }
    }

    /**
     * Retrieve a single todo by its unique identifier.
     *
     * @param id The ID of the todo to retrieve.
     * @return The requested [Todo].
     * @throws ResponseStatusException 404 NOT_FOUND if no todo matches the given ID.
     */
    @GetMapping("/{id}")
    fun getTodoById(@PathVariable id: Long): Todo {
        return todoRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID $id not found") }
    }

    /**
     * Create and persist a new todo item.
     *
     * @param todo The payload containing todo attributes.
     * @return The created [Todo] including its generated ID.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTodo(@RequestBody todo: Todo): Todo {
        if (todo.date == null) {
            todo.date = Instant.now()
        }
        return todoRepository.save(todo)
    }

    /**
     * Fully update an existing todo item by replacing its core attributes.
     *
     * @param id The ID of the target todo.
     * @param todo The payload containing updated values for text, status, and color.
     * @return The updated [Todo].
     * @throws ResponseStatusException 404 NOT_FOUND if the target todo does not exist.
     */
    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: Long,
        @RequestBody todo: Todo
    ): Todo {
        val existingTodo = todoRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID $id not found") }

        existingTodo.text = todo.text
        existingTodo.completed = todo.completed ?: false
        existingTodo.color = todo.color
        if (todo.date != null) {
            existingTodo.date = todo.date
        }

        return todoRepository.save(existingTodo)
    }

    /**
     * Toggle the completion status (completed) of a specific todo.
     *
     * @param id The ID of the target todo.
     * @return The updated [Todo] with its inverted status.
     * @throws ResponseStatusException 404 NOT_FOUND if the target todo does not exist.
     */
    @PatchMapping("/{id}/toggle")
    fun toggleTodoStatus(@PathVariable id: Long): Todo {
        val existingTodo = todoRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID $id not found") }

        existingTodo.completed = !(existingTodo.completed ?: false)
        return todoRepository.save(existingTodo)
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
        if (!todoRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with ID $id not found")
        }
        todoRepository.deleteById(id)
    }

    /**
     * Delete all completed todos from the database in bulk.
     */
    @DeleteMapping("/completed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCompletedTodos() {
        val completed = todoRepository.findAll().filter { it.completed == true }
        todoRepository.deleteAll(completed)
    }
}