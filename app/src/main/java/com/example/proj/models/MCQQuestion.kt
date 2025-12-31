package com.example.proj.models

data class MCQQuestion(
    val id: String,
    val question: String,
    val options: List<String>, // A, B, C, D
    val correctAnswer: Int, // Index of correct option (0-3)
    val explanation: String
)