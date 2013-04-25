package controllers;

import controllers.ws.WSWrapper;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;

public class KikuyuController extends Controller {

    private String kikuyuLayoutWebserviceAddress;
    private WSWrapper wsWrapper;

    public Result siphon(String path) {
        final String wsRequestPath = kikuyuLayoutWebserviceAddress + "urlMapping/" + path + "/page/url";

        final F.Function<WS.Response, Result> processDestinationURLResult = getProcessDestinationURLResultFunction();

        final F.Function<WS.Response, F.Promise<Result>> processLayoutWSResponse = getProcessLayoutWSResponseFunction(wsRequestPath, processDestinationURLResult);

        final F.Promise<Result> resultPromise = WS.url(wsRequestPath).get().flatMap(processLayoutWSResponse);

        return async(resultPromise);
    }

    private F.Function<WS.Response, Result> getProcessDestinationURLResultFunction() {
        return new F.Function<WS.Response, Result>() {
            @Override
            public Result apply(WS.Response response) throws Throwable {
                String finalResult = response.getBody();
                Logger.info("returned from WS.get(" + response.getUri() + "):");
                Logger.trace(finalResult);
                return ok(finalResult).as("text/html");
            }
        };
    }

    private F.Function<WS.Response, F.Promise<Result>> getProcessLayoutWSResponseFunction(final String wsRequestPath, final F.Function<WS.Response, Result> processDestinationURLResult) {
        return new F.Function<WS.Response, F.Promise<Result>>() {
            @Override
            public F.Promise<Result> apply(WS.Response response) throws Throwable {
                final String body = response.getBody();
                Logger.info("returned from WS.get(" + wsRequestPath + "): \n" + body);

                F.Promise<Result> map = WS.url(body).get().map(processDestinationURLResult);
                return map;
            }
        };
    }

    public void setKikuyuLayoutWebserviceAddress(String kikuyuLayoutWebserviceAddress) {
        this.kikuyuLayoutWebserviceAddress = kikuyuLayoutWebserviceAddress;
    }
}
