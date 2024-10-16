package com.abovepersonal.aiportfolio

data class MessageInfo(
    val text: String,
    val from: String,
    val fromMe: Boolean,
    var isPresented: Boolean = false
)