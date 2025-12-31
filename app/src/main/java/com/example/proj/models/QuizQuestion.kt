package com.example.proj.models

data class QuizQuestion(
    val id: String,
    val question: String,
    val answer: String,
    val points: Int = 10
)