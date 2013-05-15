package controllers;

import controllers.ws.WSWrapper;
import domain.Page;
import domain.PageComponent;
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
import util.*;

import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {KikuyuController.class, Controller.class, F.Promise.class})
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
    @Mock
    private ComponentRequestPromiseFactory requestPromiseFactory;

    private KikuyuController kikuyuController = new KikuyuController();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        kikuyuController.setWsWrapper(wrapper);
        kikuyuController.setUrlMappingsRetriever(urlMappingsRetriever);
        kikuyuController.setResponseComposer(responseComposer);
        kikuyuController.setRequestPromiseFactory(requestPromiseFactory);
    }

    @Test
    public void testSiphon() throws Exception {

        final UrlMatcher urlMatcher = mock(UrlMatcher.class);

        PageComponent pageComponent = new PageComponent(TEMPLATE_URL, false, true, new HashMap());
        PageComponent pageComponent1 = new PageComponent(COMPONENT_URL1, false, true, new HashMap());
        PageComponent pageComponent2 = new PageComponent(COMPONENT_URL2, false, true, new HashMap());
        final Page page = new Page(Arrays.asList(pageComponent,
                pageComponent1, pageComponent2));
        final WS.WSRequestHolder templateRequestHolder = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> templateResponsePromise = mock(F.Promise.class);
        final WS.WSRequestHolder componentRequestHolder = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> componentResponsePromise = mock(F.Promise.class);
        final WS.WSRequestHolder componentRequestHolder2 = mock(WS.WSRequestHolder.class);
        final F.Promise<WS.Response> componentResponsePromise2 = mock(F.Promise.class);
        final Http.Request mockRequest = mock(Http.Request.class);

        final Results.AsyncResult mockResult = mock(Results.AsyncResult.class);
        final ComposeClientResponseFunction outputFunction =
                mock(ComposeClientResponseFunction.class);

        when(urlMappingsRetriever.getUrlMatcher()).thenReturn(urlMatcher);
        when(urlMatcher.match(anyString())).thenReturn(page);

        when(wrapper.url(TEMPLATE_URL)).thenReturn(templateRequestHolder);
        when(templateRequestHolder.get()).thenReturn(templateResponsePromise);

        when(wrapper.url(COMPONENT_URL1)).thenReturn(componentRequestHolder);
        when(componentRequestHolder.get()).thenReturn(componentResponsePromise);

        when(wrapper.url(COMPONENT_URL2)).thenReturn(componentRequestHolder2);
        when(componentRequestHolder2.get()).thenReturn(componentResponsePromise2);

        PowerMockito.stub(PowerMockito.method(Controller.class, "request")).toReturn(mockRequest);

        PowerMockito.whenNew(ComposeClientResponseFunction.class).withAnyArguments().thenReturn(outputFunction);
        PowerMockito.stub(PowerMockito.method(F.Promise.class, "sequence", F.Promise[].class)).toReturn(mock(F.Promise.class));

        PowerMockito.stub(PowerMockito.method(Results.class, "async")).toReturn(mockResult);

        Result result = kikuyuController.siphon(TEST_PATH);

        verify(wrapper).url(TEMPLATE_URL);
        verify(wrapper).url(COMPONENT_URL1);
        verify(wrapper).url(COMPONENT_URL2);

        verify(requestPromiseFactory).setRequestParams(new String[]{TEMPLATE_URL}, templateRequestHolder);
        verify(requestPromiseFactory).setRequestParams(new String[]{COMPONENT_URL1}, componentRequestHolder);
        verify(requestPromiseFactory).setRequestParams(new String[]{COMPONENT_URL2}, componentRequestHolder2);
        PowerMockito.verifyNew(ComposeClientResponseFunction.class).withArguments(responseComposer, page);
    }

}
