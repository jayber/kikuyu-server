package controllers;

import controllers.ws.WSWrapper;
import domain.ComponentUrl;
import domain.Page;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.ComposeClientResponseFunction;
import util.ResponseComposer;
import util.UrlMappingsRetriever;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class KikuyuController extends Controller {

    private static final String POST = "POST";
    private static final String GET = "GET";
    private UrlMappingsRetriever urlMappingsRetriever;
    private WSWrapper wsWrapper;
    private ResponseComposer responseComposer;

    //todo: record each request that is mapped to a url and show in urlmappings number of requests in last month, so can delete mappings that aren't being used
    //todo: copy headers (e.g. cookies) from original request into sub requests and then from responses
    public Result siphon(String path) {
        final Page matchingPage = urlMappingsRetriever.getUrlMatcher().match(path);

        F.Promise<WS.Response>[] promises = getPromisesForUrls(matchingPage.getComponentUrls(), request());

        F.Promise<List<WS.Response>> promisesSequence = F.Promise.sequence(promises);
        F.Promise<Result> resultPromise = promisesSequence.map(new ComposeClientResponseFunction(responseComposer, matchingPage));

        return async(resultPromise);
    }

    private F.Promise<WS.Response>[] getPromisesForUrls(List<ComponentUrl> componentUrls, Http.Request request) {
        F.Promise<WS.Response>[] promises = new F.Promise[componentUrls.size()];
        for (int i = 0; i < componentUrls.size(); i++) {
            ComponentUrl componentUrl = componentUrls.get(i);

            final String[] urlParts = splitUrl(componentUrl.getUrl());

            final WS.WSRequestHolder urlHolder = wsWrapper.url(urlParts[0]);

            copyQueryParams(urlParts, urlHolder);

            promises[i] = combineIncomingAndOutgoingRequests(componentUrl, urlHolder, request);
        }
        return promises;
    }

    private F.Promise<WS.Response> combineIncomingAndOutgoingRequests(ComponentUrl componentUrl, WS.WSRequestHolder urlHolder, Http.Request request) {
        F.Promise<WS.Response> componentPromise;
        copyHeaders(urlHolder, request);
        if (request.method().equals(POST) && componentUrl.isAcceptPost()) {
            final String postData = getPostData(request.body().asFormUrlEncoded());
            componentPromise = urlHolder.post(postData);
        } else {
            componentPromise = urlHolder.get();
        }
        return componentPromise;
    }

    private void copyHeaders(WS.WSRequestHolder urlHolder, Http.Request request) {
        //todo: copying all header causes problems, so just copying Content-Type and Cookie for now
        String[] headerNames = new String[]{"Content-Type", "Cookie"};
        Map<String, String[]> headers = request.headers();
        for (String key : headerNames) {
            String[] values = headers.get(key);
            if (values != null) {
                for (String value : values) {
                    urlHolder.setHeader(key, value);
                }
            }
        }
    }

    private void copyQueryParams(String[] urlParts, WS.WSRequestHolder urlHolder) {
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
    }

    private String getPostData(Map<String, String[]> map) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String key : map.keySet()) {
            try {
                sb.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(map.get(key)[0], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
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
