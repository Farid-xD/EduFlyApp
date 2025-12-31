package com.example.proj.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiService {

    private const val TAG = "GeminiAPI"

    // YOUR API KEY
    private const val API_KEY = "AIzaSyDgxfP567tZ8NGy9vpY5F214LXiI3dBVfc"

    // CORRECT ENDPOINT
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun isValidApiKey(): Boolean {
        return API_KEY.isNotBlank() &&
                API_KEY.length > 30 &&
                API_KEY.startsWith("AIza")
    }

    suspend fun generateCourseContent(
        prompt: String,
        includeQuiz: Boolean = false,
        includeMCQs: Boolean = false,
        includeVideo: Boolean = false
    ): ApiResult<String> {
        return try {
            // Validate API key
            if (!isValidApiKey()) {
                Log.e(TAG, "Invalid API key")
                return ApiResult.Error("Invalid or missing API key. Please check your configuration.")
            }

            val url = "$BASE_URL?key=$API_KEY"

            // Build the full prompt
            val fullPrompt = buildCoursePrompt(prompt, includeQuiz, includeMCQs, includeVideo)

            Log.d(TAG, "Sending request to Gemini API...")
            Log.d(TAG, "Prompt length: ${fullPrompt.length}")

            // Build request body according to Gemini API specs
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", fullPrompt)
                            })
                        })
                    })
                })

                // Generation config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topK", 40)
                    put("topP", 0.95)
                    put("maxOutputTokens", 4096) // Increased for video data
                })

                // Safety settings
                put("safetySettings", JSONArray().apply {
                    put(JSONObject().apply {
                        put("category", "HARM_CATEGORY_HARASSMENT")
                        put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                    })
                    put(JSONObject().apply {
                        put("category", "HARM_CATEGORY_HATE_SPEECH")
                        put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                    })
                    put(JSONObject().apply {
                        put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT")
                        put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                    })
                    put(JSONObject().apply {
                        put("category", "HARM_CATEGORY_DANGEROUS_CONTENT")
                        put("threshold", "BLOCK_MEDIUM_AND_ABOVE")
                    })
                })
            }

            val body = requestBody.toString()
            Log.d(TAG, "Request body prepared, size: ${body.length} bytes")

            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            withContext(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: ${responseBody?.take(500)}")

                when {
                    response.isSuccessful -> {
                        if (responseBody != null) {
                            val content = parseApiResponse(responseBody)
                            if (content.isNotBlank()) {
                                Log.d(TAG, "Successfully parsed response")
                                ApiResult.Success(content)
                            } else {
                                Log.e(TAG, "Empty content received")
                                ApiResult.Error("AI generated empty content. Please try again.")
                            }
                        } else {
                            Log.e(TAG, "Response body is null")
                            ApiResult.Error("No response from API")
                        }
                    }
                    response.code == 400 -> {
                        Log.e(TAG, "Bad Request (400): $responseBody")
                        val errorMsg = parseErrorMessage(responseBody)
                        ApiResult.Error("Invalid request: $errorMsg")
                    }
                    response.code == 429 -> {
                        Log.e(TAG, "Rate limit exceeded")
                        ApiResult.Error("Rate limit exceeded. Please try again in a few minutes.")
                    }
                    response.code == 403 -> {
                        Log.e(TAG, "API key issue (403)")
                        ApiResult.Error("API key invalid or quota exceeded. Please check your API key.")
                    }
                    else -> {
                        Log.e(TAG, "API Error ${response.code}: ${response.message}")
                        ApiResult.Error("API Error: ${response.code} - ${response.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            ApiResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    private fun buildCoursePrompt(
        userPrompt: String,
        includeQuiz: Boolean,
        includeMCQs: Boolean,
        includeVideo: Boolean
    ): String {
        return """
Create a comprehensive online course based on the following topic:

$userPrompt

Please structure the course with these sections:

1. COURSE OVERVIEW
   - Brief introduction (2-3 sentences)
   - Learning objectives (3-5 bullet points)

2. COURSE MODULES (Create 4-6 modules)
   For each module include:
   - Module title
   - 3-4 key topics covered
   - Brief description

3. DETAILED CONTENT
   - Provide detailed explanation for the first module
   - Include key concepts and examples

${if (includeQuiz) """
4. ASSESSMENT QUESTIONS
   - Create 3 open-ended questions to test understanding
   - Include brief answer guidelines
""" else ""}

${if (includeMCQs) """
5. MULTIPLE CHOICE QUESTIONS
   - Create 3 multiple choice questions
   - Provide 4 options (A, B, C, D) for each
   - Mark the correct answer (0 for A, 1 for B, 2 for C, 3 for D)
   - Provide explanation
""" else ""}

${if (includeVideo) """
6. RECOMMENDED YOUTUBE VIDEOS
   - Suggest 3 specific YouTube videos that would complement this course
   - For each video, provide the following EXACT JSON format:
     {
       "title": "Clear, descriptive video title",
       "searchQuery": "specific youtube search query",
       "description": "Brief description of video content",
       "channelName": "YouTube channel name"
     }
   - Return ONLY a JSON array containing these 3 video objects
   - Make search queries specific and searchable on YouTube
   - Example search queries: "C++ data structures tutorial for beginners", "Learn OOP in Java full course", "Python machine learning basics tutorial"
""" else ""}

7. FINAL PROJECT
   - Suggest a practical project students can complete
   - Include key deliverables

8. ADDITIONAL RESOURCES
   - List 2-3 recommended resources (books, websites, tools)

IMPORTANT: For section 6 (RECOMMENDED YOUTUBE VIDEOS), return ONLY a pure JSON array. Do not include any text before or after the JSON.
        """.trimIndent()
    }

    private fun parseApiResponse(response: String?): String {
        return try {
            if (response.isNullOrBlank()) {
                Log.e(TAG, "Empty response")
                return ""
            }

            val json = JSONObject(response)
            Log.d(TAG, "Parsing JSON response...")

            // Check for prompt feedback (blocking)
            if (json.has("promptFeedback")) {
                val feedback = json.getJSONObject("promptFeedback")
                if (feedback.optBoolean("blocked", false)) {
                    Log.e(TAG, "Content blocked by safety filters")
                    return "Content blocked due to safety policies. Please try a different topic or rephrase your request."
                }
            }

            // Get candidates array
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                Log.e(TAG, "No candidates in response")
                return "No content generated. Please try again."
            }

            val candidate = candidates.getJSONObject(0)

            // Check finish reason
            val finishReason = candidate.optString("finishReason", "")
            if (finishReason == "SAFETY") {
                Log.e(TAG, "Content blocked for safety")
                return "Content blocked for safety reasons. Please try a different topic."
            }

            // Extract the text content
            val content = candidate.optJSONObject("content")
            if (content == null) {
                Log.e(TAG, "No content object")
                return ""
            }

            val parts = content.optJSONArray("parts")
            if (parts == null || parts.length() == 0) {
                Log.e(TAG, "No parts in content")
                return ""
            }

            val textPart = parts.getJSONObject(0)
            val text = textPart.optString("text", "").trim()

            Log.d(TAG, "Successfully extracted text, length: ${text.length}")
            return text

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response: ${e.message}", e)
            return "Error parsing AI response: ${e.localizedMessage}"
        }
    }

    private fun parseErrorMessage(response: String?): String {
        return try {
            if (response.isNullOrBlank()) return "Unknown error"

            val json = JSONObject(response)
            val error = json.optJSONObject("error")
            error?.optString("message", "Unknown error") ?: "Unknown error"
        } catch (e: Exception) {
            "Unable to parse error"
        }
    }
}

// Result sealed class
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}