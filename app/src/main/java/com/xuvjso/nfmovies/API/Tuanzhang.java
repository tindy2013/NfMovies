package com.xuvjso.nfmovies.API;

import android.util.Log;
import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.Utils.AESUtil;
import com.xuvjso.nfmovies.Utils.OkHttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tuanzhang implements ISite {
    private static final String HOST = "https://b.apkgm.top";
    public static final String NAME = "团长资源";
    private static Tuanzhang mInstance;
    private static final Type type = Type.TUANZHANG;

    public static synchronized Tuanzhang getInstance() {
        if (mInstance == null) {
            mInstance = new Tuanzhang();
        }
        return mInstance;
    }

    private Tuanzhang() {

    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Movie getMovieDetail(Movie movie) {
        String html = OkHttpUtil.getInstance().getHtml(movie.getUrl(), HOST);
        if (html == null) return null;
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass("moviedteail_list");
        elements = elements.select("li");
        for (Element t : elements) {
            if (t.text().contains("类型")) movie.setType(t.children().text());
            if (t.text().contains("年份")) movie.setYear(t.children().text());
        }
        Element epEl = doc.getElementsByClass("paly_list_btn").get(0);

        List<Episode> episodes = new ArrayList<>();
        for (Element e: epEl.children()) {
            episodes.add(new Episode(e.text(), e.attr("href")));
        }
        Map<String, List<Episode>> map = new LinkedHashMap<String, List<Episode>>();
        map.put(NAME, episodes);
        movie.setEpisodes(map);
        String de = doc.getElementsByClass("yp_context").text();
        movie.setDescription(de);
        return movie;
    }

    @Override
    public String getPlayURL(Episode ep) throws Exception {
        String url = ep.getUrl();
        String html = OkHttpUtil.getInstance().getHtml(url, HOST);
        Pattern p1 = Pattern.compile("window\\.isplay=true;[\\s\\S]*?=\"(.*?)\"");
        Matcher matcher = p1.matcher(html);
        String str1 = "", str2 = "", iv = "";
        if (matcher.find()) {
            str1 = matcher.group(1);
            Log.i("str1", str1);
        }
        Pattern p2 = Pattern.compile("md5\\.enc\\.Utf8\\.parse\\(\"(.*?)\"\\)");
        matcher = p2.matcher(html);
        if (matcher.find()) {
            str2 = matcher.group(1);
            Log.i("str2", matcher.group());
        } else {
            return null;
        }
        Pattern p3 = Pattern.compile("md5\\.enc\\.Utf8\\.parse\\((\\d+?)\\)");
        matcher = p3.matcher(html);
        if (matcher.find()) {
            iv = matcher.group(1);
            Log.i("iv", matcher.group());
        } else {
            return null;
        }

        String js = new String(AESUtil.decryptData(str1, str2, iv));
        Log.i("TUANZHANG", "PLAY" + js);
        Pattern p = Pattern.compile("url:[\\s\\S]*?[\"\'](.*?)[\"\']");
        Log.i("js", js);
        Matcher m = p.matcher(js);
        if (m.find()) {
            Log.i("matcher", m.group(1));
        }
        return m.group(1);
    }
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Category search(String str) {
        String html = OkHttpUtil.getInstance().getHtml(HOST + "/?s=" + str, HOST);
        if (html == null) return null;
        Category category = new Category();
        category.setTitle("团长资源");
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass("thumb lazy");
        if (elements.size() == 0) return null;
        List<Movie> movies = new ArrayList<>();
        for (Element e: elements) {

            String href = e.parent().attr("href");
            String img = e.attr("data-original");
            String name = e.attr("alt");
            Log.i("TUANZHANG", name + ' ' + img);
            Movie m = new Movie(name, img, href);
            m.setSite(type);
            movies.add(m);
        }
        category.setMovies(movies);
        category.setUrl(null);
        return category;
    }

    @Override
    public List<Category> getCategories() {

        String html = OkHttpUtil.getInstance().getHtml(HOST, HOST);
        if (html == null) return null;

        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass("mi_btcon");
        List<Category> categories = new ArrayList<>();
        for (int i = 1; i < elements.size(); i++) {
            Element element = elements.get(i);
            Element a = element.selectFirst("div>div>h2>a");
            String title = a.text();
            String url = a.attr("href");
            Elements movieElements = element.getElementsByClass("thumb lazy");
            List<Movie> movies = new ArrayList<>();
            for (Element e: movieElements) {
                String href = e.parent().attr("href");
                String img = e.attr("data-original");
                String name = e.attr("alt");
                Log.i("TUANZHANG", name + ' ' + img);
                Movie m = new Movie(name, img, href);
                m.setSite(type);
                movies.add(m);
            }
            categories.add(new Category(title, movies, url));
        }
        return categories;
    }


}
