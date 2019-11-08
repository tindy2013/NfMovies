package com.xuvjso.nfmovies.API;

import android.service.autofill.FieldClassification;
import android.util.Log;
import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.Utils.OkHttpUtil;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seamless.util.logging.LoggingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duboku implements ISite {
    public static final String HOST = "https://www.duboku.net";
    public static final String NAME = "独播库";
    private static Duboku mInstance;
    private static final Site SITE = Site.DUBOKU;

    public static synchronized Duboku getInstance() {
        if (mInstance == null) {
            mInstance = new Duboku();
        }
        return mInstance;
    }

    @Override
    public List<Category> getCategories() {
        String html = OkHttpUtil.getInstance().getHtml(HOST, HOST);
        if (html == null) return null;
        Document doc = Jsoup.parse(html);
        Elements all = doc.getElementsByClass("myui-panel-box clearfix");
        List<Category> categories = new ArrayList<>();
        for (Element e : all) {
            Category category = new Category();
            String title = e.getElementsByClass("title").first().text();
            Log.i("Duboku title", title);
            category.setTitle(title);
            Element more = e.selectFirst("a[class=more]");
            if (more != null) category.setUrl(HOST + more.attr("href"));
            Elements movieBoxes = e.getElementsByClass("myui-vodlist__box");
            List<Movie> movies = new ArrayList<>();
            for (Element movieEl : movieBoxes) {
                Element thumb = movieEl.getElementsByClass("myui-vodlist__thumb lazyload").first();
                Movie movie = new Movie(thumb.attr("title"),
                        thumb.attr("data-original"), HOST + thumb.attr("href"),
                        SITE);
                movies.add(movie);
            }
            category.setMovies(movies);
            categories.add(category);
        }
        return categories;
    }

    @Override
    public Site getSite() {
        return SITE;
    }

    @Override
    public Movie getMovieDetail(Movie movie) {
        String html = OkHttpUtil.getInstance().getHtml(movie.getUrl(), HOST);
        if (html == null) return null;
        Document doc = Jsoup.parse(html);
        String desc = doc.select("meta[name=description]").first().attr("content");
        movie.setDescription(desc);
        Element episodes = doc.getElementById("playlist1");
        Elements a = episodes.select("li>a");
        List<Episode> epList = new ArrayList<>();
        for (Element epEl : a) {
            Episode ep = new Episode(epEl.text(), epEl.attr("href"));
            epList.add(ep);
        }
        Map<String, List<Episode>> m = new HashMap<>();
        m.put(NAME, epList);
        movie.setEpisodes(m);
        return movie;
    }

    @Override
    public String getPlayURL(Episode e) {
        String url = e.getUrl();
        String html = OkHttpUtil.getInstance().getHtml(url, HOST);
        Pattern p = Pattern.compile("player_data[\\s\\S]*?\"url\":\"(.*?)\"");
        Matcher m = p.matcher(html);
        String play = null;
        if (m.find()) {
            play = m.group(1);
            play = play.replaceAll("\\\\", "");
        }
        if (play.contains("share")) {
            String refer = "https://v.zdubo.com";
            String realHtml = OkHttpUtil.getInstance().getHtml(play, refer);
            Pattern main = Pattern.compile("main[\\s\\S]*?=[\\s\\S]*?\"(.*?)\"");
            Matcher km = main.matcher(realHtml);
            if (km.find()) {
                String m3u8 = km.group(1);
                Log.d("m3u8", m3u8);
                m3u8 = m3u8.replaceAll("index.*", "hls/index.m3u8");
                play = refer + m3u8;
            } else {
                return null;
            }
        }
        return play;
    }

    @Override
    public Category search(String str) {
        String url = HOST + "/vodsearch/-------------.html?wd=" + str;
        String html = OkHttpUtil.getInstance().getHtml(url, HOST);
        Document doc = Jsoup.parse(html);
        Elements resultEl = doc.getElementsByClass("thumb");
        if (resultEl.size() == 0) return null;
        List<Movie> movies = new ArrayList<>();
        for (Element single : resultEl) {
            Element a = single.select("a").first();
            Movie m = new Movie(a.attr("title"), a.attr("data-original"),
                    HOST + a.attr("href"), SITE);
            movies.add(m);

        }
        Category category = new Category(NAME, movies, null);
        return category;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
