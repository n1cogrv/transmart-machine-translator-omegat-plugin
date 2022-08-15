package cc.jasonchen.omegatplugin.transmartmachinetranslator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.machinetranslators.BaseTranslate;
import org.omegat.core.machinetranslators.MachineTranslators;
import org.omegat.util.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author MijazzChan
 * @Time 2022-08-14 2:10 PM
 */
public class TranSmartMT extends BaseTranslate {

    protected static final ResourceBundle res = ResourceBundle.getBundle("TranSmartMT", Locale.getDefault());
    protected static final String TRANSMART_URL = "https://transmart.qq.com/api/imt";
    private static final Logger logger = LoggerFactory.getLogger(TranSmartMT.class);
    private static final int CONNECTION_TIMEOUT_SECOND = 3;
    private static final int RETRY_CONNECTION = 2;
    /**
     * User Agents from
     * [User Agents Database](https://developers.whatismybrowser.com/useragents/explore/)
     */
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/E7FBAF",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/E7FBAF",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.114 Safari/537.36 Edg/103.0.1264.62",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 Edg/103.0.1264.71",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 Edg/103.0.1264.77",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.64 Safari/537.36 Edg/101.0.1210.53",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36 Edg/100.0.1185.44",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:102.0) Gecko/20100101 Firefox/102.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:103.0) Gecko/20100101 Firefox/103.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:98.0) Gecko/20100101 Firefox/98.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:102.0) Gecko/20100101 Firefox/102.0"
    };
    private final static String PAYLOAD_FORMAT = "{\"header\":{\"fn\":\"auto_translation\"},\"type\":\"plain\",\"model_category\":\"normal\",\"source\":{\"lang\":\"%s\",\"text_list\":[\"%s\"]},\"target\":{\"lang\":\"zh\"}}";
    private final static int USER_AGENTS_LENGTH = USER_AGENTS.length;
    private final Random rnd = new Random();
    private final Map<String, String> headers = new HashMap<>();
    private final List<String> supportedLangs = Collections.unmodifiableList(Arrays.asList("ar", "en", "zh", "de", "ru", "fr", "ko", "pt", "ja", "es"));

    {
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("authority", "transmart.qq.com");
        headers.put("cache-control", "no-cache");
        headers.put("accept-language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7");
        headers.put("content-type", "application/json");
        headers.put("pragma", "no-cache");
        headers.put("referer", "https://transmart.qq.com/zh-CN/index");
    }

    // Plugin setup
    public static void loadPlugins() {
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
                MachineTranslators.add(new TranSmartMT());
            }

            @Override
            public void onApplicationShutdown() {
                /* empty */
            }
        });
    }

    public static void unloadPlugins() {
        /* empty */
    }

    protected static Optional<String> postRequestHelper(Map<String, String> headers,
                                                        String srcLangCode,
                                                        String targetLangCode,
                                                        String translationPayload) throws IOException {
        // Format POST json data payload
        String jsonPayload = String.format(PAYLOAD_FORMAT, srcLangCode, translationPayload, targetLangCode);

        // Fire POST - [curl command 2 Java](https://curlconverter.com/java)
        HttpURLConnection conn = (HttpURLConnection) new URL(TRANSMART_URL).openConnection();

        try {
            conn.setConnectTimeout(CONNECTION_TIMEOUT_SECOND * 1000);
            conn.setRequestMethod("POST");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(jsonPayload);
            writer.flush();
            writer.close();
            conn.getOutputStream().close();
            // Request Fired.

            StringBuffer responseBuffer = new StringBuffer();
            // Response
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );
                String tmp = null;
                if ((tmp = br.readLine()) != null) {
                    responseBuffer.append(tmp.trim());
                }
            }
            if (responseBuffer.length() == 0) {
                logger.debug("Response Length 0");
                return Optional.empty();
            }

            // Json Parsing and Retrieving.
            String res = responseBuffer.toString();
            logger.debug("response from TranSmart: {}", res);
            JsonObject jsonObject = new Gson().fromJson(res, JsonObject.class);
            if (jsonObject.has("header") &&
                    jsonObject.getAsJsonObject("header").has("ret_code") &&
                    jsonObject.get("header").getAsJsonObject().get("ret_code").getAsString().equalsIgnoreCase("succ") &&
                    jsonObject.has("auto_translation")) {
                return Optional.ofNullable(jsonObject.getAsJsonArray("auto_translation").get(0).getAsString());
            }
        } finally {
            conn.disconnect();
        }
        return Optional.empty();
    }

    @Override
    protected String getPreferenceName() {
        return "allow_transmart_mt_plugin";
    }

    @Override
    protected String translate(Language srcLang, Language targetLang, String untranslatedText) throws Exception {
        if (!supportedLangs.contains(srcLang.getLanguageCode())) {
            logger.info("Unsupported source language for TranSmartMT");
            return null;
        }

        // Get from Cached Translation
        String cachedTranslation = getFromCache(srcLang, targetLang, untranslatedText);
        if (cachedTranslation != null) {
            return cachedTranslation;
        }

        logger.debug("Untranslated Text: {}", untranslatedText);
        // Prepare POST Request Header with random ua string
        headers.put("user-agent", USER_AGENTS[rnd.nextInt(USER_AGENTS_LENGTH)]);
        int fail_count = 0;
        Optional<String> response = postRequestHelper(headers, srcLang.getLanguageCode(), targetLang.getLanguageCode(), untranslatedText);
        for (int i = 0; i < RETRY_CONNECTION && !response.isPresent(); i++) {
            response = postRequestHelper(headers, srcLang.getLanguageCode(), targetLang.getLanguageCode(), untranslatedText);
        }
        // After retry, still not present, fail this translation.
        if (!response.isPresent()) {
            return null;
        }
        // ifPresent, cache it, then return it
        putToCache(srcLang, targetLang, untranslatedText, response.get());
        return response.get().trim();
    }

    @Override
    public String getName() {
        return res.getString("PluginName");
    }
}
