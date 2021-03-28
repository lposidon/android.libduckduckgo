package posidon.android.loader

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

object TextLoader {

    inline fun load(url: String, crossinline onFinished: (String) -> Unit): Thread = load(URL(url), onFinished)

    inline fun load(
        url: URL,
        crossinline onFinished: (String) -> Unit
    ): Thread = thread {
        try {
            val builder = StringBuilder()
            var buffer: String?
            val bufferReader = BufferedReader(InputStreamReader(url.openStream()))
            while (bufferReader.readLine().also { buffer = it } != null) {
                builder.append(buffer).append('\n')
            }
            bufferReader.close()
            onFinished(builder.toString())
        }
        catch (e: IOException) {}
        catch (e: Exception) { e.printStackTrace() }
    }
}