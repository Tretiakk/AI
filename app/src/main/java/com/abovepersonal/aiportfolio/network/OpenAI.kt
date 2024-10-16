package com.abovepersonal.aiportfolio.network

import android.content.Context
import android.util.Log
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.abovepersonal.aiportfolio.BuildConfig

class ChatGPTapi {

    companion object {
        private const val GPT4o_MINI = "gpt-4o-mini"
        private const val GPT4_TURBO = "gpt-4-turbo"
        private const val GPT3_5_TURBO = "gpt-3.5-turbo"
    }

    @OptIn(BetaOpenAI::class)
    suspend fun makeGPTRequest(text: String): String {
        val openAI = OpenAI(BuildConfig.OPEN_AI_KEY)

        try {
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId(GPT4o_MINI),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.Assistant,
                        content = text
                    )
                )
            )


            val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)

            return completion.choices.first().message?.content ?: "An error occurred"
        } catch (e: Exception) {
            Log.e("ERROR:", e.cause?.message ?: "An error occurred")
        }

        return "An error occurred"
    }
}