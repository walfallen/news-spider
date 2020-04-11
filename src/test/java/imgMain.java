import com.yc.spider.util.ImgUtils;

/**
 * Created by youmingwei on 17/3/10.
 */
public class imgMain {
    public static void main(String... args) throws Exception {
        String s = "/Users/youmingwei/Desktop/33.png";
        String s1 = "/Users/youmingwei/Desktop/3.png";
        boolean res = ImgUtils.zoomImage(s, s1, 128, null, "png", null);
        System.out.print(res);
    }
}
