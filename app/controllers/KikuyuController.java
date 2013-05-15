package controllers;

import controllers.ws.WSWrapper;
import domain.Page;
import domain.PageComponent;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.ComponentRequestPromiseFactory;
import util.ComposeClientResponseFunction;
import util.ResponseComposer;
import util.UrlMappingsRetriever;

import java.util.List;

public class KikuyuController extends Controller {

    private UrlMappingsRetriever urlMappingsRetriever;
    private WSWrapper wsWrapper;
    private ResponseComposer responseComposer;
    private ComponentRequestPromiseFactory requestPromiseFactory;

    //todo: record each request that is mapped to a url and show in urlmappings number of requests in last month, so can delete mappings that aren't being used
    //todo: copy headers (e.g. cookies) from original request into sub requests and then from responses
    public Result siphon(String path) {
        final Page matchingPage = urlMappingsRetriever.getUrlMatcher().match(path);

        F.Promise<WS.Response>[] promises = getPromisesForUrls(matchingPage.getPageComponents(), request());

        F.Promise<List<WS.Response>> promisesSequence = F.Promise.sequence(promises);
        F.Promise<Result> resultPromise = promisesSequence.map(new ComposeClientResponseFunction(responseComposer, matchingPage));

        return async(resultPromise);
    }

    private F.Promise<WS.Response>[] getPromisesForUrls(List<PageComponent> pageComponents, Http.Request request) {
        F.Promise<WS.Response>[] promises = new F.Promise[pageComponents.size()];
        for (int i = 0; i < pageComponents.size(); i++) {
            PageComponent pageComponent = pageComponents.get(i);

            final String[] urlParts = splitUrl(pageComponent.getUrl());

            final WS.WSRequestHolder urlHolder = wsWrapper.url(urlParts[0]);

            requestPromiseFactory.setRequestParams(urlParts, urlHolder);

            requestPromiseFactory.copyRequestHeaders(urlHolder, request);

            promises[i] = requestPromiseFactory.copyMethodAndBody(pageComponent, urlHolder, request);
        }
        return promises;
    }

    private String[] splitUrl(String componentUrl) {
        return componentUrl.split("[\\?&=]");
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

    public void setRequestPromiseFactory(ComponentRequestPromiseFactory requestPromiseFactory) {
        this.requestPromiseFactory = requestPromiseFactory;
    }
}
