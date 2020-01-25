package com.futuretech.nfmovies.API

import android.util.Log
import com.futuretech.nfmovies.Entity.Category
import com.futuretech.nfmovies.Entity.Episode
import com.futuretech.nfmovies.Entity.Episodes
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.Utils.OkHttpUtil
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.net.URLDecoder
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class NfMovies private constructor() : ISite {


    override val site: Site
        get() = SITE

    override val name: String
        get() = NAME

    override val host: String
        get() = HOST

    override val categories: List<Category>
        get() {
            val html = OkHttpUtil.instance.getHtml(HOST, HOST) ?: return ArrayList()
            val doc = Jsoup.parse(html)
            val elements = doc.getElementsByClass("hy-layout clearfix")
            val categories = ArrayList<Category>()
            for (i in elements.indices) {
                val element = elements[i]
                val title = element.getElementsByClass("margin-0").text()
                val url = element.getElementsByClass("active").select("a").attr("href")
                Log.i("NFMOVIES", "$title $url")
                val movieElements = element.getElementsByClass("videopic lazy")
                if (movieElements.size < 1)
                    continue
                val movies = ArrayList<Movie>()
                for (e in movieElements) {
                    val href = e.attr("href")
                    val img = e.attr("data-original")
                    val name = e.attr("title")
                    val m = Movie(name, HOST + img, HOST + href, SITE)
                    movies.add(m)
                }
                categories.add(Category(title, movies, HOST + url))
            }
            return categories
        }

    override fun getMovieDetail(movie: Movie): Movie {
        val html = OkHttpUtil.instance.getHtml(movie.url!!, HOST) ?: return Movie()
        val doc = Jsoup.parse(html)
        val de = doc.getElementById("list3")
        movie.description = de.text().trim { it <= ' ' }
        val elements = doc.getElementsByClass("panel clearfix")
        val episodesList = ArrayList<Episodes>()
        for (e in elements) {
            val episodes = ArrayList<Episode>()
            val es = e.getElementsByClass("list-15256").select("li>a")
            for (et in es) {
                val episode = Episode(et.attr("title"),
                        HOST + et.attr("href"))
                episodes.add(episode)
            }
            val title = e.getElementsByClass("option").first().attr("title")
            val eps = Episodes(title, episodes)
            episodesList.add(eps)
        }
        movie.episodes = episodesList
        return movie
    }

    @Throws(Exception::class)
    override fun getPlayURL(episode: Episode): String {
        var url = episode.url
        var html = OkHttpUtil.instance.getHtml(url, HOST)
        if (html == null) {
            Log.d("html:", "null")
            return ""
        }
        Log.d("parse", "start")
        val pattern = Pattern.compile("now=unescape\\(\"(.*?)\"\\)")
        val matcher = pattern.matcher(html)
        var shumafen = ""
        if (matcher.find())
            shumafen = matcher.group(1)!!.toString()


        shumafen = URLDecoder.decode(shumafen, "utf-8")
        Log.d("NFMOVIES", shumafen)
        shumafen = shumafen.replace("http:", "https:")
        if (shumafen.contains("m3u8")) return shumafen
        if (shumafen.contains("youku.com")) {
            val kuyun = OkHttpUtil.instance.getHtml(shumafen, HOST)
            Log.d("NFMOVIES", kuyun!!)
            val kuyunp = Pattern.compile("main[\\s\\S]*?=[\\s\\S]*?\"(.*?)\"")
            val km = kuyunp.matcher(kuyun)
            return if (km.find()) {
                var m3u8 = km.group(1)
                Log.d("m3u8", m3u8!!)
                m3u8 = m3u8.replace("index.*".toRegex(), "1000k/hls/index.m3u8")
                shumafen = shumafen.replace("/share.*".toRegex(), m3u8)
                Log.d("NFMOVIES", shumafen)
                shumafen
            } else {
                ""
            }
        }


        val b = StringBuilder()
        b.append(HOST).append("/api/vproxy.php?url=").append(shumafen).append("&type=json")
        url = b.toString()
        Log.d("url", url)
        html = OkHttpUtil.instance.getHtml(url, "https://www.nfmovies.com/js/player/mp4.html?191026")
        Log.d("html", html!!)
        var `object`: JSONObject? = null
        try {
            `object` = JSONObject(html)
            url = `object`.get("data").toString()
            Log.i("NFMOVIES", url)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return if (`object` == null) "" else url
    }

    override fun search(str: String): Category {
        val html = OkHttpUtil.instance.getHtml("$HOST/search.php?searchword=$str",
                HOST)
                ?: return Category()
        val category = Category()
        category.title = "奈菲影视"
        val movies = ArrayList<Movie>()
        val doc = Jsoup.parse(html)
        val eles = doc.getElementsByClass("content")
        if (eles.size == 0) return Category()
        for (e in eles) {
            val m = Movie()
            m.site = Site.NFMOVIES
            val pic = e.getElementsByClass("videopic").first()
            m.url = HOST + pic.attr("href")
            val style = pic.attr("style")
            val p = Pattern.compile("url\\((.*?)\\)")
            val matcher = p.matcher(style)
            if (matcher.find()) m.img = HOST + matcher.group(1)!!
            val name = e.getElementsByClass("head").first().text()
            m.name = name
            movies.add(m)
        }
        category.movies = movies
        return category
    }

    private fun getSearch(url: String, page: Int): List<Movie>? {
        val movies = ArrayList<Movie>()
        val u = "$url&page=$page"
        val html = OkHttpUtil.instance.getHtml(u, HOST) ?: return null


        return null
    }

    companion object {

        private const val HOST = "https://www.nfmovies.com"
        const val NAME = "奈菲影视"
        private val mInstance: NfMovies = NfMovies()
        private val SITE = Site.NFMOVIES


        val instance: NfMovies
            @Synchronized get() {
                return mInstance
            }
    }

}
