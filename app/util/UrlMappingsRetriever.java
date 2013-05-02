package util;

import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.client.RestTemplate;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class UrlMappingsRetriever implements InitializingBean {
    private static final String URL_MAPPINGS = "/urlMappings";

    private String kikuyuLayoutWebserviceAddress;
    private UrlMatcher urlMatcher;
    private int reloadUrlMappingsFreqSecs = 10;

    public void afterPropertiesSet() {

        final String wsRequestPath = kikuyuLayoutWebserviceAddress + URL_MAPPINGS;

        loadUrlMappings(wsRequestPath);

        Akka.system().scheduler().schedule(Duration.Zero(), Duration.create(reloadUrlMappingsFreqSecs, TimeUnit.SECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        loadUrlMappings(wsRequestPath);
                    }
                }, Akka.system().dispatcher());
    }

    //default visibility for test access
    void loadUrlMappings(String wsRequestPath) {
        Logger.debug("Loading urlMappings from: " + wsRequestPath);
        RestTemplate restTemplate = new RestTemplate();
        JsonNode jsonNode = restTemplate.getForObject(wsRequestPath, JsonNode.class);
        urlMatcher = new UrlMatcherImpl(jsonNode);
    }

    public UrlMatcher getUrlMatcher() {
        return urlMatcher;
    }

    public void setKikuyuLayoutWebserviceAddress(String kikuyuLayoutWebserviceAddress) {
        this.kikuyuLayoutWebserviceAddress = kikuyuLayoutWebserviceAddress;
    }

    public void setReloadUrlMappingsFreqSecs(int reloadUrlMappingsFreqSecs) {
        this.reloadUrlMappingsFreqSecs = reloadUrlMappingsFreqSecs;
    }
}
