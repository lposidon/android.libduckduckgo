package posidon.android.loader.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import posidon.android.loader.rss.RssItem
import posidon.android.loader.rss.RssLoader

class RSSTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = Text(this)
        val err = Text(this, textColor = 0xffff4444.toInt())
        val textField = TextField(this, hint = "https://example.com/rss", textSize = 20f)
        setContentView(
            Scrollable(this,
                Column(this,
                    Row(this,
                        textField,
                        Button(this, "go") {
                            var u = textField.text.toString()
                            if (!u.startsWith("https://") && !u.startsWith("http://")) {
                                u = "https://$u"
                            }
                            RssLoader.load(listOf(u)) { success: Boolean, items: List<RssItem> ->
                                if (success) {
                                    runOnUiThread {
                                        data.isVisible = true
                                        data.text = items.joinToString("\n\n") {
                                            buildString {
                                                append("Title: ")
                                                append(it.title)
                                                append('\n')
                                                append("Url: ")
                                                append(it.link)
                                                append('\n')
                                                append("Cover: ")
                                                append(it.img)
                                                append('\n')
                                                append("Time: ")
                                                append(it.time)
                                                append('\n')
                                                append("Source name: ")
                                                append(it.source.name)
                                                append('\n')
                                                append("Source domain: ")
                                                append(it.source.domain)
                                                append('\n')
                                                append("Source url: ")
                                                append(it.source.url)
                                                append('\n')
                                                append("Source icon: ")
                                                append(it.source.iconUrl)
                                                append('\n')
                                                append("Source color: ")
                                                append(it.source.accentColor.toString(16))
                                            }
                                        }
                                        err.isVisible = false
                                    }
                                } else {
                                    runOnUiThread {
                                        err.isVisible = true
                                        err.text = "Error"
                                        data.isVisible = false
                                    }
                                }
                            }
                        }
                    ),
                    err,
                    data,
                )
            )
        )
    }
}