/* RAF is a Wrapper Framework to enhance working with Automation Projects.
 THis source was extracted from the RAF project
 */
package core;
import org.jsoup.nodes.Document;
/**
 *
 * @author Ephrahim Adedamola 
 */
public interface MOperation {
    default void pageLoadingTimeout(String pageUrl){}
    default void pageNotAvailable(String pageUrl){}
    default void elementInvalid(String elementStr){}
    default void elementNotFound(String elementStr){}
    default void elementValid(String elementStr){}
    default void retry(String pageUrl){}
    default void exec(Document window, String elementStr, String pageUrl){}//for other usages
}
