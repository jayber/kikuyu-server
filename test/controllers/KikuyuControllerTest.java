package controllers;

import controllers.ws.WSWrapper;
import domain.ComponentUrl;
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
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import util.ComposeClientResponseFunction;
import util.ResponseComposer;
import util.UrlMappingsRetriever;
import util.UrlMatcher;

import java.util.Arrays;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {KikuyuController.class, F.Promise.class})
public class KikuyuControllerTest {

    private static final String TEST_PATH = "testPath";
    private static final String COMPONENT_URL1 = "component url1";
    private static final String COMPONENT_URL2 = "component url2";
    private static final String TEMPLATE_URL = "templateUrl";

    @Mock
    private WSWrapper wrapper;
    @Mock
    private UrlMappingsRetriever urlMappingsRetriever;
    @Mock
    private ResponseComposer responseComposer;

    private KikuyuController kikuyuController = new KikuyuController();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        kikuyuController.setWsWrapper(wrapper);
        kikuyuController.setUrlMappingsRetriever(urlMappingsRetriever);
        kikuyuController.setResponseComposer(responseComposer);
    }

    @Test
    public void testSiphon() throws Throwable {

        final UrlMatcher urlMatcher = mock(UrlMatcher.class);

        final WS.WSRequestHolder templateRequestHolder = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> templateResponsePromise = mock(F.Promise.class);
        final WS.WSRequestHolder componentRequestHolder = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> componentResponsePromise = mock(F.Promise.class);
        final WS.WSRequestHolder componentRequestHolder2 = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> componentResponsePromise2 = mock(F.Promise.class);

        final Results.AsyncResult mockResult = mock(Results.AsyncResult.class);
        final ComposeClientResponseFunction outputFunction =
                mock(ComposeClientResponseFunction.class);
        final Http.Request request = mock(Http.Request.class);


        when(urlMappingsRetriever.getUrlMatcher()).thenReturn(urlMatcher);
        when(urlMatcher.match(anyString())).thenReturn(new Page(Arrays.asList(new ComponentUrl(TEMPLATE_URL, false, true),
                new ComponentUrl(COMPONENT_URL1, false, true), new ComponentUrl(COMPONENT_URL2, false, true))));

        when(wrapper.url(TEMPLATE_URL)).thenReturn(templateRequestHolder);
        when(templateRequestHolder.get()).thenReturn(templateResponsePromise);

        when(wrapper.url(COMPONENT_URL1)).thenReturn(componentRequestHolder);
        when(componentRequestHolder.get()).thenReturn(componentResponsePromise);

        when(wrapper.url(COMPONENT_URL2)).thenReturn(componentRequestHolder2);
        when(componentRequestHolder2.get()).thenReturn(componentResponsePromise2);

        PowerMockito.mockStatic(Controller.class);
        PowerMockito.when(Controller.request()).thenReturn(request);
        when(request.method()).thenReturn("GET");

        PowerMockito.spy(F.Promise.class);
        F.Promise promisesSequence = mock(F.Promise.class);


        PowerMockito.when(PowerMockito.method(F.Promise.class, "sequence", F.Promise[].class)).thenReturn(promisesSequence);
        when(promisesSequence.map(any(F.Function.class))).thenReturn(mock(F.Promise.class));

        PowerMockito.whenNew(ComposeClientResponseFunction.class).withAnyArguments().thenReturn(outputFunction);
        PowerMockito.stub(PowerMockito.method(Results.class, "async")).toReturn(mockResult);


        Result result = kikuyuController.siphon(TEST_PATH);

        verify(wrapper).url(TEMPLATE_URL);
        verify(wrapper).url(COMPONENT_URL1);
        verify(templateRequestHolder).get();
        verify(componentRequestHolder).get();
        verify(componentRequestHolder2).get();
        PowerMockito.verifyNew(ComposeClientResponseFunction.class).withArguments(responseComposer);
    }

}
