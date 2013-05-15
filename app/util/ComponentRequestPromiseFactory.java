package util;

import domain.PageComponent;
import play.libs.F;
import play.libs.WS;
import play.mvc.Http;

public interface ComponentRequestPromiseFactory {
    void setRequestParams(String[] urlParts, WS.WSRequestHolder urlHolder);

    void copyRequestHeaders(WS.WSRequestHolder urlHolder, Http.Request request);

    F.Promise<WS.Response> copyMethodAndBody(PageComponent pageComponent, WS.WSRequestHolder urlHolder, Http.Request request);
}
