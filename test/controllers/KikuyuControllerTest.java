package controllers;

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
    private UrlMappingsRetriever urlMappingsRetriever;
    @Mock
    private ResponseComposer responseComposer;
    @Mock
    private ResponsePromiseFactory requestPromiseFactory;

    private KikuyuController kikuyuController = new KikuyuController();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        kikuyuController.setUrlMappingsRetriever(urlMappingsRetriever);
        kikuyuController.setResponseComposer(responseComposer);
        kikuyuController.setResponsePromiseFactory(requestPromiseFactory);
    }

    @Test
    public void testSiphon() throws Exception {

        final UrlMatcher urlMatcher = mock(UrlMatcher.class);

        PageComponent templatePageComponent = new PageComponent(TEMPLATE_URL, false, true, new HashMap());
        PageComponent pageComponent1 = new PageComponent(COMPONENT_URL1, false, true, new HashMap());
        PageComponent pageComponent2 = new PageComponent(COMPONENT_URL2, false, true, new HashMap());
        final Page page = new Page(Arrays.asList(templatePageComponent,
                pageComponent1, pageComponent2));
        final Http.Request mockRequest = mock(Http.Request.class);

        final Results.AsyncResult mockResult = mock(Results.AsyncResult.class);
        final ComposeClientResponseFunction outputFunction =
                mock(ComposeClientResponseFunction.class);

        when(urlMappingsRetriever.getUrlMatcher()).thenReturn(urlMatcher);
        when(urlMatcher.match(anyString())).thenReturn(page);

        PowerMockito.stub(PowerMockito.method(Controller.class, "request")).toReturn(mockRequest);

        PowerMockito.whenNew(ComposeClientResponseFunction.class).withAnyArguments().thenReturn(outputFunction);
        PowerMockito.stub(PowerMockito.method(F.Promise.class, "sequence", F.Promise[].class)).toReturn(mock(F.Promise.class));

        PowerMockito.stub(PowerMockito.method(Results.class, "async")).toReturn(mockResult);

        Result result = kikuyuController.siphon(TEST_PATH);

        verify(requestPromiseFactory).getResponsePromise(mockRequest, templatePageComponent);
        verify(requestPromiseFactory).getResponsePromise(mockRequest, pageComponent1);
        verify(requestPromiseFactory).getResponsePromise(mockRequest, pageComponent2);
        PowerMockito.verifyNew(ComposeClientResponseFunction.class).withArguments(responseComposer, page);
    }

}
