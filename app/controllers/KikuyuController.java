package controllers;

import controllers.ws.WSWrapper;
import domain.Page;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import util.ComposeClientResponseFunction;
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

        F.Promise<WS.Response>[] promises = getPromisesForUrls(match);

        F.Promise<List<WS.Response>> promisesSequence = F.Promise.sequence(promises);
        F.Promise<Result> resultPromise = promisesSequence.map(new ComposeClientResponseFunction(responseComposer));

        return async(resultPromise);
    }

    private F.Promise<WS.Response>[] getPromisesForUrls(Page match) {
        final List<String> componentUrls = match.getComponentUrls();
        F.Promise<WS.Response>[] promises = new F.Promise[componentUrls.size()];
        for (int i = 0; i < componentUrls.size(); i++) {
            String componentUrl = componentUrls.get(i);
            final WS.WSRequestHolder urlHolder = wsWrapper.url(componentUrl);
            final F.Promise<WS.Response> componentPromise = urlHolder.get();
            promises[i] = componentPromise;
        }
        return promises;
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
