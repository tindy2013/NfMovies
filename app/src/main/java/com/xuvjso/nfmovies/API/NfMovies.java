package com.xuvjso.nfmovies.API;

import android.util.Log;
import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.Utils.OkHttpUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NfMovies implements ISite {

    public static final String HOST = "https://www.nfmovies.com";
    public static final String NAME = "奈菲影视";
    private static NfMovies mInstance;
    private static final Type type = Type.NFMOVIES;


    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Movie getMovieDetail(Movie movie) {
        String html = OkHttpUtil.getInstance().getHtml(movie.getUrl(), HOST);
        if (html == null) return null;
        Document doc = Jsoup.parse(html);
        Element de = doc.getElementById("list3");
        movie.setDescription(de.text().trim());
        Elements elements = doc.getElementsByClass("panel clearfix");
        Map<String, List<Episode>> map = new LinkedHashMap<String, List<Episode>>();

        for (Element e: elements) {
            List<Episode> episodes = new ArrayList<>();
            Elements es = e.getElementsByClass("list-15256").select("li>a");
            for (Element et : es) {
                Episode episode = new Episode(et.attr("title"),
                        HOST + et.attr("href"));
                episodes.add(episode);
            }
            String title = e.getElementsByClass("option").first().attr("title");
            if (map.containsKey(title)) {
                for (int i = 1; i < 4; i++) {
                    if (!map.containsKey(title + String.valueOf(i))) map.put(title + String.valueOf(i), episodes);
                    break;
                }
            }
            else map.put(title, episodes);
        }
        movie.setEpisodes(map);
        Elements infos = doc.getElementsByClass("text-muted");
        String y = infos.get(5).parent().childNode(1).toString();
        movie.setYear(y);
        String t = infos.get(3).siblingElements().get(0).text();
        movie.setType(t);
        return movie;
    }

    @Override
    public String getPlayURL(Episode ep) throws MalformedURLException, UnsupportedEncodingException {
        String url = ep.getUrl();
        String html = OkHttpUtil.getInstance().getHtml(url, HOST);
        if (html == null) {
            Log.d("html:", "null");
            return null;
        }
        Log.d("parse", "start");
        Pattern pattern = Pattern.compile("now=unescape\\(\"(.*?)\"\\)");
        Matcher matcher = pattern.matcher(html);
        String shumafen = "";
        if (matcher.find())
            shumafen = String.valueOf(matcher.group(1));

        shumafen = URLDecoder.decode(shumafen, "utf-8");
        Log.d("NFMOVIES", shumafen);
        shumafen = shumafen.replace("http:", "https:");
        if (shumafen.contains("m3u8")) return shumafen;
        if (shumafen.contains("youku.com")) { ;
            String kuyun = OkHttpUtil.getInstance().getHtml(shumafen, HOST);
            Log.d("NFMOVIES", kuyun);
            Pattern kuyunp = Pattern.compile("main[\\s\\S]*?=[\\s\\S]*?\"(.*?)\"");
            Matcher km = kuyunp.matcher(kuyun);
            if (km.find()) {
                String m3u8 = km.group(1);
                Log.d("m3u8", m3u8);
                m3u8 = m3u8.replaceAll("index.*", "1000k/hls/index.m3u8");
                shumafen = shumafen.replaceAll("/share.*", m3u8);
                Log.d("NFMOVIES", shumafen);
                return shumafen;
            } else {
                return null;
            }
        }


        StringBuilder b = new StringBuilder();
        b.append(HOST).append("/api/vproxy.php?url=").append(shumafen).append("&type=json");
        url = b.toString();
        Log.d("url", url);
        html = OkHttpUtil.getInstance().getHtml(url, "https://www.nfmovies.com/js/player/mp4.html?191026");
        Log.d("html", html);
        JSONObject object = null;
        try {
            object = new JSONObject(html);
            url = object.get("data").toString();
            Log.i("NFMOVIES", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (object == null) return null;
        return url;
    }

    @Override
    public List<Movie> getSinglePage(String url, int page) {
        if (url.contains("/list")) return getList(url, page);
        if (url.contains("/search.php")) return getSearch(url, page);

        return null;
    }

    @Override
    public Category search(String str) {
        String html = OkHttpUtil.getInstance().getHtml(HOST + "/search.php?searchword="+ str,
                HOST);
        if (html == null) return null;
        Category category = new Category();
        category.setTitle("奈菲影视");
        List<Movie> movies = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements eles = doc.getElementsByClass("content");
        if (eles.size() == 0) return null;
        for (Element e : eles) {
            Movie m = new Movie();
            m.setSite(Type.NFMOVIES);
            Element pic = e.getElementsByClass("videopic").first();
            m.setUrl(HOST + pic.attr("href"));
            String style = pic.attr("style");
            Pattern p = Pattern.compile("url\\((.*?)\\)");
            Matcher matcher = p.matcher(style);
            if (matcher.find()) m.setImg(HOST + matcher.group(1));
            String name = e.getElementsByClass("head").first().text();
            m.setName(name);
            movies.add(m);
        }
        category.setMovies(movies);
        return category;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private List<Movie> getSearch(String url,  int page) {
        List<Movie> movies = new ArrayList<>();
        String u = url + "&page=" + String.valueOf(page);
        String html = OkHttpUtil.getInstance().getHtml(u, HOST);
        if (html == null) return null;


        return null;
    }

    private List<Movie> getList(String url, int page) {
        return null;
    }

    public static synchronized NfMovies getInstance() {
        if (mInstance == null) {
            mInstance = new NfMovies();
        }
        return mInstance;
    }

    private NfMovies() {

    }

    @Override
    public List<Category> getCategories() {
        String html = OkHttpUtil.getInstance().getHtml(HOST, HOST);
        if (html == null) return null;
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByClass("hy-layout clearfix");
        String[] nf = new String[]{"奈菲独家", "最近热播", "电影", "电视剧", "综艺", "动漫"};
        List<Category> categories = new ArrayList<>();
        for (int i = 1; i < elements.size(); i++) {
            Element element = elements.get(i);
            String title = element.getElementsByClass("margin-0").text();
            String url = element.getElementsByClass("active").select("a").attr("href");
            Log.i("NFMOVIES", title  + ' ' + url);
            Elements movieElements = element.getElementsByClass("videopic lazy");
            List<Movie> movies = new ArrayList<>();
            for (Element e: movieElements) {
                String href = e.attr("href");
                String img = e.attr("data-original");
                String name = e.attr("title");
                Movie m = new Movie(name, HOST + img, HOST + href);
                m.setSite(type);
                movies.add(m);
            }
            categories.add(new Category(title, movies, HOST + url));
        }
        return categories;
    }

}
