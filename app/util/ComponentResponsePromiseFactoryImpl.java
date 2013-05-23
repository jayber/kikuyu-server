package util;

import controllers.ws.WSWrapper;
import domain.PageComponent;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentResponsePromiseFactoryImpl implements ComponentResponsePromiseFactory {

    private static final Pattern QUERY_STRING_BOUNDARY = Pattern.compile("[\\?]");
    private static final Pattern PARAMS_PATTERN = Pattern.compile("\\{params\\}");

    private WSWrapper wsWrapper;
    private static final String POST = "POST";

    @Override
    public F.Promise<WS.Response> getResponsePromise(Http.Request request, PageComponent pageComponent) {

        final String rQueryString = findQueryString(request.uri());

        final String pageComponentUrl = substituteVars(pageComponent.getUrl(), rQueryString);

        Logger.debug("promising content from: " + pageComponentUrl);
        final WS.WSRequestHolder urlHolder = wsWrapper.url(pageComponentUrl);

        this.copyRequestHeaders(urlHolder, request);

        return this.copyMethodAndBody(pageComponent, urlHolder, request);
    }

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

    private String findQueryString(String componentUrl) {
        if (StringUtils.isNotEmpty(componentUrl)) {
            String[] split = QUERY_STRING_BOUNDARY.split(componentUrl);
            if (split.length == 2) {
                return split[1];
            }
        }
        return "";
    }

    private String substituteVars(String pageComponent, String rQueryString) {
        final Matcher matcher = PARAMS_PATTERN.matcher(pageComponent);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, rQueryString);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public void setWsWrapper(WSWrapper wsWrapper) {
        this.wsWrapper = wsWrapper;
    }
}
