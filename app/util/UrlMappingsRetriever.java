package util;

import controllers.ws.WSWrapper;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class UrlMappingsRetriever implements InitializingBean {
    private static final String URL_MAPPINGS = "/urlMappings";

    private WSWrapper wsWrapper;
    private String kikuyuLayoutWebserviceAddress;
    private UrlMatcher urlMatcher;
    private int reloadUrlMappingsFreqMins = 1;

    public void afterPropertiesSet() {

        final String wsRequestPath = kikuyuLayoutWebserviceAddress + URL_MAPPINGS;

        loadUrlMappings(wsRequestPath);

        Akka.system().scheduler().schedule(Duration.Zero(), Duration.create(reloadUrlMappingsFreqMins, TimeUnit.MINUTES), new Runnable() {
            @Override
            public void run() {
                loadUrlMappings(wsRequestPath);
            }
        }, Akka.system().dispatcher());

    }

    private void loadUrlMappings(String wsRequestPath) {
        Logger.info("Loading urlMappings.");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> entity = restTemplate.getForEntity(wsRequestPath, JsonNode.class);
        JsonNode jsonNode = entity.getBody();
        urlMatcher = new UrlMatcherImpl(jsonNode);
    }

    public UrlMatcher getUrlMatcher() {
        return urlMatcher;
    }

    public void setKikuyuLayoutWebserviceAddress(String kikuyuLayoutWebserviceAddress) {
        this.kikuyuLayoutWebserviceAddress = kikuyuLayoutWebserviceAddress;
    }

    public void setWsWrapper(WSWrapper wsWrapper) {
        this.wsWrapper = wsWrapper;
    }

    public void setReloadUrlMappingsFreqMins(int reloadUrlMappingsFreqMins) {
        this.reloadUrlMappingsFreqMins = reloadUrlMappingsFreqMins;
    }
}
