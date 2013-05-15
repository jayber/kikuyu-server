package util;

import domain.PageComponent;
import play.libs.F;
import play.libs.WS;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

public class ComponentRequestPromiseFactoryImpl implements ComponentRequestPromiseFactory {

    private static final String POST = "POST";

    public F.Promise<WS.Response> copyMethodAndBody(PageComponent pageComponent, WS.WSRequestHolder urlHolder, Http.Request request) {
        F.Promise<WS.Response> componentPromise;
        if (request.method().equals(POST) && pageComponent.isAcceptPost()) {
            final String postData = getPostData(request.body().asFormUrlEncoded());
            componentPromise = urlHolder.post(postData);
        } else {
            componentPromise = urlHolder.get();
        }
        return componentPromise;
    }

    public void copyRequestHeaders(WS.WSRequestHolder urlHolder, Http.Request request) {
        //todo: copying all headers causes problems, so just copying Content-Type and Cookie for now
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

    public void setRequestParams(String[] urlParts, WS.WSRequestHolder urlHolder) {
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
}
