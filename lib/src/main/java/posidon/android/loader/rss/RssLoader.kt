package posidon.android.loader.rss

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.max

object RssLoader {

    private val pullParserFactory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
    private val endStrings = arrayOf("",
        "/feed",
        "/feed.xml",
        "/rss",
        "/rss.xml",
        "/atom",
        "/atom.xml",
    )

    /**
     * Loads the rss content asynchronously
     *
     * [feedUrls] A list of all the rss/atom feeds to load
     * [maxItems] Maximum amount of items to load (if 0, no limit is applied)
     * [doSorting] Whether to sort the feed items by time or not
     * [filter] A function to decide whether to include an item or not
     * [onFinished] The function to handle the loaded data
     */
    inline fun load(
        feedUrls: Collection<String>,
        maxItems: Int = 0,
        doSorting: Boolean = true,
        noinline filter: (url: String, title: String, time: Date) -> Boolean = { _, _, _ -> true },
        crossinline onFinished: (success: Boolean, items: List<RssItem>) -> Unit,
    ) = thread(name = "RssLoader loading thread", isDaemon = true) {
        val feedItems = ArrayList<RssItem>()
        val success = load(feedItems, feedUrls, maxItems, doSorting, filter)
        feedItems.trimToSize()
        onFinished(success, feedItems)
    }

    /**
     * Loads the rss content on the current thread
     *
     * [feedItems] The place to write the rss data to
     * [feedUrls] A list of all the rss/atom feeds to load
     * [maxItems] Maximum amount of items to load (if 0, no limit is applied)
     * [doSorting] Whether to sort the feed items by time or not
     * [filter] A function to decide whether to include an item or not
     * @return Whether the loading was successful or not
     */
    fun load(
        feedItems: MutableList<RssItem>,
        feedUrls: Collection<String>,
        maxItems: Int = 0,
        doSorting: Boolean = true,
        filter: (url: String, title: String, time: Date) -> Boolean = { _, _, _ -> true }
    ): Boolean {
        val success = loadFeedInternal(feedUrls, feedItems, filter, maxItems, doSorting)

        if (maxItems != 0) {
            if (feedItems.size > maxItems) {
                repeat(feedItems.size - maxItems) {
                    feedItems.removeAt(feedItems.lastIndex)
                }
            }
        }

        return success
    }

    private fun loadFeedInternal(
        feedUrls: Collection<String>,
        feedItems: MutableList<RssItem>,
        filter: (url: String, title: String, time: Date) -> Boolean,
        maxItems: Int,
        doSorting: Boolean
    ): Boolean {
        val lock = ReentrantLock()

        val erroredSources = LinkedList<RssSource>()
        val threads = LinkedList<Thread>()
        for (u in feedUrls) {
            if (u.isNotEmpty()) {
                val (url, domain, name) = getSourceInfo(u)
                threads.add(thread(name = "RssLoader internal thread", isDaemon = true) {
                    var i = 0
                    while (i < endStrings.size) {
                        try {
                            val newUrl = url + endStrings[i]
                            val connection = URL(newUrl).openConnection()
                            parseFeed(
                                connection.getInputStream(),
                                RssSource(name, newUrl, domain),
                                lock,
                                feedItems,
                                filter,
                                maxItems
                            )
                            i = -1
                            break
                        } catch (e: Exception) {}
                        i++
                    }
                    if (i != -1) {
                        erroredSources.add(RssSource(name, url, domain))
                    }
                })
            }
        }

        var i = 0
        var j: Int
        var temp: RssItem

        val m = System.currentTimeMillis()
        for (thread in threads) {
            val millis = System.currentTimeMillis() - m
            kotlin.runCatching {
                thread.join(max(60000 - millis, 0))
            }
        }

        if (doSorting) while (i < feedItems.size - 1) {
            j = i + 1
            while (j < feedItems.size) {
                if (feedItems[i].isBefore(feedItems[j])) {
                    temp = feedItems[i]
                    feedItems[i] = feedItems[j]
                    feedItems[j] = temp
                }
                j++
            }
            i++
        }


        for (s in erroredSources) {
            println("Feed source ${s.name} failed")
        }

        return feedItems.size != 0
    }

    private fun getSourceInfo(url: String): Triple<String, String, String> {
        val u = if (url.endsWith("/")) {
            url.substring(0, url.length - 1)
        } else url
        return if (u.startsWith("http://") || u.startsWith("https://")) {
            val slashI = u.indexOf('/', 8)
            val domain = if (slashI != -1) u.substring(0, slashI) else u
            Triple(
                u, domain, if (domain.startsWith("www.")) {
                    domain.substring(4)
                } else domain
            )
        } else {
            val slashI = u.indexOf('/')
            val domain = if (slashI != -1) u.substring(0, slashI) else u
            Triple(
                "https://$u", "https://$domain", if (domain.startsWith("www.")) {
                    domain.substring(4)
                } else domain
            )
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private inline fun parseFeed(inputStream: InputStream, source: RssSource, lock: ReentrantLock, feedItems: MutableList<RssItem>, filter: (url: String, title: String, time: Date) -> Boolean, maxItems: Int) {
        var title: String? = null
        var link: String? = null
        var img: String? = null
        var time: Date? = null
        var isItem = 0
        val items = ArrayList<RssItem>()
        inputStream.use {
            val parser: XmlPullParser = pullParserFactory.newPullParser()
            parser.setInput(inputStream, null)
            parser.nextTag()
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                val name = parser.name ?: continue
                when (parser.eventType) {
                    XmlPullParser.END_TAG -> when {
                        name.equals("item", ignoreCase = true) ||
                        name.equals("entry", ignoreCase = true) -> {
                            isItem = 0
                            if (title != null && link != null) {
                                if (filter(link!!, title!!, time!!)) {
                                    items.add(RssItem(title!!, link!!, img, time!!, source))
                                    if (maxItems != 0 && items.size >= maxItems) {
                                        return@use
                                    }
                                }
                            }
                            title = null
                            link = null
                            img = null
                            time = null
                        }
                    }
                    XmlPullParser.START_TAG -> when {
                        name.equals("item", ignoreCase = true) -> isItem = 1
                        name.equals("entry", ignoreCase = true) -> isItem = 2
                        isItem == 1 -> when { //RSS
                            name.equals("title", ignoreCase = true) -> title = getText(parser)
                            name.equals("guid", ignoreCase = true) -> {
                                val isPermaLink = parser.getAttributeValue(null, "isPermaLink")
                                if (isPermaLink == null || isPermaLink.toBoolean()) link = getText(parser)
                            }
                            name.equals("link", ignoreCase = true) && link == null -> link = getText(parser)
                            name.equals("pubDate", ignoreCase = true) -> {
                                val text = getText(parser)
                                    .replace("GMT", "+0000")
                                    .replace("EDT", "+0000")
                                    .trim()
                                time = try {
                                    SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(text)!!
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    try { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(text)!! }
                                    catch (e: Exception) {
                                        e.printStackTrace()
                                        Date(0)
                                    }
                                }
                            }
                            img == null -> when (name) {
                                "description", "content:encoded" -> {
                                    val result = getText(parser)
                                    val i = result.indexOf("src=\"", result.indexOf("img"))
                                    if (i != -1) {
                                        val end = result.indexOf("\"", i + 5)
                                        img = result.substring(i + 5, end)
                                    }
                                }
                                "image" -> img = getText(parser)
                                "media:content" -> {
                                    val medium = parser.getAttributeValue(null, "medium")
                                    val url = parser.getAttributeValue(null, "url")
                                    if (medium == "image" ||
                                        url.endsWith(".jpg") ||
                                        url.endsWith(".png") ||
                                        url.endsWith(".svg") ||
                                        url.endsWith(".jpeg")) {
                                        img = url
                                    }
                                }
                                "media:thumbnail", "enclosure" -> img = parser.getAttributeValue(null, "url")
                                "itunes:image" -> img = parser.getAttributeValue(null, "href")
                            }
                        }
                        isItem == 2 -> when { //Atom
                            name.equals("title", ignoreCase = true) -> title = getText(parser)
                            name.equals("id", ignoreCase = true) -> link = getText(parser)
                            name.equals("published", ignoreCase = true) ||
                            name.equals("updated", ignoreCase = true) -> {
                                val text = getText(parser).trim()
                                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                                time = try { format.parse(text)!! } catch (e: Exception) { Date(0) }
                            }
                            img == null && (name.equals("summary", ignoreCase = true) || name.equals("content", ignoreCase = true)) -> {
                                val result = getText(parser)
                                val i = result.indexOf("src=\"", result.indexOf("img"))
                                if (i != -1) {
                                    val end = result.indexOf("\"", i + 5)
                                    img = result.substring(i + 5, end)
                                }
                            }
                        }
                        name.equals("title", ignoreCase = true) -> {
                            val new = getText(parser)
                            if (new.isNotBlank()) {
                                source.name = new
                            }
                        }
                        name.equals("icon", ignoreCase = true) -> {
                            val new = getText(parser)
                            if (new.isNotBlank()) {
                                source.iconUrl = new
                            }
                        }
                        name.equals("image", ignoreCase = true) -> {
                            val new = parseInside(parser, "image", "url")
                            if (!new.isNullOrBlank()) {
                                source.iconUrl = new
                            }
                        }
                        name.equals("webfeeds:icon", ignoreCase = true) -> {
                            val new = getText(parser)
                            if (new.isNotBlank()) {
                                source.iconUrl = new
                            }
                        }
                        name.equals("webfeeds:accentColor", ignoreCase = true) -> {
                            val new = getText(parser)
                            if (new.isNotBlank()) {
                                val color = new.toInt(16)
                                source.accentColor = color or 0xff000000.toInt()
                            }
                        }
                    }
                }
            }
        }
        lock.lock()
        feedItems.addAll(items)
        lock.unlock()
    }

    private fun parseInside(parser: XmlPullParser, parentTag: String, childTag: String): String? {
        loop@ while (parser.next() != XmlPullParser.END_DOCUMENT) {
            val innerName = parser.name ?: continue
            when (parser.eventType) {
                XmlPullParser.END_TAG -> {
                    if (innerName == parentTag) return null
                }
                XmlPullParser.START_TAG -> {
                    when (innerName) {
                        childTag -> return getText(parser)
                    }
                }
            }
            parser.next()
        }
        return null
    }

    private inline fun getText(parser: XmlPullParser): String {
        return if (parser.next() == XmlPullParser.TEXT) {
            parser.text.also { parser.nextTag() }
        } else ""
    }
}