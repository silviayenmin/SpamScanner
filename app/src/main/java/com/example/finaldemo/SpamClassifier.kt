package com.example.finaldemo

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.util.Log

object SmsClassifier {

    private var model: Interpreter? = null
    private var vocab: Map<String, Int>? = null
    private const val maxLen = 200

    private fun loadModel(context: Context, filename: String): MappedByteBuffer {
        val fd = context.assets.openFd(filename)
        val input = fd.createInputStream()
        return input.channel.map(
            FileChannel.MapMode.READ_ONLY,
            fd.startOffset,
            fd.declaredLength
        )
    }

    private fun loadVocab(context: Context, filename: String): Map<String, Int> {
        val map = HashMap<String, Int>()
        context.assets.open(filename).bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, word -> map[word] = index + 1 }
        }
        return map
    }

    /** call one time only */
    fun init(context: Context) {
        if (model == null) {
            model = Interpreter(loadModel(context, "sms_spam_model.tflite"))
            vocab = loadVocab(context, "vocab.txt")
        }
    }

    private fun tokenize(text: String): IntArray {
        val words = text.lowercase().split(Regex("\\W+"))
//        val tokens = words.map { vocab?.get(it) ?: 1 }
        val tokens = words.map { vocab?.get(it) ?: vocab?.get("<OOV>") ?: 1 }

        val arr = IntArray(maxLen) { 0 }
        val start = maxLen - tokens.size

        for (i in tokens.indices) {
            if (start + i >= 0 && start + i < maxLen)
                arr[start + i] = tokens[i]
        }
        return arr
    }







    fun predict(text: String): Pair<String, Float> {
        val tokens = tokenize(text)
        val floatTokens = FloatArray(tokens.size)   // FloatArray

        for (i in tokens.indices) {
            floatTokens[i] = tokens[i].toFloat()
        }
        // Correct input tensor shape: [1, 200] float32
//        val input = Array(1) { tokens.map { it.toFloat() }.toFloatArray() }
        val input = arrayOf(floatTokens)
        val output = Array(1) { FloatArray(3) }  // 3 classes

        model?.run(input, output)

        val result = output[0]
        val index = result.indices.maxByOrNull { result[it] } ?: 0

        val labels = arrayOf("HAM", "SPAM", "SMISHING")
        val label = labels[index]
        val confidence = result[index]

        Log.d("SMS_DEBUG", "TEXT: $text")
        Log.d("SMS_DEBUG", "TOKEN IDS: ${tokens.toList()}")
        Log.d("SMS_DEBUG", "MODEL RAW OUTPUT: ${result.toList()}")
        Log.d("SMS_DEBUG", "PREDICTION: $label ($confidence)")

        return Pair(label, confidence)
    }


}
