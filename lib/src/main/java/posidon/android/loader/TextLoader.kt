package posidon.android.loader

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import kotlin.concurrent.thread

object TextLoader {

    fun loadText(
        url: String,
        onFinished: (string: String) -> Unit
    ) = thread {
        try {
            val builder = StringBuilder()
            var buffer: String?
            val bufferReader = BufferedReader(InputStreamReader(URL(url).openStream()))
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