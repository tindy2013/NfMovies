package com.futuretech.nfmovies.API

import android.util.Log
import com.futuretech.nfmovies.API.Duboku.Companion.NAME
import com.futuretech.nfmovies.Entity.Category
import com.futuretech.nfmovies.Entity.Episode
import com.futuretech.nfmovies.Entity.Episodes
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.Utils.OkHttpUtil

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

class Duboku : ISite {

    override val categories: List<Category>
        get() {
            val html = OkHttpUtil.instance.getHtml(HOST, HOST) ?: return ArrayList()
            val doc = Jsoup.parse(html)
            val all = doc.getElementsByClass("myui-panel-box clearfix")
            val categories = ArrayList<Category>()
            for (e in all) {
                val category = Category()
                val title = e.getElementsByClass("title").first().text()
                Log.i("Duboku title", title)
                category.title = title
                val more = e.selectFirst("a[class=more]")
                if (more != null) category.url = HOST + more.attr("href")
                val movieBoxes = e.getElementsByClass("myui-vodlist__box")
                val movies = ArrayList<Movie>()
                for (movieEl in movieBoxes) {
                    val thumb = movieEl.getElementsByClass("myui-vodlist__thumb lazyload").first()
                    val movie = Movie(thumb.attr("title"),
                            thumb.attr("data-original"), HOST + thumb.attr("href"),
                            SITE)
                    movies.add(movie)
                }
                category.movies = movies
                categories.add(category)
            }
            return categories
        }

    override val site: Site
        get() = SITE

    override val name: String
        get() = NAME

    override val host: String
        get() = HOST

    override fun getMovieDetail(movie: Movie): Movie {
        val html = OkHttpUtil.instance.getHtml(movie.url!!, HOST) ?: return Movie()
        val doc = Jsoup.parse(html)
        val desc = doc.select("meta[name=description]").first().attr("content")
        movie.description = desc
        val episodes = doc.getElementById("playlist1")
        val a = episodes.select("li>a")
        val epList = ArrayList<Episode>()
        for (epEl in a) {
            val ep = Episode(epEl.text(), epEl.attr("href"))
            epList.add(ep)
        }
        val episodesList = ArrayList<Episodes>()
        episodesList.add(Episodes(NAME, epList))
        movie.episodes = episodesList
        return movie
    }

    override fun getPlayURL(e: Episode): String {
        val url = e.url
        val html = OkHttpUtil.instance.getHtml(url, HOST)
        val p = Pattern.compile("player_data[\\s\\S]*?\"url\":\"(.*?)\"")
        val m = p.matcher(html!!)
        var play: String? = null
        if (m.find()) {
            play = m.group(1)
            play = play!!.replace("\\\\".toRegex(), "")
        }
        if (play!!.contains("share")) {
            val refer = "https://v.zdubo.com"
            val realHtml = OkHttpUtil.instance.getHtml(play, refer)
            val main = Pattern.compile("main[\\s\\S]*?=[\\s\\S]*?\"(.*?)\"")
            val km = main.matcher(realHtml!!)
            if (km.find()) {
                var m3u8 = km.group(1)
                Log.d("m3u8", m3u8!!)
                m3u8 = m3u8.replace("index.*".toRegex(), "hls/index.m3u8")
                play = refer + m3u8
            } else {
                return ""
            }
        }
        return play
    }

    override fun search(str: String): Category {
        val url = "$HOST/vodsearch/-------------.html?wd=$str"
        val html = OkHttpUtil.instance.getHtml(url, HOST)
        val doc = Jsoup.parse(html)
        val resultEl = doc.getElementsByClass("thumb")
        if (resultEl.size == 0) return Category()
        val movies = ArrayList<Movie>()
        for (single in resultEl) {
            val a = single.select("a").first()
            val m = Movie(a.attr("title"), a.attr("data-original"),
                    HOST + a.attr("href"), SITE)
            movies.add(m)

        }
        return Category(NAME, movies, "")
    }

    companion object {
        private const val HOST = "https://www.duboku.net"
        const val NAME = "独播库"
        private val mInstance: Duboku = Duboku()
        private val SITE = Site.DUBOKU

        val instance: Duboku
            @Synchronized get() {
                return mInstance
            }
    }
}
