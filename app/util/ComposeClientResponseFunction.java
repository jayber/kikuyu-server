package util;

import domain.Page;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.List;

public class ComposeClientResponseFunction implements F.Function<List<WS.Response>, Result> {
    //todo: for some reason the WS.response doesn't list the headers, so you have to guess!
    public static final String[] HEADER_NAMES = new String[]
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
                    "Location",
                    "Vary"
            };

    private ResponseComposer responseComposer;
    private Page page;

    public ComposeClientResponseFunction(ResponseComposer responseComposer, Page matchingPage) {
        this.responseComposer = responseComposer;
        this.page = matchingPage;
    }

    @Override
    public Result apply(List<WS.Response> responses) throws Throwable {
        WS.Response templateResponse = responses.get(0);
        if (Logger.isInfoEnabled()) {
            for (WS.Response response : responses) {
                Logger.info("received response from: " + response.getUri());
            }
        }

        final Http.Response response = Controller.response();

        copyResponseHeaders(templateResponse, response);

        Results.Status status = mergeIncomingResponsesToOutgoingResponse(responses);

        return status;
    }

    private Results.Status mergeIncomingResponsesToOutgoingResponse(List<WS.Response> responses) {
        WS.Response templateResponse = responses.get(0);
        Results.Status status;
        final String templateContentType = templateResponse.getHeader("Content-Type");
        if (templateResponse.getStatus() != 200) {
            status = Results.status(templateResponse.getStatus(), templateResponse.getBody());
        } else {
            //todo: should probably make set of Content-Types that can be processed an application setting
            if (templateContentType.startsWith("text")) {
                String responseBodies[] = new String[responses.size()];
                responseBodies[0] = templateResponse.getBody();
                populateBodies(responseBodies, responses);
                status = Results.ok(responseComposer.composeBody(page, responseBodies)).as(templateContentType);
            } else {
                status = Results.status(templateResponse.getStatus(), templateResponse.getBodyAsStream()).as(templateContentType);
            }
        }
        return status;
    }

    private void copyResponseHeaders(WS.Response incomingResponse, Http.Response outgoingResponse) {
        for (String header : HEADER_NAMES) {
            String headerValue = incomingResponse.getHeader(header);
            if (headerValue != null) {
                outgoingResponse.setHeader(header, headerValue);
            }
        }
    }

    private void populateBodies(String[] responseBodies, List<WS.Response> responses) {
        // starting from 1 is not a mistake!
        for (int i = 1; i < responses.size(); i++) {
            WS.Response componentResponse = responses.get(i);
            responseBodies[i] = componentResponse.getBody();
        }
    }
}
