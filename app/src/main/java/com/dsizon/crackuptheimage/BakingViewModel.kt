package com.dsizon.crackuptheimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    fun sendPrompt(bitmap: Bitmap) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resizedBitmap = resizeAndCompressBitmap(bitmap, maxWidth = 800, maxHeight = 800, quality = 85)
                val humorousPrompt = "Here's a photo, now make it funny! Generate a humorous joke referencing it."

                val response = generativeModel.generateContent(
                    content {
                        image(resizedBitmap)
                        text(humorousPrompt)
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun resizeAndCompressBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int, quality: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height
        val width = if (bitmap.width > maxWidth) maxWidth else bitmap.width
        val height = (width / aspectRatio).toInt().coerceAtMost(maxHeight)

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
    }
}
