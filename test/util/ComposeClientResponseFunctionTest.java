package util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.libs.WS;
import play.mvc.Result;
import play.mvc.Results;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Results.class)
public class ComposeClientResponseFunctionTest {
    private static final String TEMPLATE_PAGE_HTML = "destination page html";
    private static final String COMPONENT1_PAGE_HTML = "component1 page html";
    private static final String COMPONENT2_PAGE_HTML = "component2 page html";
    private static final String COMBINED_RESPONSE = "combined response";

    @Test
    public void testOutputPageResponseAsTextToClientFunction() throws Throwable {
        final WS.Response templatePageResponse = mock(WS.Response.class);
        final WS.Response componentPageResponse1 = mock(WS.Response.class);
        final WS.Response componentPageResponse2 = mock(WS.Response.class);
        List<WS.Response> responses = new ArrayList<WS.Response>();
        responses.add(templatePageResponse);
        responses.add(componentPageResponse1);
        responses.add(componentPageResponse2);
        final ResponseComposer responseComposer = mock(ResponseComposer.class);

        when(templatePageResponse.getHeader("Content-Type")).thenReturn("text/html");
        when(templatePageResponse.getBody()).thenReturn(TEMPLATE_PAGE_HTML);
        when(componentPageResponse1.getBody()).thenReturn(COMPONENT1_PAGE_HTML);
        when(componentPageResponse2.getBody()).thenReturn(COMPONENT2_PAGE_HTML);
        when(responseComposer.composeBody(TestFixtures.page, TEMPLATE_PAGE_HTML, COMPONENT1_PAGE_HTML, COMPONENT2_PAGE_HTML)).thenReturn(COMBINED_RESPONSE);

        final Results.Status okStatus = mock(Results.Status.class);
        final Results.Status htmlStatus = mock(Results.Status.class);

        PowerMockito.stub(PowerMockito.method(Results.class, "ok", String.class)).toReturn(okStatus);
        when(okStatus.as("text/html")).thenReturn(htmlStatus);

        final ComposeClientResponseFunction outputPageWSResponse = new ComposeClientResponseFunction(responseComposer, TestFixtures.page);
        final Result actualResult = outputPageWSResponse.apply(responses);

        assertEquals(htmlStatus, actualResult);
        verify(templatePageResponse).getHeader("Content-Type");
        verify(templatePageResponse).getBody();
        verify(componentPageResponse1).getBody();
        verify(componentPageResponse2).getBody();
        verify(responseComposer).composeBody(TestFixtures.page, TEMPLATE_PAGE_HTML, COMPONENT1_PAGE_HTML, COMPONENT2_PAGE_HTML);
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

        final ComposeClientResponseFunction outputPageWSResponse = new ComposeClientResponseFunction(responseComposer, TestFixtures.page);
        final Result actualResult = outputPageWSResponse.apply(responses);

        assertEquals(httpStatus, actualResult);
        verify(templatePageResponse).getHeader("Content-Type");
        verify(templatePageResponse).getStatus();
        verify(templatePageResponse).getBodyAsStream();
        verify(okStatus).as("binary");
        verifyNoMoreInteractions(componentPageResponse);
    }
}
