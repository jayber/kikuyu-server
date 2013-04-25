package controllers;

import controllers.ws.WSWrapper;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;

public class KikuyuController extends Controller {

    private static final String URL_MAPPINGS = "/urlMappings";

    private String kikuyuLayoutWebserviceAddress;
    private WSWrapper wsWrapper;

    public Result siphon(String path) {
        final String wsRequestPath = kikuyuLayoutWebserviceAddress + URL_MAPPINGS;
        final WS.WSRequestHolder retrieveAllUrlMappingsHolder = wsWrapper.url(wsRequestPath);
        final F.Promise<WS.Response> allUrlMappingsResponsePromise = retrieveAllUrlMappingsHolder.get();

        final F.Function<WS.Response, F.Promise<Result>> processLayoutWSResponse = chainFunctionsToProcessWSResponses(wsRequestPath);

        final F.Promise<Result> resultPromise = allUrlMappingsResponsePromise.flatMap(processLayoutWSResponse);

        return async(resultPromise);
    }

    private F.Function<WS.Response, F.Promise<Result>> chainFunctionsToProcessWSResponses(String wsRequestPath) {
        final F.Function<WS.Response, Result> outputPageResponseToClientFunction = new OutputPageResponseToClientFunction();
        return new UrlMappingsToPageRequestPromiseFunction(wsRequestPath, outputPageResponseToClientFunction, wsWrapper);
    }

    public void setKikuyuLayoutWebserviceAddress(String kikuyuLayoutWebserviceAddress) {
        this.kikuyuLayoutWebserviceAddress = kikuyuLayoutWebserviceAddress;
    }

    public void setWsWrapper(WSWrapper wsWrapper) {
        this.wsWrapper = wsWrapper;
    }

    public static class OutputPageResponseToClientFunction implements F.Function<WS.Response, Result> {
        @Override
        public Result apply(WS.Response response) throws Throwable {
            String finalResult = response.getBody();
            Logger.info("returned from WS.get(" + response.getUri() + "):");
            Logger.trace(finalResult);
            final Status status = ok(finalResult).as("text/html");
            return status;
        }
    }

    public static class UrlMappingsToPageRequestPromiseFunction implements F.Function<WS.Response, F.Promise<Result>> {
        private final String wsRequestPath;
        private final F.Function<WS.Response, Result> outputPageResponseToClientFunction;
        private WSWrapper wsWrapper;

        public UrlMappingsToPageRequestPromiseFunction(String wsRequestPath, F.Function<WS.Response, Result> outputPageResponseToClientFunction, WSWrapper wsWrapper) {
            this.wsRequestPath = wsRequestPath;
            this.outputPageResponseToClientFunction = outputPageResponseToClientFunction;
            this.wsWrapper = wsWrapper;
        }

        @Override
        public F.Promise<Result> apply(WS.Response response) throws Throwable {
            final String body = response.getBody();
            Logger.info("returned from WS.get(" + wsRequestPath + "): \n" + body);

            final WS.WSRequestHolder url = wsWrapper.url(body);
            final F.Promise<WS.Response> responsePromise = url.get();
            final F.Promise<Result> map = responsePromise.map(outputPageResponseToClientFunction);
            return map;
        }
    }
}
