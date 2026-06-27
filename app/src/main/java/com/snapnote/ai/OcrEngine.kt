package com.snapnote.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * OCR 引擎：本地文字识别
 * 在实际项目中可接入 ML Kit Text Recognition 或 Tesseract
 * 当前提供模拟实现，展示完整的扫描-识别流程
 */
interface OcrEngine {
    suspend fun recognize(uri: Uri): OcrResult
}

data class OcrResult(
    val text: String,
    val blocks: List<TextBlock>,
    val confidence: Float
)

data class TextBlock(
    val text: String,
    val boundingBox: android.graphics.Rect? = null
)

class LocalOcrEngine(private val context: Context) : OcrEngine {
    override suspend fun recognize(uri: Uri): OcrResult = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmap(uri)
            // 模拟 OCR 识别：实际项目中接入 ML Kit
            val simulatedText = simulateRecognition(bitmap)
            val blocks = simulatedText.split("\n").filter { it.isNotBlank() }.map { TextBlock(it) }
            OcrResult(
                text = simulatedText,
                blocks = blocks,
                confidence = 0.92f
            )
        } catch (e: Exception) {
            OcrResult(
                text = "",
                blocks = emptyList(),
                confidence = 0f
            )
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun simulateRecognition(bitmap: Bitmap?): String {
        // 模拟识别结果，实际项目中替换为真实 OCR
        return buildString {
            appendLine("第1章 运动学")
            appendLine("1.1 匀变速直线运动")
            appendLine("匀变速直线运动是指加速度恒定的直线运动。")
            appendLine("位移公式：s = v0t + 1/2 * at^2")
            appendLine("速度公式：v = v0 + at")
            appendLine()
            appendLine("1.2 自由落体运动")
            appendLine("自由落体是初速度为零、加速度为g的匀加速运动。")
            appendLine("下落高度：h = 1/2 * gt^2")
            appendLine()
            appendLine("例题1：一辆汽车以10m/s初速度匀加速行驶，加速度2m/s^2，求5s后的位移。")
            appendLine("解：s = 10*5 + 1/2*2*25 = 50 + 25 = 75m")
            appendLine()
            appendLine("易错点：注意区分位移和路程的概念")
            appendLine("位移是矢量，有方向；路程是标量，无方向。")
        }
    }
}
