package posidon.android.loader.text

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

object TextLoader {

    /**
     * Asynchronously loads the text from the [url].
     * If it succeeds, the [onFinished] function is run with the result as the parameter
     *
     * [url] must start with http:// or https://
     */
    inline fun load(
        url: String,
        crossinline onFailed: (Exception) -> Unit = {},
        crossinline onFinished: (String) -> Unit,
    ): Thread {
        return load(URL(url), onFailed, onFinished)
    }

    /**
     * Asynchronously loads the text from the [url].
     * If it succeeds, the [onFinished] function is run with the result as the parameter
     *
     * [url] must start with http:// or https://
     */
    inline fun load(
        url: URL,
        crossinline onFailed: (Exception) -> Unit = {},
        crossinline onFinished: (String) -> Unit,
    ): Thread {
        return thread(name = "TextLoader thread") {
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
            catch (e: IOException) { onFailed(e) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }
}