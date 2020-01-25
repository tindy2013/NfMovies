package com.futuretech.nfmovies.API

import android.util.Log
import com.futuretech.nfmovies.Entity.Category
import com.futuretech.nfmovies.Entity.Episode
import com.futuretech.nfmovies.Entity.Episodes
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.Utils.OkHttpUtil
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class DDRK : ISite {

    override//     all.put("纪录片", "/category/documentary/");
    val categories: List<Category>
        get() {
            val categories = ArrayList<Category>()
            val all = HashMap<String, String>()
            all["热映中"] = "/category/airing/"
            all["站长推荐"] = "/tag/recommend/"
            all["剧集"] = "/category/drama/"
            all["电影"] = "/category/movie/"
            all["动画"] = "/category/anime/"
            for (name in all.keys) {
                val url = HOST + all[name]
                Log.i("DDRK", url)
                val html: String?
                try {
                    html = OkHttpUtil.instance.getHtml(url, url)
                } catch (e: Exception) {
                    continue
                }

                val doc = Jsoup.parse(html!!)
                val eles = doc.getElementsByClass("post-box-container")
                val movies = ArrayList<Movie>()
                for (t in eles) {
                    val movie = Movie()
                    val titleEl = t.getElementsByAttributeValue("rel", "bookmark")[0]
                    movie.url = titleEl.attr("href")
                    movie.name = titleEl.text()
                    val p = Pattern.compile("url\\((.*?)\\)")
                    val matcher = p.matcher(t.getElementsByClass("post-box-image")[0].attr("style"))
                    if (matcher.find())
                        movie.img = matcher.group(1)
                    else
                        Log.i("DDRK", movie.name!! + "未找到图片")
                    movie.site = SITE
                    movies.add(movie)
                }
                categories.add(Category(name, movies, url))
            }
            return categories
        }

    override val site: Site
        get() = SITE
    override val name: String
        get() = NAME

    override val host: String
        get() = HOST

    @Throws(Exception::class)
    override fun getMovieDetail(movie: Movie): Movie {
        Log.i("DDRK", movie.url!!)
        val pUrl = Pattern.compile(".*?//ddrk.me/.*?/")
        var url = movie.url!!
        val mUrl = pUrl.matcher(url)
        if (mUrl.find()) url = mUrl.group()
        var html: String? = OkHttpUtil.instance.getHtml(url, HOST) ?: return Movie()
        val dp = Pattern.compile("简介:([\\s\\S]*?)</")
        val ip = Pattern.compile("src=\"(.*?douban_cache.*?)\"")

        val dm = dp.matcher(html!!)
        if (dm.find()) movie.description = dm.group(1)

        if (movie.img == null) {
            val matcher = ip.matcher(html)
            if (matcher.find()) movie.img = matcher.group(1)
        }

        val doc = Jsoup.parse(html)
        val pageLink = doc.getElementsByClass("page-links").first()
        val episodesList = ArrayList<Episodes>()
        var ep = getSingleSeason(html)
        if (pageLink == null) {
            episodesList.add(Episodes(NAME, ep))
        } else {
            episodesList.add(Episodes("1", ep))
            val num = pageLink.select("a").size
            for (i in 2..num + 1) {
                val page = url + i.toString()
                html = OkHttpUtil.instance.getHtml(page, HOST)
                ep = getSingleSeason(html)
                episodesList.add(Episodes(i.toString(), ep))
            }
        }
        movie.episodes = episodesList
        return movie
    }

    @Throws(Exception::class)
    private fun getSingleSeason(html: String?): List<Episode> {
        val doc = Jsoup.parse(html!!)
        val episodesEl = doc.getElementsByClass("wp-playlist-script")
        val `object` = JSONObject(episodesEl[0].html())
        val ea = `object`.getJSONArray("tracks")
        val episodes = ArrayList<Episode>()
        for (i in 0 until ea.length()) {
            val e = Episode()
            e.name = ea.getJSONObject(i).getString("caption")
            e.url = ea.getJSONObject(i).getString("src")
            var caption = ea.getJSONObject(i).getString("subsrc")
            caption = caption.replace("\\\\".toRegex(), "")
            caption = "http://ddrk.oss-cn-shanghai.aliyuncs.com$caption"
            e.caption = caption
            episodes.add(e)
        }
        return episodes
    }

    @Throws(Exception::class)
    override fun getPlayURL(episode: Episode): String {
        var url = episode.url
        url = "http://v3.ddrk.me:9443/video?type=json&id=$url"
        val html = OkHttpUtil.instance.getHtml(url, HOST)
        val r = JSONObject(html!!)
        return r.getString("url")
    }

    override fun search(str: String): Category {
        val url = "https://www.sogou.com/web?query=site:ddrk.me+$str"
        val html = OkHttpUtil.instance.getHtml(url, HOST)
        val category = Category()
        category.title = NAME
        if (html == null) return Category()
        val movies = ArrayList<Movie>()
        val document = Jsoup.parse(html)
        val elements = document.select("[href^=/link]")
        for (t in elements) {
            if (t.attr("class").contains("img")) continue
            val m = Movie()
            m.site = SITE
            val href = "https://www.sogou.com" + t.attr("href")
            val result = OkHttpUtil.instance.getHtml(href, HOST)
            val pt = Pattern.compile("\\(\"([\\s\\S]*?)\"\\)")
            val mt = pt.matcher(result!!)
            if (mt.find())
                m.url = mt.group(1)
            else
                continue
            m.name = t.text()
            movies.add(m)
        }

        category.url = ""
        category.movies = movies
        return category
    }

    companion object {
        private const val HOST = "http://ddrk.me"
        const val NAME = "低端影视"
        private val mInstance: DDRK = DDRK()
        private val SITE = Site.DDRK

        val instance: DDRK
            @Synchronized get() {
                return mInstance
            }
    }
}
