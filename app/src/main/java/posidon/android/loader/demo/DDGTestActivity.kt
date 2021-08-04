package posidon.android.loader.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import posidon.android.loader.duckduckgo.DuckInstantAnswer

class DDGTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = Text(this)
        val sourceName = Text(this)
        val sourceUrl = Text(this)
        val description = Text(this)
        val infobox = Text(this)
        val searchUrl = Text(this)
        val data = Text(this)
        val textField = TextField(this, hint = "search...", textSize = 20f)
        setContentView(
            Scrollable(this,
                Column(this,
                    Row(this,
                        textField,
                        Button(this, "go") {
                            DuckInstantAnswer.load(textField.text.toString(), "posidon.android.loader") { answer ->
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
                                    searchUrl.text = answer.searchUrl
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