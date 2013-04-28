package controllers;

import controllers.ws.WSWrapper;
import domain.Page;
import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.libs.F;
import play.libs.WS;
import play.mvc.Result;
import play.mvc.Results;
import util.UrlMatcherImpl;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KikuyuController.class)
public class KikuyuControllerTest {

    private static final String TEST_ADDRESS = "testAddress";
    private static final String TEST_PATH = "testPath";
    private static final String DESTINATION_PAGE_URL = "destination page URL";
    private static final String DESTINATION_PAGE_HTML = "destination page html";

    @Mock
    private WSWrapper wrapper;

    private KikuyuController kikuyuController = new KikuyuController();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        kikuyuController.setKikuyuLayoutWebserviceAddress(TEST_ADDRESS);
        kikuyuController.setWsWrapper(wrapper);
    }

    @Test
    public void testSiphon() throws Exception {
        final WS.WSRequestHolder requestHolder = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> responsePromise = mock(F.Promise.class);
        final Results.AsyncResult mockResult = mock(Results.AsyncResult.class);
        final KikuyuController.OutputPageResponseToClientFunction outputFunction =
                mock(KikuyuController.OutputPageResponseToClientFunction.class);
        final KikuyuController.UrlMappingsToPageRequestPromiseFunction urlToPageRequestFunction =
                mock(KikuyuController.UrlMappingsToPageRequestPromiseFunction.class);


        when(wrapper.url(anyString())).thenReturn(requestHolder);
        when(requestHolder.get()).thenReturn(responsePromise);
        PowerMockito.whenNew(KikuyuController.OutputPageResponseToClientFunction.class).withNoArguments().thenReturn(outputFunction);
        PowerMockito.whenNew(KikuyuController.UrlMappingsToPageRequestPromiseFunction.class).withArguments(outputFunction, wrapper, TEST_PATH).thenReturn(urlToPageRequestFunction);
        PowerMockito.stub(PowerMockito.method(Results.class, "async")).toReturn(mockResult);

        Result result = kikuyuController.siphon(TEST_PATH);

        verify(wrapper).url(TEST_ADDRESS + "/urlMappings");
        verify(requestHolder).get();
        PowerMockito.verifyNew(KikuyuController.OutputPageResponseToClientFunction.class).withNoArguments();
        PowerMockito.verifyNew(KikuyuController.UrlMappingsToPageRequestPromiseFunction.class).withArguments(outputFunction, wrapper, TEST_PATH);
    }

    @Test
    public void testUrlMappingsToPageRequestPromiseFunction() throws Throwable {
        final WS.Response urlMappingsResponse = mock(WS.Response.class);
        final JsonNode urlMappings = mock(JsonNode.class);
        final UrlMatcherImpl matcher = mock(UrlMatcherImpl.class);
        final WS.WSRequestHolder destinationPageRequestHolder = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> destinationPageResponsePromise = mock(F.Promise.class);
        final F.Function<WS.Response, Result> destinationPageOutputFunction = mock(F.Function.class);
        final F.Promise<Result> outputToClientPromise = mock(F.Promise.class);

        when(urlMappingsResponse.asJson()).thenReturn(urlMappings);
        PowerMockito.whenNew(UrlMatcherImpl.class).withAnyArguments().thenReturn(matcher);
        when(matcher.match(TEST_PATH)).thenReturn(new Page(DESTINATION_PAGE_URL, ""));
        when(wrapper.url(DESTINATION_PAGE_URL)).thenReturn(destinationPageRequestHolder);
        when(destinationPageRequestHolder.get()).thenReturn(destinationPageResponsePromise);
        when(destinationPageResponsePromise.map(destinationPageOutputFunction)).thenReturn(outputToClientPromise);

        final KikuyuController.UrlMappingsToPageRequestPromiseFunction urlMappingsToPageRequestPromiseFunction =
                new KikuyuController.UrlMappingsToPageRequestPromiseFunction(destinationPageOutputFunction, wrapper, TEST_PATH);

        final F.Promise<Result> actualOutputToClientPromise = urlMappingsToPageRequestPromiseFunction.apply(urlMappingsResponse);
        assertEquals(outputToClientPromise, actualOutputToClientPromise);

        verify(urlMappingsResponse).asJson();
        verify(matcher).match(TEST_PATH);
        verify(wrapper).url(DESTINATION_PAGE_URL);
        verify(destinationPageRequestHolder).get();
        verify(destinationPageResponsePromise).map(destinationPageOutputFunction);
    }

    @Test
    public void testOutputPageResponseAsTextToClientFunction() throws Throwable {
        final WS.Response destinationPageResponse = mock(WS.Response.class);
        final Results.Status okStatus = mock(Results.Status.class);
        final Results.Status htmlStatus = mock(Results.Status.class);

        when(destinationPageResponse.getHeader("Content-Type")).thenReturn("text/html");
        when(destinationPageResponse.getBody()).thenReturn(DESTINATION_PAGE_HTML);

        PowerMockito.stub(PowerMockito.method(Results.class, "ok", String.class)).toReturn(okStatus);
        when(okStatus.as("text/html")).thenReturn(htmlStatus);

        final KikuyuController.OutputPageResponseToClientFunction outputPageWSResponse = new KikuyuController.OutputPageResponseToClientFunction();
        final Result actualResult = outputPageWSResponse.apply(destinationPageResponse);

        assertEquals(htmlStatus, actualResult);
        verify(destinationPageResponse).getHeader("Content-Type");
        verify(destinationPageResponse).getBody();
        verify(okStatus).as("text/html");
    }

    @Test
    public void testOutputPageResponseNOTAsTextToClientFunction() throws Throwable {
        final WS.Response destinationPageResponse = mock(WS.Response.class);
        final Results.Status okStatus = mock(Results.Status.class);
        final Results.Status httpStatus = mock(Results.Status.class);

        when(destinationPageResponse.getHeader("Content-Type")).thenReturn("binary");
        when(destinationPageResponse.getStatus()).thenReturn(200);
        when(destinationPageResponse.getBodyAsStream()).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });
        PowerMockito.stub(PowerMockito.method(Results.class, "status", int.class, InputStream.class)).toReturn(okStatus);
        when(okStatus.as("binary")).thenReturn(httpStatus);

        final KikuyuController.OutputPageResponseToClientFunction outputPageWSResponse = new KikuyuController.OutputPageResponseToClientFunction();
        final Result actualResult = outputPageWSResponse.apply(destinationPageResponse);

        assertEquals(httpStatus, actualResult);
        verify(destinationPageResponse).getHeader("Content-Type");
        verify(destinationPageResponse).getStatus();
        verify(destinationPageResponse).getBodyAsStream();
        verify(okStatus).as("binary");
    }
}
