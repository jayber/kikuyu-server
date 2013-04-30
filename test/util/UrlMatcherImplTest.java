package util;

import org.apache.commons.collections.iterators.EmptyIterator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlMatcherImplTest {

    private static final String URL_FOR_WILD_CARD_MATCH = "urlForWildCardMatch";
    private static final String URL_MAPPINGS = "testData.json";
    private static final String NO_SPECIFIC_MATCH = "noSpecificMatch";

    private UrlMatcherImpl urlMatcher;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream(URL_MAPPINGS));
        urlMatcher = new UrlMatcherImpl(jsonNode);
    }

    @Test
    public void testWildcardMatch() throws Exception {
        assertEquals(URL_FOR_WILD_CARD_MATCH, urlMatcher.match(NO_SPECIFIC_MATCH).getComponentUrls().get(0));
    }

    @Test
    public void testBeforeWildcardMatch() throws Exception {
        assertEquals("http://news.bbc.co.uk", urlMatcher.match("bbc").getComponentUrls().get(0));
    }

    @Test
    public void testNeedsSortingBeforeWildcardMatch() throws Exception {
        assertEquals("http://uk.practicallaw.com", urlMatcher.match("page").getComponentUrls().get(0));
    }

    @Test
    public void testWildcardMatchBecauseOfOrder() throws Exception {
        assertEquals("urlForWildCardMatch", urlMatcher.match("whippet").getComponentUrls().get(0));
    }

    @Test
    public void testMatchingGroup() throws Exception {
        assertEquals("http://uk.practicallaw.com/page2", urlMatcher.match("page2").getComponentUrls().get(0));
        assertEquals("http://component/page2", urlMatcher.match("page2").getComponentUrls().get(1));
    }

    @Test
    public void testComponentUrl() throws Exception {
        assertEquals("componentUrl", urlMatcher.match("bbc").getComponentUrls().get(1));
    }

    @Test
    public void testNull() throws Exception {
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.iterator()).thenReturn(EmptyIterator.INSTANCE);
        urlMatcher = new UrlMatcherImpl(jsonNode);

        assertEquals(null, urlMatcher.match(NO_SPECIFIC_MATCH));
    }

}
