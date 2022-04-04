package io.posidon.android.libduckduckgo.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.posidon.android.libduckduckgo.DuckDuckGo
import io.posidon.android.libduckduckgo.InstantAnswer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = Text(this)
        val sourceName = Text(this)
        val sourceUrl = Text(this)
        val description = Text(this)
        val infobox = Text(this)
        val searchUrl = Text(this)
        val data = Text(this)
        val textField = TextField(this, hint = "Search...", textSize = 20f)
        setContentView(
            Scrollable(this,
                Column(this,
                    Row(this,
                        textField,
                        Button(this, "go") {
                            val q = textField.text.toString()
                            DuckDuckGo.instantAnswer(q, BuildConfig.APPLICATION_ID) { answer ->
                                val infoboxText = answer.infoTable?.joinToString("\n") {
                                    buildString {
                                        appendLine(it.dataType)
                                        appendLine(it.label)
                                        appendLine(it.value)
                                        appendLine(it.wikiOrder)
                                    }
                                }
                                runOnUiThread {
                                    title.text = answer.title
                                    sourceName.text = answer.sourceName
                                    sourceUrl.text = answer.sourceUrl
                                    description.text = answer.description
                                    infobox.text = infoboxText
                                    searchUrl.text = DuckDuckGo.searchURL(q, BuildConfig.APPLICATION_ID)
                                    data.text = answer.rawData
                                }
                            }
                        }
                    ),
                    Row(this, Text(this, "Search url: "), searchUrl),
                    Row(this, Text(this, "Answer title: "), title),
                    Row(this, Text(this, "Answer source name: "), sourceName),
                    Row(this, Text(this, "Answer source url: "), sourceUrl),
                    Row(this, Text(this, "Answer: "), description),
                    Row(this, Text(this, "Infobox: "), infobox),
                    Text(this, "Data:"),
                    data
                )
            )
        )
    }
}