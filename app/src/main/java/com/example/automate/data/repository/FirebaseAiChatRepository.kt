package com.example.automate.data.repository

import android.util.Log
import com.example.automate.domain.repository.AiChatRepository
import com.example.automate.ui.model.ChatMessageUiModel
import com.example.automate.ui.model.ChatSender
import com.example.automate.ui.viewmodel.VehicleUiModel
import com.example.automate.util.AiConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import java.io.IOException
import java.net.UnknownHostException

class FirebaseAiChatRepository : AiChatRepository {

    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())
    private val TAG = "FirebaseAiChat"
    
    override suspend fun sendMessage(
        vehicle: VehicleUiModel,
        conversationHistory: List<ChatMessageUiModel>,
        userMessage: String
    ): Result<String> {
        try {
            val systemInstruction = content {
                text("""
                    You are an AI assistant inside a vehicle-management application.
                    
                    Selected vehicle:
                    Manufacturer: ${vehicle.manufacturer}
                    Model: ${vehicle.model}
                    Year: ${vehicle.year}
                    
                    Your role:
                    - Answer questions related to the selected vehicle
                    - Explain dashboard warning lights
                    - Explain maintenance and common vehicle problems
                    - Use clear and simple language
                    - Ask a short follow-up question when essential information is missing
                    - Distinguish between general guidance and confirmed diagnosis
                    - Never claim that a mechanical fault is confirmed without inspection
                    - For red warning lights, braking issues, overheating, smoke, steering problems, or serious safety concerns, recommend stopping safely and contacting a qualified mechanic or emergency service
                    - Do not provide unsafe roadside repair instructions
                    - Keep normal answers concise
                    - Mention that the answer may vary by exact trim, engine, region, and vehicle condition
                """.trimIndent())
            }

            val generativeModel = ai.generativeModel(
                modelName = AiConfig.MODEL_NAME,
                systemInstruction = systemInstruction
            )

            val chatHistory = conversationHistory.map { msg ->
                content(role = if (msg.sender == ChatSender.USER) "user" else "model") {
                    text(msg.text)
                }
            }

            val chatSession = generativeModel.startChat(history = chatHistory)
            
            val response = chatSession.sendMessage(userMessage)
            val responseText = response.text
            
            return if (responseText != null) {
                Result.success(responseText)
            } else {
                Result.failure(Exception("The AI did not return a response. Please try again."))
            }

        } catch (e: UnknownHostException) {
            Log.e(TAG, "UnknownHostException in sendMessage", e)
            return Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "IOException in sendMessage", e)
            return Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception class: ${e.javaClass.canonicalName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Full stack trace:", e)
            
            // Print to Logcat (already covered by Log.e(TAG, ..., e) but being explicit)
            e.printStackTrace()

            // Return original exception as requested
            return Result.failure(e)
        }
    }
}
