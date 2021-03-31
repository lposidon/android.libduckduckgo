package posidon.android.loader.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

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

    inline fun <T : Activity> ActivityButton(context: Context, name: String, activity: Class<T>): View {
        return Button(context, name, textSize = 28f) {
            context.startActivity(Intent(context, activity))
        }
    }
}