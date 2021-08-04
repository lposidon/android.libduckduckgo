package posidon.android.loader.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import posidon.android.loader.text.TextLoader

class TextLoadingTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = Text(this)
        val err = Text(this, textColor = 0xffff4444.toInt())
        val textField = TextField(this, hint = "https://example.com", textSize = 20f)
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
                            TextLoader.load(u, onFailed = { e ->
                                e.printStackTrace()
                                runOnUiThread {
                                    err.isVisible = true
                                    err.text = e.stackTraceToString()
                                    data.isVisible = false
                                }
                            }) { result ->
                                runOnUiThread {
                                    data.isVisible = true
                                    data.text = result
                                    err.isVisible = false
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