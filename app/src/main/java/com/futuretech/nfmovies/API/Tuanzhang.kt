package com.futuretech.nfmovies.API

import android.util.Log
import com.futuretech.nfmovies.Entity.Category
import com.futuretech.nfmovies.Entity.Episode
import com.futuretech.nfmovies.Entity.Episodes
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.Utils.AESUtil
import com.futuretech.nfmovies.Utils.OkHttpUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class Tuanzhang private constructor() : ISite {

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
            val elements = doc.getElementsByClass("mi_btcon")
            val categories = ArrayList<Category>()
            for (i in 1 until elements.size) {
                val element = elements[i]
                val a = element.selectFirst("div>div>h2>a")
                val title = a.text()
                val url = a.attr("href")
                val movieElements = element.getElementsByClass("thumb lazy")
                val movies = ArrayList<Movie>()
                for (e in movieElements) {
                    val href = e.parent().attr("href")
                    val img = e.attr("data-original")
                    val name = e.attr("alt")
                    Log.i("TUANZHANG", "$name $img")
                    val m = Movie(name, img, href, SITE)
                    movies.add(m)
                }
                categories.add(Category(title, movies, url))
            }
            return categories
        }

    override fun getMovieDetail(movie: Movie): Movie {
        val html = OkHttpUtil.instance.getHtml(movie.url!!, HOST) ?: return Movie()
        val doc = Jsoup.parse(html)
        val elements = doc.getElementsByClass("moviedteail_list")
        val epEl = doc.getElementsByClass("paly_list_btn")[0]
        val episodesList = ArrayList<Episodes>()
        val episodes = ArrayList<Episode>()
        for (e in epEl.children()) {
            episodes.add(Episode(e.text(), e.attr("href")))
        }
        episodesList.add(Episodes(NAME, episodes))
        movie.episodes = episodesList
        val de = doc.getElementsByClass("yp_context").text()
        movie.description = de
        return movie
    }

    override fun getPlayURL(ep: Episode): String {
        val url = ep.url
        val html = OkHttpUtil.instance.getHtml(url, HOST)
        val p1 = Pattern.compile("window\\.isplay=true;[\\s\\S]*?=\"(.*?)\"")
        var matcher = p1.matcher(html!!)
        var str1: String? = ""
        val str2: String?
        val iv: String?
        if (matcher.find()) {
            str1 = matcher.group(1)
            Log.i("str1", str1!!)
        }
        val p2 = Pattern.compile("md5\\.enc\\.Utf8\\.parse\\(\"(.*?)\"\\)")
        matcher = p2.matcher(html)
        if (matcher.find()) {
            str2 = matcher.group(1)
            Log.i("str2", matcher.group())
        } else {
            return ""
        }
        val p3 = Pattern.compile("md5\\.enc\\.Utf8\\.parse\\((\\d+?)\\)")
        matcher = p3.matcher(html)
        if (matcher.find()) {
            iv = matcher.group(1)
            Log.i("iv", matcher.group())
        } else {
            return ""
        }

        var js: String? = null
        try {
            js = AESUtil.decryptData(str1!!, str2!!, iv!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Log.i("TUANZHANG", "PLAY" + js!!)
        val p = Pattern.compile("url:[\\s\\S]*?[\"\'](.*?)[\"\']")
        Log.i("js", js)
        val m = p.matcher(js)
        if (m.find()) {
            Log.i("matcher", m.group(1)!!)
        }
        return m.group(1)!!
    }

    override fun search(str: String): Category {
        val html = OkHttpUtil.instance.getHtml("$HOST/?s=$str", HOST) ?: return Category()
        val category = Category()
        category.title = "团长资源"
        val doc = Jsoup.parse(html)
        val elements = doc.getElementsByClass("thumb lazy")
        if (elements.size == 0) return Category()
        val movies = ArrayList<Movie>()
        for (e in elements) {

            val href = e.parent().attr("href")
            val img = e.attr("data-original")
            val name = e.attr("alt")
            Log.i("TUANZHANG", "$name $img")
            val m = Movie(name, img, href, SITE)
            movies.add(m)
        }
        category.movies = movies
        category.url = ""
        return category
    }

    companion object {
        private const val HOST = "https://tzfile.com"
        const val NAME = "团长资源"
        private val mInstance: Tuanzhang = Tuanzhang()
        private val SITE = Site.TUANZHANG

        val instance: Tuanzhang
            @Synchronized get() {
                return mInstance
            }
    }


}
