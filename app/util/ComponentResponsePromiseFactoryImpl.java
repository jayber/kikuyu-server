package util;

import controllers.ws.WSWrapper;
import domain.PageComponent;
import play.libs.F;
import play.libs.WS;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

public class ComponentResponsePromiseFactoryImpl implements ComponentResponsePromiseFactory {

    private WSWrapper wsWrapper;
    private static final String POST = "POST";

    private F.Promise<WS.Response> copyMethodAndBody(PageComponent pageComponent, WS.WSRequestHolder urlHolder, Http.Request request) {
        F.Promise<WS.Response> componentPromise;
        if (request.method().equals(POST) && pageComponent.isAcceptPost()) {
            final String postData = getPostData(request.body().asFormUrlEncoded());
            componentPromise = urlHolder.post(postData);
        } else {
            componentPromise = urlHolder.get();
        }
        return componentPromise;
    }

    private void copyRequestHeaders(WS.WSRequestHolder urlHolder, Http.Request request) {
        //todo: copying all headers causes problems, so just copying Content-Type and Cookie for now
        //this is probably to do with Accept-Encoding causing response to be compressed, breaking something else
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

    private void setRequestParams(UriSplit urlParts, WS.WSRequestHolder urlHolder) {
        for (String[] query : urlParts.getParams()) {
            if (query.length > 1) {
                try {
                    urlHolder.setQueryParameter(query[0], URLDecoder.decode(query[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                urlHolder.setQueryParameter(query[0], null);
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

    private UriSplit splitUrl(String componentUrl) {
        return new UriSplit(componentUrl);
    }

    @Override
    public F.Promise<WS.Response> getResponsePromise(Http.Request request, PageComponent pageComponent) {

        final UriSplit urlParts = splitUrl(pageComponent.getUrl());

        final WS.WSRequestHolder urlHolder = wsWrapper.url(urlParts.getUri());

        this.setRequestParams(urlParts, urlHolder);

        this.copyRequestHeaders(urlHolder, request);

        return this.copyMethodAndBody(pageComponent, urlHolder, request);
    }

    public void setWsWrapper(WSWrapper wsWrapper) {
        this.wsWrapper = wsWrapper;
    }
}
