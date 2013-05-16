package util;

import controllers.ws.WSWrapper;
import domain.PageComponent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Http;

import java.util.HashMap;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {Controller.class})
public class ComponentResponsePromiseFactoryImplTest {

    private static final String TEMPLATE_URL = "templateUrl";

    @Mock
    private WSWrapper wrapper;

    private ComponentResponsePromiseFactoryImpl responsePromiseFactory;
    private PageComponent templatePageComponent;
    private WS.WSRequestHolder templateRequestHolder;
    private F.Promise<WS.Response> templateResponsePromise;
    private Http.Request mockRequest;

    @Before
    public void setUp() throws Exception {
        templateRequestHolder = mock(WS.WSRequestHolder.class);
        templateResponsePromise = mock(F.Promise.class);
        mockRequest = mock(Http.Request.class);

        responsePromiseFactory = new ComponentResponsePromiseFactoryImpl();
        responsePromiseFactory.setWsWrapper(wrapper);

        when(wrapper.url(anyString())).thenReturn(templateRequestHolder);
        when(templateRequestHolder.get()).thenReturn(templateResponsePromise);
    }

    @Test
    public void testPostWithAcceptPostNoBody() throws Exception {
        templatePageComponent = new PageComponent(TEMPLATE_URL, true, true, new HashMap());
        final Http.RequestBody requestBody = mock(Http.RequestBody.class);
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.body()).thenReturn(requestBody);
        HashMap<String, String[]> formValues = new HashMap<>();
        when(requestBody.asFormUrlEncoded()).thenReturn(formValues);

        F.Promise<WS.Response> responsePromise = responsePromiseFactory.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).post("");
        verifyNoMoreInteractions(templateRequestHolder);
    }

    @Test
    public void testPostWithAcceptPost() throws Exception {
        templatePageComponent = new PageComponent(TEMPLATE_URL, true, true, new HashMap());
        final Http.RequestBody requestBody = mock(Http.RequestBody.class);
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.body()).thenReturn(requestBody);
        HashMap<String, String[]> formValues = new HashMap<>();
        formValues.put("body", new String[]{"test"});
        formValues.put("body2", new String[]{"test2"});
        when(requestBody.asFormUrlEncoded()).thenReturn(formValues);

        F.Promise<WS.Response> responsePromise = responsePromiseFactory.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).post("body2=test2&body=test");
        verifyNoMoreInteractions(templateRequestHolder);
    }

    @Test
    public void testPostWithoutAcceptPostFlag() throws Exception {
        templatePageComponent = new PageComponent(TEMPLATE_URL, false, true, new HashMap());
        when(mockRequest.method()).thenReturn("POST");

        F.Promise<WS.Response> responsePromise = responsePromiseFactory.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).get();
        verifyNoMoreInteractions(templateRequestHolder);
    }

    @Test
    public void testPlainGetWithHeaders() throws Exception {
        templatePageComponent = new PageComponent(TEMPLATE_URL, false, true, new HashMap());
        HashMap headers = new HashMap();
        headers.put("Content-Type", new String[]{"value"});
        headers.put("Cookie", new String[]{"cvalue"});
        headers.put("Accept-Encoding", new String[]{"encvalue"});
        when(mockRequest.headers()).thenReturn(headers);
        when(mockRequest.method()).thenReturn("GET");

        F.Promise<WS.Response> responsePromise = responsePromiseFactory.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).get();
        verify(templateRequestHolder).setHeader("Content-Type", "value");
        verify(templateRequestHolder).setHeader("Cookie", "cvalue");
        verifyNoMoreInteractions(templateRequestHolder);
    }

    @Test
    public void testUrlGetWithQueryParams() throws Exception {
        String uri = "http://" + TEMPLATE_URL + ".com/?param1=value1&param2&param3=value3";
        templatePageComponent = new PageComponent(uri, false, true, new HashMap());
        when(mockRequest.method()).thenReturn("GET");


        F.Promise<WS.Response> responsePromise = responsePromiseFactory.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).setQueryParameter("param1", "value1");
        verify(templateRequestHolder).setQueryParameter("param2", null);
        verify(templateRequestHolder).setQueryParameter("param3", "value3");

        verify(templateRequestHolder).get();
        verifyNoMoreInteractions(templateRequestHolder);
    }
}
