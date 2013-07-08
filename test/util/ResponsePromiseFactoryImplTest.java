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
public class ResponsePromiseFactoryImplTest {

    private static final String TEMPLATE_URL = "templateUrl";

    @Mock
    private WSWrapper wrapper;

    private ResponsePromiseFactoryImpl target;

    private PageComponent templatePageComponent;
    private WS.WSRequestHolder templateRequestHolder;
    private F.Promise<WS.Response> templateResponsePromise;
    private Http.Request mockRequest;

    @Before
    public void setUp() throws Exception {
        templateRequestHolder = mock(WS.WSRequestHolder.class);
        templateResponsePromise = mock(F.Promise.class);
        mockRequest = mock(Http.Request.class);

        target = new ResponsePromiseFactoryImpl();
        target.setWsWrapper(wrapper);

        when(templateRequestHolder.get()).thenReturn(templateResponsePromise);
        when(mockRequest.host()).thenReturn("uk.practicallaw.com");
        when(mockRequest.uri()).thenReturn("/absolute/uri?p=1");

    }

    @Test
    public void testPostWithAcceptPostNoBody() throws Exception {
        when(wrapper.url(anyString())).thenReturn(templateRequestHolder);
        templatePageComponent = new PageComponent(TEMPLATE_URL, true, true, new HashMap());
        final Http.RequestBody requestBody = mock(Http.RequestBody.class);
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.body()).thenReturn(requestBody);
        HashMap<String, String[]> formValues = new HashMap<String, String[]>();
        when(requestBody.asFormUrlEncoded()).thenReturn(formValues);

        F.Promise<WS.Response> responsePromise = target.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).post("");
        verify(templateRequestHolder).setHeader("Originator-Uri", "http://uk.practicallaw.com/absolute/uri?p=1");
        verifyNoMoreInteractions(templateRequestHolder);
    }

    @Test
    public void testPostWithAcceptPost() throws Exception {
        when(wrapper.url(anyString())).thenReturn(templateRequestHolder);
        templatePageComponent = new PageComponent(TEMPLATE_URL, true, true, new HashMap());
        final Http.RequestBody requestBody = mock(Http.RequestBody.class);
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.body()).thenReturn(requestBody);
        HashMap<String, String[]> formValues = new HashMap<String, String[]>();
        formValues.put("body", new String[]{"test"});
        formValues.put("body2", new String[]{"test2"});
        when(requestBody.asFormUrlEncoded()).thenReturn(formValues);

        F.Promise<WS.Response> responsePromise = target.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).post("body2=test2&body=test");
        verify(templateRequestHolder).setHeader("Originator-Uri", "http://uk.practicallaw.com/absolute/uri?p=1");
        verifyNoMoreInteractions(templateRequestHolder);
    }

    @Test
    public void testPostWithoutAcceptPostFlag() throws Exception {
        when(wrapper.url(anyString())).thenReturn(templateRequestHolder);
        templatePageComponent = new PageComponent(TEMPLATE_URL, false, true, new HashMap());
        when(mockRequest.method()).thenReturn("POST");

        F.Promise<WS.Response> responsePromise = target.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).get();
        verify(templateRequestHolder).setHeader("Originator-Uri", "http://uk.practicallaw.com/absolute/uri?p=1");
        verifyNoMoreInteractions(templateRequestHolder);
    }

    @Test
    public void testPlainGetWithHeaders() throws Exception {
        when(wrapper.url(anyString())).thenReturn(templateRequestHolder);
        templatePageComponent = new PageComponent(TEMPLATE_URL, false, true, new HashMap());
        HashMap headers = new HashMap();
        headers.put("Content-Type", new String[]{"value"});
        headers.put("Cookie", new String[]{"cvalue"});
        headers.put("Accept-Encoding", new String[]{"encvalue"});
        when(mockRequest.headers()).thenReturn(headers);
        when(mockRequest.method()).thenReturn("GET");

        F.Promise<WS.Response> responsePromise = target.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).get();
        verify(templateRequestHolder).setHeader("Content-Type", "value");
        verify(templateRequestHolder).setHeader("Cookie", "cvalue");
        verify(templateRequestHolder).setHeader("Originator-Uri", "http://uk.practicallaw.com/absolute/uri?p=1");
        verifyNoMoreInteractions(templateRequestHolder);
    }

    @Test
    public void testUrlGetWithQueryParams() throws Exception {
        String componentUrl = "http://" + TEMPLATE_URL + ".com/?param1=value1&{params}&param2&param3=value3";
        String requestParams = "rparam1=rval1&rparam2=rval2";
        String urlToBeRequested = "http://" + TEMPLATE_URL + ".com/";
        when(wrapper.url(urlToBeRequested)).thenReturn(templateRequestHolder);
        templatePageComponent = new PageComponent(componentUrl, false, true, new HashMap());
        when(mockRequest.uri()).thenReturn("/testUrl?" + requestParams);
        when(mockRequest.method()).thenReturn("GET");

        F.Promise<WS.Response> responsePromise = target.getResponsePromise(mockRequest, templatePageComponent);

        verify(wrapper).url(urlToBeRequested);

        verify(templateRequestHolder).setQueryParameter("param1", "value1");
        verify(templateRequestHolder).setQueryParameter("rparam1", "rval1");
        verify(templateRequestHolder).setQueryParameter("rparam2", "rval2");
        verify(templateRequestHolder).setQueryParameter("param2", "");
        verify(templateRequestHolder).setQueryParameter("param3", "value3");

        verify(templateRequestHolder).get();
        verify(templateRequestHolder).setHeader(
                "Originator-Uri",
                "http://uk.practicallaw.com/testUrl?rparam1=rval1&rparam2=rval2"
        );
        verifyNoMoreInteractions(templateRequestHolder);
    }

    //just tests that params are decoded before being set on the request holder, otherwise Play will double encode them
    @Test
    public void testUrlGetWithQueryParamsUrlEncoded() throws Exception {
        String componentUrl = "http://" + TEMPLATE_URL + ".com/?{params}";
        String requestParams = "childpagename=PLC%2FPageLayout&pagename=PLCWrapper&view=cselement%3APLC%2FAuthentication%2FDefaultLogin";
        String urlToBeRequested = "http://" + TEMPLATE_URL + ".com/";

        when(wrapper.url(urlToBeRequested)).thenReturn(templateRequestHolder);
        templatePageComponent = new PageComponent(componentUrl, false, true, new HashMap());
        when(mockRequest.uri()).thenReturn("/testUrl?" + requestParams);
        when(mockRequest.method()).thenReturn("GET");

        F.Promise<WS.Response> responsePromise = target.getResponsePromise(mockRequest, templatePageComponent);

        verify(templateRequestHolder).setQueryParameter("childpagename", "PLC/PageLayout");
        verify(templateRequestHolder).setQueryParameter("pagename", "PLCWrapper");
        verify(templateRequestHolder).setQueryParameter("view", "cselement:PLC/Authentication/DefaultLogin");
        verify(templateRequestHolder).get();
        verify(templateRequestHolder).setHeader(
                "Originator-Uri",
                "http://uk.practicallaw.com/testUrl?childpagename=PLC%2FPageLayout&pagename=PLCWrapper&view=cselement%3APLC%2FAuthentication%2FDefaultLogin"
        );
        verifyNoMoreInteractions(templateRequestHolder);
    }

}
