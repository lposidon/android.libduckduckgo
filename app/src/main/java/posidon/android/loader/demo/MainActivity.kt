package posidon.android.loader.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            Column(this,
                ActivityButton(this, "Rss", RSSTestActivity::class.java),
                ActivityButton(this, "DuckDuckGo", DDGTestActivity::class.java),
                ActivityButton(this, "TextLoading", TextLoadingTestActivity::class.java)
            )
        )
    }
}