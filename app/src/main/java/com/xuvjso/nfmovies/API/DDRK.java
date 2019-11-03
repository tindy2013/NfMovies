package com.xuvjso.nfmovies.API;

import android.util.Log;
import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.Utils.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DDRK implements ISite {
    private static final String HOST = "http://ddrk.me";
    public static final String NAME = "低端影视";
    private static DDRK mInstance;
    private static final Type type = Type.DDRK;

    public static synchronized DDRK getInstance() {
        if (mInstance == null) {
            mInstance = new DDRK();
        }
        return mInstance;
    }

    @Override
    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        Map<String, String> all = new HashMap<String, String>();
        all.put("热映中", "/category/airing/");
        all.put("站长推荐", "/tag/recommend/");
        all.put("剧集", "/category/drama/");
        all.put("电影", "/category/movie/");
        all.put("动画", "/category/anime/");
   //     all.put("纪录片", "/category/documentary/");
        for (String name : all.keySet()) {
            String url = HOST + all.get(name);
            Log.i("DDRK", url);
            String html;
            try {
                html = OkHttpUtil.getInstance().getHtml(url, url);
            } catch (Exception e) {
                continue;
            }
            Document doc = Jsoup.parse(html);
            Elements eles = doc.getElementsByClass("post-box-container");
            List<Movie> movies = new ArrayList<>();
            for (Element t : eles) {
                Movie movie = new Movie();
                Element titleEl = t.getElementsByAttributeValue("rel", "bookmark").get(0);
                movie.setUrl(titleEl.attr("href"));
                movie.setName(titleEl.text());
                Pattern p = Pattern.compile("url\\((.*?)\\)");
                Matcher matcher = p.matcher(t.getElementsByClass("post-box-image").get(0).attr("style"));
                if (matcher.find()) movie.setImg(matcher.group(1));
                else Log.i("DDRK", movie.getName() + "未找到图片");
                movie.setSite(type);
                movies.add(movie);
            }
            categories.add(new Category(name, movies, url));
        }
        return categories;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Movie getMovieDetail(Movie movie) throws JSONException {
        Log.i("DDRK", movie.getUrl());
        String html = OkHttpUtil.getInstance().getHtml(movie.getUrl(), HOST);
        if (html == null) return null;
        Pattern tp = Pattern.compile("<br>类型:[\\s\\S*](.*?)<br>");
        Pattern yp = Pattern.compile("<br>年份:[\\s\\S*](\\d*?)<br>");
        Pattern dp = Pattern.compile("简介:([\\s\\S]*?)<\\/");
        Pattern ip= Pattern.compile("src=\"(.*?douban_cache.*?)\"");

        Matcher tm = tp.matcher(html);
        if (tm.find()) movie.setType(tm.group(1));
        Matcher ym = yp.matcher(html);
        if (ym.find()) movie.setYear(ym.group(1));
        Matcher dm = dp.matcher(html);
        if (dm.find()) movie.setDescription(dm.group(1));

        if (movie.getImg().equals("none")) {
            Matcher matcher = ip.matcher(html);
            if (matcher.find()) movie.setImg(matcher.group(1));
        }

        Document doc = Jsoup.parse(html);
        Elements episodesEl = doc.getElementsByClass("wp-playlist-script");
        JSONObject object = new JSONObject(episodesEl.get(0).html());
        JSONArray ea = object.getJSONArray("tracks");
        List<Episode> episodes = new ArrayList<>();
        for (int i = 0; i < ea.length(); i++) {
            Episode e = new Episode();
            e.setName(ea.getJSONObject(i).getString("caption"));
            e.setUrl(ea.getJSONObject(i).getString("src"));
            String caption = ea.getJSONObject(i).getString("subsrc");
            caption = caption.replaceAll("\\\\", "");
            caption = "http://ddrk.oss-cn-shanghai.aliyuncs.com" + caption;
            e.setCaption(caption);
            episodes.add(e);
        }
        Map<String, List<Episode>> map = new LinkedHashMap<String, List<Episode>>();
        map.put(NAME, episodes);
        movie.setEpisodes(map);
        return movie;
    }

    @Override
    public String getPlayURL(Episode ep) throws Exception {
        String url = ep.getUrl();
        url = "http://v3.ddrk.me:9443/video?type=json&id=" + url;
        String html = OkHttpUtil.getInstance().getHtml(url, HOST);
        JSONObject r = new JSONObject(html);
        return r.getString("url");
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public List<Movie> getSinglePage(String url, int page) {
        return null;
    }

    @Override
    public Category search(String str) {
        String url = "https://www.sogou.com/web?query=site:ddrk.me+" + str;
        String html = OkHttpUtil.getInstance().getHtml(url, HOST);
        Category category = new Category();
        category.setTitle(NAME);
        if (html == null) return null;
        List<Movie> movies = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("[href^=/link]");
        for (Element t: elements) {
            if (t.attr("class").contains("img")) continue;
            Movie m = new Movie();
            m.setImg("none");
            m.setSite(type);
            String href = "https://www.sogou.com" + t.attr("href");
            String result = OkHttpUtil.getInstance().getHtml(href, HOST);
            Pattern pt = Pattern.compile("\\(\"([\\s\\S]*?)\"\\)");
            Matcher mt = pt.matcher(result);
            if(mt.find()) m.setUrl(mt.group(1));
            else continue;
            m.setName(t.text());
            movies.add(m);
        }

        category.setUrl(null);
        category.setMovies(movies);
        return category;
    }
}
