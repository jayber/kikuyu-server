package controllers;

import controllers.ws.WSWrapper;
import domain.Page;
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
import util.ResponseComposer;
import util.UrlMappingsRetriever;
import util.UrlMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KikuyuController.class)
public class KikuyuControllerTest {

    private static final String TEST_PATH = "testPath";
    private static final String TEMPLATE_PAGE_HTML = "destination page html";
    private static final String COMPONENT_URL = "component url";
    private static final String TEMPLATE_URL = "templateUrl";
    private static final String COMPONENT_PAGE_HTML = "component page html";
    private static final String COMBINED_RESPONSE = "combined response";

    @Mock
    private WSWrapper wrapper;
    @Mock
    private UrlMappingsRetriever urlMappingsRetriever;

    private KikuyuController kikuyuController = new KikuyuController();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        kikuyuController.setWsWrapper(wrapper);
        kikuyuController.setUrlMappingsRetriever(urlMappingsRetriever);
    }

    @Test
    public void testSiphon() throws Exception {

        final UrlMatcher urlMatcher = mock(UrlMatcher.class);

        final WS.WSRequestHolder templateRequestHolder = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> templateResponsePromise = mock(F.Promise.class);
        final WS.WSRequestHolder componentRequestHolder = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> componentResponsePromise = mock(F.Promise.class);

        final Results.AsyncResult mockResult = mock(Results.AsyncResult.class);
        final KikuyuController.ComposeClientResponseFunction outputFunction =
                mock(KikuyuController.ComposeClientResponseFunction.class);


        when(urlMappingsRetriever.getUrlMatcher()).thenReturn(urlMatcher);
        when(urlMatcher.match(anyString())).thenReturn(new Page(TEMPLATE_URL, COMPONENT_URL));

        when(wrapper.url(TEMPLATE_URL)).thenReturn(templateRequestHolder);
        when(templateRequestHolder.get()).thenReturn(templateResponsePromise);

        when(wrapper.url(COMPONENT_URL)).thenReturn(componentRequestHolder);
        when(componentRequestHolder.get()).thenReturn(componentResponsePromise);

        PowerMockito.whenNew(KikuyuController.ComposeClientResponseFunction.class).withAnyArguments().thenReturn(outputFunction);
        PowerMockito.stub(PowerMockito.method(Results.class, "async")).toReturn(mockResult);

        Result result = kikuyuController.siphon(TEST_PATH);

        verify(wrapper).url(TEMPLATE_URL);
        verify(wrapper).url(COMPONENT_URL);
        verify(templateRequestHolder).get();
        verify(componentRequestHolder).get();
        PowerMockito.verifyNew(KikuyuController.ComposeClientResponseFunction.class).withNoArguments();
    }


    @Test
    public void testOutputPageResponseAsTextToClientFunction() throws Throwable {
        final WS.Response templatePageResponse = mock(WS.Response.class);
        final WS.Response componentPageResponse = mock(WS.Response.class);
        List<WS.Response> responses = new ArrayList<WS.Response>();
        responses.add(templatePageResponse);
        responses.add(componentPageResponse);
        final ResponseComposer responseComposer = mock(ResponseComposer.class);

        when(templatePageResponse.getHeader("Content-Type")).thenReturn("text/html");
        when(templatePageResponse.getBody()).thenReturn(TEMPLATE_PAGE_HTML);
        when(componentPageResponse.getBody()).thenReturn(COMPONENT_PAGE_HTML);
        when(responseComposer.composeBody(TEMPLATE_PAGE_HTML, COMPONENT_PAGE_HTML)).thenReturn(COMBINED_RESPONSE);

        final Results.Status okStatus = mock(Results.Status.class);
        final Results.Status htmlStatus = mock(Results.Status.class);

        PowerMockito.stub(PowerMockito.method(Results.class, "ok", String.class)).toReturn(okStatus);
        when(okStatus.as("text/html")).thenReturn(htmlStatus);

        final KikuyuController.ComposeClientResponseFunction outputPageWSResponse = new KikuyuController.ComposeClientResponseFunction(responseComposer);
        final Result actualResult = outputPageWSResponse.apply(responses);

        assertEquals(htmlStatus, actualResult);
        verify(templatePageResponse).getHeader("Content-Type");
        verify(templatePageResponse).getBody();
        verify(componentPageResponse).getBody();
        verify(responseComposer).composeBody(TEMPLATE_PAGE_HTML, COMPONENT_PAGE_HTML);
        verify(okStatus).as("text/html");
    }


    @Test
    public void testOutputPageResponseNOTAsTextToClientFunction() throws Throwable {
        final WS.Response templatePageResponse = mock(WS.Response.class);
        final WS.Response componentPageResponse = mock(WS.Response.class);
        List<WS.Response> responses = new ArrayList<WS.Response>();
        responses.add(templatePageResponse);
        responses.add(componentPageResponse);

        final Results.Status okStatus = mock(Results.Status.class);
        final Results.Status httpStatus = mock(Results.Status.class);
        final ResponseComposer responseComposer = mock(ResponseComposer.class);

        when(templatePageResponse.getHeader("Content-Type")).thenReturn("binary");
        when(templatePageResponse.getStatus()).thenReturn(200);
        when(templatePageResponse.getBodyAsStream()).thenReturn(new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        });
        PowerMockito.stub(PowerMockito.method(Results.class, "status", int.class, InputStream.class)).toReturn(okStatus);
        when(okStatus.as("binary")).thenReturn(httpStatus);

        final KikuyuController.ComposeClientResponseFunction outputPageWSResponse = new KikuyuController.ComposeClientResponseFunction(responseComposer);
        final Result actualResult = outputPageWSResponse.apply(responses);

        assertEquals(httpStatus, actualResult);
        verify(templatePageResponse).getHeader("Content-Type");
        verify(templatePageResponse).getStatus();
        verify(templatePageResponse).getBodyAsStream();
        verify(okStatus).as("binary");
        verify(componentPageResponse).getUri();
        verifyNoMoreInteractions(componentPageResponse);
    }

}
