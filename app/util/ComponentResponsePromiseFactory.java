package util;

import domain.PageComponent;
import play.libs.F;
import play.libs.WS;
import play.mvc.Http;

public interface ComponentResponsePromiseFactory {
    F.Promise<WS.Response> getResponsePromise(Http.Request request, PageComponent pageComponent);
}
