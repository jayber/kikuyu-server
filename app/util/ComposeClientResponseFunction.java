package util;

import domain.Page;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.util.List;

public class ComposeClientResponseFunction implements F.Function<List<WS.Response>, Result> {
    private ResponseComposer responseComposer;
    private Page page;

    public ComposeClientResponseFunction(ResponseComposer responseComposer, Page matchingPage) {
        this.responseComposer = responseComposer;
        this.page = matchingPage;
    }

    @Override
    public Result apply(List<WS.Response> responses) throws Throwable {
        WS.Response templateResponse = responses.get(0);
        Logger.info("template content from: " + templateResponse.getUri());

        //todo: for some reason the WS.response doesn't list the headers, so you have to guess!
        String[] headers = new String[]
                {
                        "Content-Type",
                        "Cache-Control",
                        "Connection",
                        "Content-Encoding",
                        "Content-Type",
                        "Date",
                        "Expires",
                        "Keep-Alive",
                        "Set-Cookie",
                        "Vary"
                };

        for (String header : headers) {
            String headerValue = templateResponse.getHeader(header);
            if (headerValue != null) {
                Controller.response().setHeader(header, headerValue);
            }
        }

        final String templateContentType = templateResponse.getHeader("Content-Type");
        Results.Status status;
        //todo: should probably make set of Content-Types that can be processed an application setting
        if (templateContentType.startsWith("text")) {
            String responseBodies[] = new String[responses.size()];
            responseBodies[0] = templateResponse.getBody();
            populateBodies(responseBodies, responses);
            status = Results.ok(responseComposer.composeBody(page, responseBodies)).as(templateContentType);
        } else {
            status = Results.status(templateResponse.getStatus(), templateResponse.getBodyAsStream()).as(templateContentType);
        }
        return status;
    }

    private void populateBodies(String[] responseBodies, List<WS.Response> responses) {
        // starting from 1 is not a mistake!
        for (int i = 1; i < responses.size(); i++) {
            WS.Response componentResponse = responses.get(i);
            Logger.info("component content from: " + componentResponse.getUri());
            responseBodies[i] = componentResponse.getBody();
        }
    }
}
