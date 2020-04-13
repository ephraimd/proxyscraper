/* RAF is a Wrapper Framework to enhance working with Automation Projects.
 THis source was extracted from the RAF project
 */
package core;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * MNav is a simple convenience wrapper that helps manage Chrome window. MNav is
 * an alias for Navigation, so once a Chrome window is up, this class help you
 * navigate the web with more control and ease. THis class eliminates a lot of
 * for loop and error checking in your codes and provides a neat OO interface
 * for use with selenium. methods like getElement, getElements, getElementTrying
 * and getPageSoup will prove quite useful when you need to quickly open up a
 * page and find certain selectors easily.
 *
 * MNav is fully integrated with Jsoup so you get the choice to easily load up
 * pages using selenium and Jsoup without writing excess codes
 *
 * MNav stores its MWindow object persistently, so everything that happens in
 * MNav either loading new pages or new tabs (except windowless Jsoup
 * operations) are all happening on its saved MWindow object, the MWindow object
 * can only be passed out by reference and its never cloned
 *
 * @author Ephraim Adedamola
 */
public final class MNav {

    private static final String CHROME_USER_AGENT_ID = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0";
    private static String JSOUP_REFERRER = "https://www.google.com";

    public MNav() {

    }
/* TODO: use HttpClient here
    public Document getPageSoup() {
        return Jsoup.parse(window().driver().getPageSource());
    }
*/
    public static Document getSoup(String url, int retryTimes, MOperation retryOperation) {
        return getSoup(null, url, retryTimes, retryOperation);
    }

    /**
     * Turns a page to Jsoup Document
     *
     * @param proxy
     * @param url
     * @param retryTimes
     * @param retryOperation
     * @return
     */
    public static Document getSoup(String proxy, String url, int retryTimes, MOperation retryOperation) {
        boolean pageOpened = false;
        Document doc = null;
        for (int i = 0; i < retryTimes; i++) {
            try {
                Connection con = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .userAgent(CHROME_USER_AGENT_ID)
                        .referrer(JSOUP_REFERRER)
                        .timeout(30000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        //.header("Accept-Encoding", "gzip, deflate, br")
                        .followRedirects(true);
                if (proxy != null && !proxy.isEmpty()) {
                    String[] p = proxy.split(":");
                    //Proxy proxy_ = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(p[0], Integer.parseInt(p[1])));
                    con.proxy(p[0], Integer.parseInt(p[1]));
                }
                doc = con.get(); //30 secs
                pageOpened = true;
                retryOperation.exec(doc, null, url);
                break;
            } catch (IOException ex) {
                //ex.printStackTrace();
                retryOperation.pageNotAvailable(url);
                retryOperation.retry(url);
            }
        }
        if (!pageOpened || doc == null) {
            return null;
        }
        return doc;
    }

    /**
     *
     * <pre>Elements elems = MNav.getSoupElement(url, "#make-icons .col-md-3 a", 10, new MOperation() {
     * @Override
     * public void pageNotAvailable(MWindow window, String url) {
     * bot.logError("Retrying to open faulty page");
     * }
     *
     * @Override
     * public void exec(MWindow window, String query, String url) {
     * bot.log("Page Loaded..");
     * }
     * });
     * if (elems == null) { //failed to open the page for some reason even after 10 retries?
     * bot.logError("Failed to Open page...");
     * }</pre>
     *
     * @param url String The target url
     * @param selectQuery String The css selector to extract from the page
     * @param retryTimes int Number of times to retry loading the page if it
     * encounters error
     * @param retryOperation MOperation THe MOperation object to handle page
     * loading states
     * @return Elements This is of the Jsoup library
     */
    public static Elements getSoupElement(String url, String selectQuery, int retryTimes, MOperation retryOperation) {
        return getSoupElement(null, url, selectQuery, retryTimes, retryOperation);
    }

    /**
     *
     * <pre>Elements elems = MNav.getSoupElement(proxy, url, "#make-icons .col-md-3 a", 10, new MOperation() {
     * @Override
     * public void pageNotAvailable(String url) {
     * bot.logError("Retrying to open faulty page");
     * }
     *
     * @Override
     * public void exec(Document window, String query, String url) {
     * bot.log("Page Loaded..");
     * }
     * });
     * if (elems == null) { //failed to open the page for some reason even after 10 retries?
     * bot.logError("Failed to Open page...");
     * }</pre>
     *
     * @param proxy String The proxy to use when loading the target url
     * @param url String The target url
     * @param selectQuery String The css selector to extract from the page
     * @param retryTimes int Number of times to retry loading the page if it
     * encounters error
     * @param retryOperation MOperation THe MOperation object to handle page
     * loading states
     * @return Elements This is of the Jsoup library
     */
    public static Elements getSoupElement(String proxy, String url, String selectQuery, int retryTimes, MOperation retryOperation) {
        Document doc = getSoup(proxy, url, retryTimes, retryOperation);
        return doc == null ? null : doc.select(selectQuery);
    }

    public static ArrayList<Elements> getSoupElements(String url, List<String> selectQueries, int retryTimes, MOperation retryOperation) {
        return getSoupElements(null, url, selectQueries, retryTimes, retryOperation);
    }

    /**
     * <pre>
     * List<Elements> elems = MNav.getSoupElements(proxy, url, Arrays.asList(".product-main", ".product-main .summary-row", ".item"), 10, new MOperation() {
     * @Override
     * public void pageNotAvailable(String url) {
     * bot.logError("Retrying to open faulty page");
     * }
     * @Override public void elementNotFound(String query) {
     * bot.logError("Element not found: %s", query); }
     * @Override public void exec(Document window, String query, String url) {
     * bot.log("Page Loaded.."); } }); if (elems == null) { bot.logError("Failed
     * to Open Page"); return; }
     * </pre>
     *
     * @param proxy proxy to use
     * @param url The url to work with
     * @param selectQueries The List of queries to use to extract the needed elements on the page
     * @param retryTimes Number of times to retry till quitting
     * @param retryOperation The operation feedback handler
     * @return List of extracted elements, queries that yield invalid elements will have null in their place
     */
    public static ArrayList<Elements> getSoupElements(String proxy, String url, List<String> selectQueries, int retryTimes, MOperation retryOperation) {
        Document doc = getSoup(proxy, url, retryTimes, retryOperation);
        if (doc == null) {
            return null;
        }
        ArrayList<Elements> resp = new ArrayList<>();
        try {
            selectQueries.forEach((query) -> {
                try {
                    resp.add(doc.select(query));
                } catch (Exception ex) {
                    resp.add(null); //check each index for null values
                    retryOperation.elementNotFound(query);
                }
            });
            return resp;
        } catch (Exception ex) {
            //MLog.instance().error(ex);
            return null;
        }
    }
}
