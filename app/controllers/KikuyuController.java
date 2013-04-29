package controllers;

import controllers.ws.WSWrapper;
import domain.Page;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import util.ResponseComposer;
import util.UrlMappingsRetriever;

import java.util.List;

public class KikuyuController extends Controller {

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

        final String destinationUrl = match.getTemplateUrl();
        final WS.WSRequestHolder url = wsWrapper.url(destinationUrl);
        final F.Promise<WS.Response> templatePromise = url.get();

        final String componentUrl = match.getComponentUrl();
        final WS.WSRequestHolder urlHolder = wsWrapper.url(componentUrl);
        final F.Promise<WS.Response> componentPromise = urlHolder.get();

        F.Promise<List<WS.Response>> promises = F.Promise.sequence(templatePromise, componentPromise);
        F.Promise<Result> resultPromise = promises.map(new ComposeClientResponseFunction(responseComposer));

        return async(resultPromise);
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

    public static class ComposeClientResponseFunction implements F.Function<List<WS.Response>, Result> {
        private ResponseComposer responseComposer;

        public ComposeClientResponseFunction(ResponseComposer responseComposer) {
            this.responseComposer = responseComposer;
        }

        @Override
        public Result apply(List<WS.Response> responses) throws Throwable {
            WS.Response templateResponse = responses.get(0);
            Logger.info("template content from: " + templateResponse.getUri());
            WS.Response componentResponse = responses.get(1);
            Logger.info("component content from: " + componentResponse.getUri());
            final String templateContentType = templateResponse.getHeader("Content-Type");
            Status status;
            if (templateContentType.startsWith("text")) {
                status = ok(responseComposer.composeBody(templateResponse.getBody(), componentResponse.getBody())).as(templateContentType);
            } else {
                status = status(templateResponse.getStatus(), templateResponse.getBodyAsStream()).as(templateContentType);
            }
            return status;
        }
    }
}
