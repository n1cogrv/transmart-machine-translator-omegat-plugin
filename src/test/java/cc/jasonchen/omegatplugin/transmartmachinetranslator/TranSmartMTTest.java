package cc.jasonchen.omegatplugin.transmartmachinetranslator;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Time 2022-08-14 4:47 PM
 * @Author MijazzChan
 */
public class TranSmartMTTest {

    // Test for local development only, this test will fail in CI build process.
    //    @Test
    public void fireConnection() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept","application/json, text/plain, */*");
        headers.put("authority","transmart.qq.com");
        headers.put("cache-control","no-cache");
        headers.put("accept-language","en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7");
        headers.put("content-type","application/json");
        headers.put("pragma","no-cache");
        headers.put("referer","https://transmart.qq.com/zh-CN/index");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.81 Safari/537.36");
        Optional<String> targetText = TranSmartMT.postRequestHelper(headers, "en", "zh", "Hello Tom");
        System.out.println(targetText.orElseThrow(IOException::new));
    }


}
