package cn.ictgu.job;

import cn.ictgu.bean.response.Video;
import cn.ictgu.tools.JsoupUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯视频信息爬虫
 * Created by Silence on 2017/2/12.
 */
@Component
@Log4j2
@AllArgsConstructor
public class QqCrawler {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String TAG = "QQ";

    private final RedisSourceManager redisSourceManager;

    private static final String HOME_PAGE_PC = "https://v.qq.com/";
    private static final String HOME_PAGE_PHONE_TV = "http://v.qq.com/x/list/tv";
    private static final String HOME_PAGE_PHONE_MOVIE = "http://v.qq.com/x/list/movie";
    private static final String HOME_PAGE_PHONE_CARTOON = "http://v.qq.com/x/list/cartoon";
    private static final String HOME_PAGE_PHONE_RECOMMEND = "http://v.qq.com/x/list/variety";

    /**
     * Baidu
     */
    private static final String HOME_PAGE_BAI_DU = "https://www.baidu.com/s?ie=UTF-8&wd=%E5%8D%9A%E5%AE%A2%E5%9B%AD";

    /**
     * bilibili
     */
    private static final String BILIBILI = "http://www.bilibili.com/index.html";

    /**
     * bilibili 的主页的 key
     */
    private static final String KEY_BILIBILI_HOME = "bilibili.com";


    /**
     * 每隔1小时，爬腾讯视频官网信息
     */
    //@Scheduled(fixedRate = 60 * 60 * 1000)
    public void start() {
        Document pcDocument = JsoupUtils.getDocWithPC(HOME_PAGE_PC);
        Document phoneTVDocument = JsoupUtils.getDocWithPC(HOME_PAGE_PHONE_TV);
        Document phoneMovieDocument = JsoupUtils.getDocWithPC(HOME_PAGE_PHONE_MOVIE);
        Document phoneCartoonDocument = JsoupUtils.getDocWithPC(HOME_PAGE_PHONE_CARTOON);
        Document phoneZongyiDocument = JsoupUtils.getDocWithPC(HOME_PAGE_PHONE_RECOMMEND);
        saveCarouselsToRedis(pcDocument);
        saveRecommendsToRedis(phoneZongyiDocument);
        saveTVsToRedis(phoneTVDocument);
        saveMoviesToRedis(phoneMovieDocument);
        saveCartoonsToRedis(phoneCartoonDocument);
    }

    //@Scheduled(fixedRate = 15000)
    public void startBilibili() {

        Document docBilibili = JsoupUtils.getDocWithPC(BILIBILI);

        String text = docBilibili.text();
//        System.out.println(text);


        Elements bili_douga_Element = docBilibili.select("#bili_douga");

        Elements textElement = docBilibili.select("#bili_douga > div > div > div.zone-title > div > a.name");



//        Elements ulElementArray = docBilibili.select("#ranking_douga > div > ul.rank-list.hot-list");
//
//        if( ulElementArray != null && ulElementArray.size() > 0 ){
//            for ( Element liElement : ulElementArray ) {
////                stringRedisTemplate.opsForValue().set( key, value );
//            }
//        }

    }

    //@Scheduled(fixedRate = 15000)
    public void startBaidu() {

        Document docBaidu = JsoupUtils.getDocWithPC(HOME_PAGE_BAI_DU);

//        String text = docBaidu.text();
//        System.out.println(text);


        Elements su_ElementArray = docBaidu.select("#su");

        String outString = su_ElementArray.get(0).attributes().get("value");

        System.out.println( outString );

    }

//    @Scheduled(fixedRate = 5000)
    public void startBaidu2() {

        Document docBaidu = JsoupUtils.getDocWithPC(HOME_PAGE_BAI_DU);

//        Elements a_ElementArray = docBaidu.select("#con-ar > div:nth-child(1) > div > div > div.opr-recommends-merge-panel.opr-recommends-merge-mbGap");

        //第一个div
//        Element element = a_ElementArray.get(0).children().get(0);

//        String outString = a_ElementArray.get(0).attributes().get("value");



//        System.out.println( outString );


//        System.out.println( element );

        //Element con_ar = docBaidu.select("#con-ar").get(0).child(0);

        Elements con_ar_array = docBaidu.select("#con-ar");


        Element con_ar = con_ar_array.get(0).child(0);



        Element cr_content = con_ar.child(0);

        //cr_content.attribute
//        Element element1 = cr_content.child(1).child(1).child(0);


        Element opr_recommends_merge_content = cr_content.child(1);


        Element opr_recommends_merge_panel__opr_recommends_merge_mbGap = opr_recommends_merge_content.child(1).child(0);

        Element element1 = opr_recommends_merge_panel__opr_recommends_merge_mbGap.child(0);

        int a = 0;

    }

    /**
     * 爬腾讯视频官网-首页轮播信息
     */
    private void saveCarouselsToRedis(Document document) {
        List<Video> carouselVideos = new ArrayList<>();
        Elements carousels = document.select("div.slider_nav a");
        for (Element carousel : carousels) {
            Video video = new Video();
            String title = carousel.select("div.txt").text();
            String image = carousel.attr("data-bgimage");
            String url = carousel.attr("href");
            video.setValue(url);
            video.setTitle(title);
            video.setImage(image);
            carouselVideos.add(video);
        }
        String key = redisSourceManager.VIDEO_PREFIX_HOME_CAROUSEL_KEY + "_" + TAG;
        redisSourceManager.saveVideos(key, carouselVideos);
    }

    /**
     * 爬腾讯PC站-综艺
     */
    private void saveRecommendsToRedis(Document document) {
        String key = redisSourceManager.VIDEO_PREFIX_HOME_RECOMMEND_KEY + "_" + TAG;
        redisSourceManager.saveVideos(key, getVideosFromPhoneDocument(document));
    }

    /**
     * 爬腾讯PC站-电视剧
     */
    private void saveTVsToRedis(Document document) {
        String key = redisSourceManager.VIDEO_PREFIX_HOME_TV_KEY + "_" + TAG;
        redisSourceManager.saveVideos(key, getVideosFromPhoneDocument(document));
    }

    /**
     * 爬腾讯PC站-电影
     */
    private void saveMoviesToRedis(Document document) {
        String key = redisSourceManager.VIDEO_PREFIX_HOME_MOVIE_KEY + "_" + TAG;
        redisSourceManager.saveVideos(key, getVideosFromPhoneDocument(document));
    }

    /**
     * 爬腾讯PC站-动漫
     */
    private void saveCartoonsToRedis(Document document) {
        String key = redisSourceManager.VIDEO_PREFIX_HOME_CARTOON_KEY + "_" + TAG;
        redisSourceManager.saveVideos(key, getVideosFromPhoneDocument(document));
    }

    private List<Video> getVideosFromPhoneDocument(Document document) {
        List<Video> videos = new ArrayList<>();
        Elements elements = document.select("li.list_item a.figure");
        for (Element element : elements) {
            Video video = new Video();
            String url = element.attr("href");
            String title = element.select("img").attr("alt");
            String image = element.select("img").attr("r-lazyload");
            video.setTitle(title);
            video.setImage(image);
            video.setValue(url);
            videos.add(video);
        }
        return videos;
    }

}

//        String key = KEY_BILIBILI_HOME;
//        stringRedisTemplate.opsForValue().set( key, text );
//redisSourceManager.save( KEY_BILIBILI_HOME , text );