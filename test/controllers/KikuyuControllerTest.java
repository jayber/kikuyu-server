package controllers;

import controllers.ws.WSWrapper;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KikuyuController.class)
public class KikuyuControllerTest {

    private static final String TEST_ADDRESS = "testAddress";
    private static final String TEST_PATH = "testPath";
    private static final String DESTINATION_PAGE_HTML = "destination page html";

    @Mock
    private WSWrapper wrapper;
    @Mock
    private WS.WSRequestHolder requestHolder;
    @Mock
    private F.Promise responsePromise;
    @Mock
    private Results.AsyncResult mockResult;
    @Mock
    private F.Function<WS.Response, Result> responseResultFunction;
    @Mock
    private WS.Response response;
    @Mock
    private F.Promise<Result> resultPromise;
    @Mock
    private Results.Status status;
    @Mock
    private JsonNode urlMappingsResult;

    private KikuyuController kikuyuController = new KikuyuController();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        kikuyuController.setKikuyuLayoutWebserviceAddress(TEST_ADDRESS);
        kikuyuController.setWsWrapper(wrapper);

        when(wrapper.url(anyString())).thenReturn(requestHolder);
        when(requestHolder.get()).thenReturn(responsePromise);
    }

    @Test
    public void testSiphon() throws Exception {
        PowerMockito.stub(PowerMockito.method(Results.class, "async")).toReturn(mockResult);

        Result result = kikuyuController.siphon(TEST_PATH);
        verify(wrapper).url(TEST_ADDRESS + "/urlMappings");
        verify(requestHolder).get();
    }

    @Test
    public void testUrlMappingsToPageRequestPromiseFunction() throws Throwable {
        final KikuyuController.UrlMappingsToPageRequestPromiseFunction urlMappingsToPageRequestPromiseFunction = new KikuyuController.UrlMappingsToPageRequestPromiseFunction(
                TEST_ADDRESS, responseResultFunction, wrapper);
        when(responsePromise.map(responseResultFunction)).thenReturn(resultPromise);
        when(response.getBody()).thenReturn(DESTINATION_PAGE_HTML);

        final F.Promise<Result> actualResultPromise = urlMappingsToPageRequestPromiseFunction.apply(response);
        assertEquals(resultPromise, actualResultPromise);
        verify(wrapper).url(DESTINATION_PAGE_HTML);
        verify(requestHolder).get();
    }

    @Test
    public void testResponseResultFunction() throws Throwable {
        final KikuyuController.OutputPageResponseToClientFunction outputPageWSResponse = new KikuyuController.OutputPageResponseToClientFunction();
        PowerMockito.stub(PowerMockito.method(Results.class, "ok", String.class)).toReturn(status);
        final Result result = outputPageWSResponse.apply(response);
    }
}
