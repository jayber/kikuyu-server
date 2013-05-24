package controllers;

import domain.Page;
import domain.PageComponent;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import util.ComponentResponsePromiseFactory;
import util.ComposeClientResponseFunction;
import util.ResponseComposer;
import util.UrlMappingsRetriever;

import java.util.List;

public class KikuyuController extends Controller {

    private UrlMappingsRetriever urlMappingsRetriever;
    private ResponseComposer responseComposer;
    private ComponentResponsePromiseFactory responsePromiseFactory;

    //todo: record each request that is mapped to a url and show in app number of requests in last month, so user can delete mappings that aren't being used
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

            F.Promise<WS.Response> responsePromise = responsePromiseFactory.getResponsePromise(request, pageComponent);

            promises[i] = responsePromise;

        }
        return promises;
    }

    public void setUrlMappingsRetriever(UrlMappingsRetriever urlMappingsRetriever) {
        this.urlMappingsRetriever = urlMappingsRetriever;
    }

    public void setResponseComposer(ResponseComposer responseComposer) {
        this.responseComposer = responseComposer;
    }

    public void setResponsePromiseFactory(ComponentResponsePromiseFactory responsePromiseFactory) {
        this.responsePromiseFactory = responsePromiseFactory;
    }
}
