package util;

import org.codehaus.jackson.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UrlMappingsRetriever.class)
public class UrlMappingsRetrieverTest {
    private static final String TEST_ADDRESS = "testAddress";

    @Test
    public void testLoadUrlMappings() throws Exception {
        final RestTemplate restTemplate = mock(RestTemplate.class);
        final UrlMatcherImpl urlMatcher = mock(UrlMatcherImpl.class);
        final JsonNode jsonNode = mock(JsonNode.class);

        PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
        when(restTemplate.getForObject(TEST_ADDRESS, JsonNode.class)).thenReturn(jsonNode);
        PowerMockito.whenNew(UrlMatcherImpl.class).withArguments(jsonNode).thenReturn(urlMatcher);

        final UrlMappingsRetriever urlMappingsRetriever = new UrlMappingsRetriever();
        urlMappingsRetriever.loadUrlMappingsFromWS(TEST_ADDRESS);

        assertEquals(urlMatcher, urlMappingsRetriever.getUrlMatcher());

        PowerMockito.verifyNew(RestTemplate.class).withNoArguments();
        verify(restTemplate).getForObject(TEST_ADDRESS, JsonNode.class);
        PowerMockito.verifyNew(UrlMatcherImpl.class).withArguments(jsonNode);
    }

}
