
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * Created by youmingwei on 17/2/16.
 */
public class Test {

    public static void main(String... args) throws Exception {

        String listUrl = "http://iee.zucc.edu.cn/col/col749/index.html";

//        Document document = Jsoup.connect(listUrl)
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
//                .get();
//        Document document = getDocument(listUrl);
//        System.out.println(document);

//        Elements contentE = document.select("#Form1 > table:nth-child(5) > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(3) > td > table > tbody > tr");
//        System.out.println(contentE);

        printException(listUrl);
    }

    public static Document getDocument(String url) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);//当JS执行出错的时候是否抛出异常, 这里选择不需要
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);//是否启用CSS, 因为不需要展现页面, 所以不需要启用
        webClient.getOptions().setJavaScriptEnabled(true); //很重要，启用JS
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX

        HtmlPage page = null;

        try {
            page = webClient.getPage(url);//尝试加载上面图片例子给出的网页
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            webClient.close();
        }

        webClient.waitForBackgroundJavaScript(5000);//异步JS执行需要耗时,所以这里线程要阻塞30秒,等待异步JS执行结束
        String pageXml = page.asXml();//直接将加载完成的页面转换成xml格式的字符串

        return Jsoup.parse(pageXml);
    }

    public static void printException(String url) {
        try {
            Document document = Jsoup.connect(url).get();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}


