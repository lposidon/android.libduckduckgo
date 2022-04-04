package io.posidon.android.libduckduckgo

import android.net.Uri
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

object DuckDuckGo {

    fun searchURL(query: String, t: String): String = "https://duckduckgo.com/?q=${Uri.encode(query)}&t=$t"

    /**
     * Async version of [DuckDuckGo.instantAnswer(String, String)]
     */
    fun instantAnswer(
        query: String,
        t: String,
        onFailed: () -> Unit = {},
        onSuccess: (InstantAnswer) -> Unit,
    ) {
        thread(name = "DuckDuckGo loading thread", isDaemon = true) thread@ {
            instantAnswer(query, t)?.let(onSuccess) ?: onFailed()
        }
    }

    /**
     * Async version of [DuckDuckGo.instantAnswer(URL)]
     */
    fun instantAnswer(
        url: URL,
        onFailed: () -> Unit = {},
        onSuccess: (InstantAnswer) -> Unit,
    ) {
        thread(name = "DuckDuckGo loading thread", isDaemon = true) thread@ {
            instantAnswer(url)?.let(onSuccess) ?: onFailed()
        }
    }

    /**
     * Loads a DuckDuckGo instant answer for [query]
     *
     * @param query  The query, duh
     * @param t      An indicator of where the search came from (required by DuckDuckGo's TOS)
     */
    fun instantAnswer(query: String, t: String): InstantAnswer? = instantAnswer(URL("https://api.duckduckgo.com/?q=${Uri.encode(query)}&format=json&t=$t"))

    /**
     * Loads a DuckDuckGo instant answer from [URL]
     */
    fun instantAnswer(url: URL): InstantAnswer? {
        try {
            val builder = StringBuilder()
            var buffer: String?
            val bufferReader = BufferedReader(InputStreamReader(url.openStream()))
            while (bufferReader.readLine().also { buffer = it } != null) {
                builder.append(buffer).append('\n')
            }
            bufferReader.close()
            val rawData = builder.toString()

            try {
                val jObject = JSONObject(rawData)
                val title = jObject.getString("Heading")
                if (title.isBlank()) return null
                val sourceName = jObject.getString("AbstractSource")
                if (sourceName.isBlank()) return null
                val sourceUrl = jObject.getString("AbstractURL")
                if (sourceUrl.isBlank()) return null
                val type = jObject.getString("Type")
                if (type.isBlank()) return null
                val description =
                    if (type == "D") jObject
                        .getJSONArray("RelatedTopics")
                        .getJSONObject(0)
                        .getString("Text")
                    else jObject
                        .getString("AbstractText")
                val infoArray = jObject
                    .optJSONObject("Infobox")
                    ?.optJSONArray("content")
                val infoTable = if (infoArray == null) null else Array(infoArray.length()) {
                    val e = infoArray.getJSONObject(it)
                    InfoboxEntry(
                        e.getString("data_type"),
                        e.getString("label"),
                        e.getString("value"),
                        e.getInt("wiki_order"),
                    )
                }
                return InstantAnswer(
                    title,
                    sourceName,
                    sourceUrl,
                    description,
                    infoTable,
                    rawData,
                )
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        catch (e: IOException) {}
        return null
    }
}