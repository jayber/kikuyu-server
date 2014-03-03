package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.client.RestTemplate;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class UrlMappingsRetriever implements InitializingBean {
    private static final String URL_MAPPINGS = "/urlMappings";

    private String kikuyuLayoutWebserviceAddress;
    private UrlMatcher urlMatcher;
    private int reloadUrlMappingsFreqSecs = 60;

    public void afterPropertiesSet() {
        final String configFile = System.getProperty("configFile");

        final String wsRequestPath = kikuyuLayoutWebserviceAddress + URL_MAPPINGS;

        Akka.system().scheduler().schedule(Duration.Zero(), Duration.create(reloadUrlMappingsFreqSecs, TimeUnit.SECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        if (configFile == null) {
                            loadUrlMappingsFromWS(wsRequestPath);
                        } else {
                            loadUrlMappingsFromFile(configFile);

                        }
                    }
                }, Akka.system().dispatcher());
    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    void loadUrlMappingsFromFile(String configFile) {
        try {
            Logger.debug("Loading urlMappings from: " + configFile);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(readFile(configFile, StandardCharsets.UTF_8));
            urlMatcher = new UrlMatcherImpl(jsonNode);
        } catch (Throwable e) {
            throw new RuntimeException("Error loading mappings from file: " + configFile, e);
        }
    }

    //default visibility for test access
    void loadUrlMappingsFromWS(String wsRequestPath) {
        try {
            Logger.debug("Loading urlMappings from: " + wsRequestPath);
            RestTemplate restTemplate = new RestTemplate();
            JsonNode jsonNode = restTemplate.getForObject(wsRequestPath, JsonNode.class);
            urlMatcher = new UrlMatcherImpl(jsonNode);
        } catch (Throwable e) {
            throw new RuntimeException("Error loading mappings from address: " + wsRequestPath, e);
        }
    }

    public UrlMatcher getUrlMatcher() {
        int count = 0;
        while (urlMatcher == null && count++ < 5) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (urlMatcher == null) {
            throw new RuntimeException("no urlMatcher has been loaded - check log for 'Error loading mappings'");
        }

        return urlMatcher;
    }

    public void setKikuyuLayoutWebserviceAddress(String kikuyuLayoutWebserviceAddress) {
        this.kikuyuLayoutWebserviceAddress = kikuyuLayoutWebserviceAddress;
    }

    public void setReloadUrlMappingsFreqSecs(int reloadUrlMappingsFreqSecs) {
        this.reloadUrlMappingsFreqSecs = reloadUrlMappingsFreqSecs;
    }
}
