package controllers;

import controllers.ws.WSWrapper;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import util.UrlMatcher;
import util.UrlMatcherImpl;

public class KikuyuController extends Controller {

    private static final String URL_MAPPINGS = "/urlMappings";

    private String kikuyuLayoutWebserviceAddress;
    private WSWrapper wsWrapper;

    public Result index() {
        return ok("this is it");
    }

    //todo: record each request that is mapped to a url and show in urlmappings number of requests in last month, so can delete mappings that aren't being used
    //todo: move the retrieval of urlMappings into  a periodic task, rather than part of individual requests
    //todo: copy headers (e.g. cookies) from original request into sub requests and then from responses
    public Result siphon(String path) {
        final String wsRequestPath = kikuyuLayoutWebserviceAddress + URL_MAPPINGS;
        final WS.WSRequestHolder retrieveAllUrlMappingsRequestHolder = wsWrapper.url(wsRequestPath);
        final F.Promise<WS.Response> allUrlMappingsResponsePromise = retrieveAllUrlMappingsRequestHolder.get();

        final F.Function<WS.Response, F.Promise<Result>> processLayoutWSResponse = chainFunctionsToProcessWSResponses(path);

        final F.Promise<Result> resultPromise = allUrlMappingsResponsePromise.flatMap(processLayoutWSResponse);

        return async(resultPromise);
    }

    private F.Function<WS.Response, F.Promise<Result>> chainFunctionsToProcessWSResponses(String path) {
        final F.Function<WS.Response, Result> outputPageResponseToClientFunction = new OutputPageResponseToClientFunction();
        return new UrlMappingsToPageRequestPromiseFunction(outputPageResponseToClientFunction, wsWrapper, path);
    }

    public static class OutputPageResponseToClientFunction implements F.Function<WS.Response, Result> {
        @Override
        public Result apply(WS.Response response) throws Throwable {
            Logger.info("destination content from: " + response.getUri());
            final String contentType = response.getHeader("Content-Type");
            Status status = null;
            if (contentType.startsWith("text")) {
                status = ok(response.getBody()).as(contentType);
            } else {
                status = status(response.getStatus(), response.getBodyAsStream()).as(contentType);
            }
            return status;
        }
    }

    public static class UrlMappingsToPageRequestPromiseFunction implements F.Function<WS.Response, F.Promise<Result>> {
        private final F.Function<WS.Response, Result> outputPageResponseToClientFunction;
        private WSWrapper wsWrapper;
        private String path;

        public UrlMappingsToPageRequestPromiseFunction(F.Function<WS.Response, Result> outputPageResponseToClientFunction, WSWrapper wsWrapper, String path) {
            this.outputPageResponseToClientFunction = outputPageResponseToClientFunction;
            this.wsWrapper = wsWrapper;
            this.path = path;
        }

        @Override
        public F.Promise<Result> apply(WS.Response response) throws Throwable {
            final JsonNode body = response.asJson();

            final UrlMatcher urlMatcher = new UrlMatcherImpl(body);
            final String destinationUrl = urlMatcher.match(path).getTemplateUrl();

            final WS.WSRequestHolder url = wsWrapper.url(destinationUrl);
            final F.Promise<WS.Response> responsePromise = url.get();
            final F.Promise<Result> map = responsePromise.map(outputPageResponseToClientFunction);
            return map;
        }
    }

    public void setKikuyuLayoutWebserviceAddress(String kikuyuLayoutWebserviceAddress) {
        this.kikuyuLayoutWebserviceAddress = kikuyuLayoutWebserviceAddress;
    }

    public void setWsWrapper(WSWrapper wsWrapper) {
        this.wsWrapper = wsWrapper;
    }
}