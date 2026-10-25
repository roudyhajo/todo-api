package com.example.todoapi.controller

import com.example.todoapi.entity.Todo
import com.example.todoapi.repository.TodoRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.doNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.*
import org.springframework.test.context.bean.override.mockito.MockitoBean
@WebMvcTest(TodoController::class)
class TodoControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var todoRepository: TodoRepository

    private val sampleDate: Instant = Instant.parse("2026-10-25T15:30:00Z")

    private fun buildTodo(
        id: Long = 1L,
        text: String = "Test task",
        completed: Boolean = false,
        color: String = "#3B82F6"
    ) = Todo().apply {
        this.id = id
        this.text = text
        this.completed = completed
        this.color = color
        this.date = sampleDate
    }

    @Nested
    @DisplayName("GET /api/v1/todos")
    inner class GetAllTodosTests {

        @Test
        fun `should return all todos when no filter is provided`() {
            val todos = listOf(
                buildTodo(id = 1L, text = "Task 1", completed = false),
                buildTodo(id = 2L, text = "Task 2", completed = true)
            )
            `when`(todoRepository.findAll()).thenReturn(todos)

            mockMvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Task 1"))
                .andExpect(jsonPath("$[1].text").value("Task 2"))
        }

        @Test
        fun `should filter todos by completed status`() {
            val todos = listOf(
                buildTodo(id = 1L, text = "Pending", completed = false),
                buildTodo(id = 2L, text = "Done", completed = true)
            )
            `when`(todoRepository.findAll()).thenReturn(todos)

            mockMvc.perform(get("/api/v1/todos").param("completed", "true"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Done"))
                .andExpect(jsonPath("$[0].completed").value(true))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/todos/{id}")
    inner class GetTodoByIdTests {

        @Test
        fun `should return todo when id exists`() {
            val todo = buildTodo(id = 1L, text = "Buy milk")
            `when`(todoRepository.findById(1L)).thenReturn(Optional.of(todo))

            mockMvc.perform(get("/api/v1/todos/{id}", 1L))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Buy milk"))
        }

        @Test
        fun `should return 404 when id does not exist`() {
            `when`(todoRepository.findById(999L)).thenReturn(Optional.empty())

            mockMvc.perform(get("/api/v1/todos/{id}", 999L))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/todos")
    inner class CreateTodoTests {

        @Test
        fun `should create and return new todo with 201 Created`() {
            val savedTodo = buildTodo(id = 10L, text = "New task", completed = false)
            `when`(todoRepository.save(any(Todo::class.java))).thenReturn(savedTodo)

            mockMvc.perform(
                post("/api/v1/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(savedTodo))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.text").value("New task"))
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/todos/{id}")
    inner class UpdateTodoTests {

        @Test
        fun `should fully update existing todo`() {
            val existing = buildTodo(id = 1L, text = "Old", completed = false, color = "blue")
            val updatePayload = buildTodo(id = 1L, text = "Updated", completed = true, color = "red")

            `when`(todoRepository.findById(1L)).thenReturn(Optional.of(existing))
            `when`(todoRepository.save(any(Todo::class.java))).thenAnswer { it.arguments[0] }

            mockMvc.perform(
                put("/api/v1/todos/{id}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatePayload))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.text").value("Updated"))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.color").value("red"))
        }

        @Test
        fun `should return 404 when updating non-existent todo`() {
            `when`(todoRepository.findById(999L)).thenReturn(Optional.empty())

            val payload = buildTodo(id = 999L, text = "Does not exist")

            mockMvc.perform(
                put("/api/v1/todos/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload))
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/todos/{id}/toggle")
    inner class ToggleTodoStatusTests {

        @Test
        fun `should invert completion flag from false to true`() {
            val existing = buildTodo(id = 1L, completed = false)
            `when`(todoRepository.findById(1L)).thenReturn(Optional.of(existing))
            `when`(todoRepository.save(any(Todo::class.java))).thenAnswer { it.arguments[0] }

            mockMvc.perform(patch("/api/v1/todos/{id}/toggle", 1L))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.completed").value(true))
        }

        @Test
        fun `should return 404 when toggling non-existent todo`() {
            `when`(todoRepository.findById(999L)).thenReturn(Optional.empty())

            mockMvc.perform(patch("/api/v1/todos/{id}/toggle", 999L))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/todos/{id}")
    inner class DeleteTodoTests {

        @Test
        fun `should return 204 No Content when deleting existing todo`() {
            `when`(todoRepository.existsById(1L)).thenReturn(true)
            doNothing().`when`(todoRepository).deleteById(1L)

            mockMvc.perform(delete("/api/v1/todos/{id}", 1L))
                .andExpect(status().isNoContent)

            verify(todoRepository).deleteById(1L)
        }

        @Test
        fun `should return 404 when deleting non-existent todo`() {
            `when`(todoRepository.existsById(999L)).thenReturn(false)

            mockMvc.perform(delete("/api/v1/todos/{id}", 999L))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/todos/completed")
    inner class DeleteCompletedTodosTests {

        @Test
        fun `should delete only completed items and return 204`() {
            val allTodos = listOf(
                buildTodo(id = 1L, completed = false),
                buildTodo(id = 2L, completed = true),
                buildTodo(id = 3L, completed = true)
            )
            `when`(todoRepository.findAll()).thenReturn(allTodos)

            mockMvc.perform(delete("/api/v1/todos/completed"))
                .andExpect(status().isNoContent)

            verify(todoRepository).deleteAll(org.mockito.ArgumentMatchers.anyList())
        }
    }
}