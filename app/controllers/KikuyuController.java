package controllers;

import controllers.ws.WSWrapper;
import domain.ComponentUrl;
import domain.Page;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import util.ComposeClientResponseFunction;
import util.ResponseComposer;
import util.UrlMappingsRetriever;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

public class KikuyuController extends Controller {

    private static final String POST = "POST";
    private static final String GET = "GET";
    private UrlMappingsRetriever urlMappingsRetriever;
    private WSWrapper wsWrapper;
    private ResponseComposer responseComposer;

    public Result index() {
        return ok("this is it");
    }

    //todo: record each request that is mapped to a url and show in urlmappings number of requests in last month, so can delete mappings that aren't being used
    //todo: copy headers (e.g. cookies) from original request into sub requests and then from responses
    public Result siphon(String path) {
        final Page match = urlMappingsRetriever.getUrlMatcher().match(path);

        F.Promise<WS.Response>[] promises = getPromisesForUrls(match);

        F.Promise<List<WS.Response>> promisesSequence = F.Promise.sequence(promises);
        F.Promise<Result> resultPromise = promisesSequence.map(new ComposeClientResponseFunction(responseComposer));

        return async(resultPromise);
    }

    private F.Promise<WS.Response>[] getPromisesForUrls(Page match) {
        final List<ComponentUrl> componentUrls = match.getComponentUrls();
        F.Promise<WS.Response>[] promises = new F.Promise[componentUrls.size()];
        for (int i = 0; i < componentUrls.size(); i++) {
            ComponentUrl componentUrl = componentUrls.get(i);
            final String[] urlParts = splitUrl(componentUrl.getUrl());
            final WS.WSRequestHolder urlHolder = wsWrapper.url(urlParts[0]);
            for (int j = 1; j < urlParts.length; j++) {
                String name = urlParts[j];
                if (urlParts.length > j + 1) {
                    try {
                        urlHolder.setQueryParameter(name, URLDecoder.decode(urlParts[++j], "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    urlHolder.setQueryParameter(name, "");
                }
            }
            final F.Promise<WS.Response> componentPromise;
            if (request().method().equals(POST) && componentUrl.isAcceptPost()) {
                componentPromise = urlHolder.post(getPostData(request().body().asFormUrlEncoded()));
            } else {
                componentPromise = urlHolder.execute(GET);
            }
            promises[i] = componentPromise;
        }
        return promises;
    }

    private String getPostData(Map<String, String[]> map) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String key : map.keySet()) {
            sb.append(key).append("=").append(map.get(key)[0]);
            if (map.size() - 1 > i++) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    private String[] splitUrl(String componentUrl) {
        return componentUrl.split("[\\?&=]");
    }

    public void setWsWrapper(WSWrapper wsWrapper) {
        this.wsWrapper = wsWrapper;
    }

    public void setUrlMappingsRetriever(UrlMappingsRetriever urlMappingsRetriever) {
        this.urlMappingsRetriever = urlMappingsRetriever;
    }

    public void setResponseComposer(ResponseComposer responseComposer) {
        this.responseComposer = responseComposer;
    }

}
