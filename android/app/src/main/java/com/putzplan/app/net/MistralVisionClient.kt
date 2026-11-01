package com.putzplan.app.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class FotoAufgabe(
    val titel: String = "",
    val bereich: String = "",
    val dauerMin: Int = 15,
    val hilfsmittel: List<String> = emptyList(),
    val anleitung: String = "",
    val intervall: String = "woechentlich",
    val prioritaet: Double = 0.5
)

@Serializable
data class FotoAnalyse(
    val raum: String = "",
    val beobachtungen: List<String> = emptyList(),
    val aufgaben: List<FotoAufgabe> = emptyList()
)

object FotoPrompt {
    fun fuerRaum(raum: String, anzahlBilder: Int = 1): String {
        val einleitung = if (anzahlBilder > 1)
            "Analysiere die folgenden $anzahlBilder Fotos desselben Raums zusammen (verschiedene Perspektiven/Bereiche). Fasse die Beobachtungen \u00fcber alle Bilder hinweg zusammen."
        else
            "Analysiere das Foto eines Raums (Kontext: Raum \"$raum\")."
        return """
        Du bist ein Experte f\u00fcr Wohnungsreinigung. $einleitung
        Erkenne sichtbare Objekte, Oberfl\u00e4chen und Verschmutzungen (Staub, Flecken, Kalk, Fett, Unordnung).
        Leite daraus konkrete Reinigungsaufgaben ab. Antworte AUSSCHLIESSLICH als JSON-Objekt mit diesem Schema:
        {
          "raum": "<erkannter Raum>",
          "beobachtungen": ["..."],
          "aufgaben": [
            {
              "titel": "kurzer Aufgabentitel",
              "bereich": "z.B. Boden, Fenster, Arbeitsfl\u00e4che",
              "dauerMin": 15,
              "hilfsmittel": ["..."],
              "anleitung": "Schritt-f\u00fcr-Schritt",
              "intervall": "taeglich|woechentlich|zweiwoechentlich|monatlich|quartal|halbjaehrlich|jaehrlich",
              "prioritaet": 0.0
            }
          ]
        }
        prioritaet: 0-1, wie dringend/auff\u00e4llig die Aufgabe auf den Fotos wirkt. dauerMin realistisch sch\u00e4tzen.
        Jede Aufgabe darf nur einmal vorkommen (nicht pro Bild wiederholen).
    """.trimIndent()
    }
}

class MistralVisionClient(
    private val apiKey: String,
    private val model: String = "mistral-medium-latest"
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun analysiereFoto(raum: String, imageDataUrl: String): Result<FotoAnalyse> =
        analysiereFotos(raum, listOf(imageDataUrl))

    suspend fun analysiereFotos(raum: String, imageDataUrls: List<String>): Result<FotoAnalyse> =
        withContext(Dispatchers.IO) {
            runCatching {
                val requestJson = buildJsonObject {
                    put("model", model)
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", "user")
                            put("content", buildJsonArray {
                                add(buildJsonObject {
                                    put("type", "text")
                                    put("text", FotoPrompt.fuerRaum(raum, imageDataUrls.size))
                                })
                                imageDataUrls.forEach { url ->
                                    add(buildJsonObject {
                                        put("type", "image_url")
                                        put("image_url", buildJsonObject {
                                            put("url", url)
                                        })
                                    })
                                }
                            })
                        })
                    })
                    put("response_format", buildJsonObject { put("type", "json_object") })
                    put("temperature", 0.2)
                }.toString()

                val conn = URL("https://api.mistral.ai/v1/chat/completions").openConnection()
                    as HttpURLConnection
                try {
                    conn.requestMethod = "POST"
                    conn.connectTimeout = 15000
                    conn.readTimeout = 60000
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Authorization", "Bearer $apiKey")

                    conn.outputStream.use { it.write(requestJson.toByteArray(Charsets.UTF_8)) }

                    val code = conn.responseCode
                    val antwort = (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader()?.use { it.readText() } ?: ""
                    check(code in 200..299) {
                        "Mistral API Fehler (HTTP $code): ${antwort.take(200)}"
                    }

                    val wrapper = json.decodeFromString<ChatResponse>(antwort)
                    val content = wrapper.choices.firstOrNull()?.message?.content ?: "{}"
                    json.decodeFromString<FotoAnalyse>(content)
                } finally {
                    conn.disconnect()
                }
            }
        }
}

@Serializable
data class ChatResponse(val choices: List<Choice> = emptyList()) {
    @Serializable
    data class Choice(val message: Message = Message()) {
        @Serializable
        data class Message(val role: String = "", val content: String = "")
    }
}
