package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.iterators.EmptyIterator;
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
        assertEquals(URL_FOR_WILD_CARD_MATCH, urlMatcher.match(NO_SPECIFIC_MATCH).getPageComponents().get(0).getUrl());
    }

    @Test
    public void testBeforeWildcardMatch() throws Exception {
        assertEquals("http://news.bbc.co.uk", urlMatcher.match("bbc").getPageComponents().get(0).getUrl());
    }

    @Test
    public void testNeedsSortingBeforeWildcardMatch() throws Exception {
        assertEquals("http://uk.practicallaw.com", urlMatcher.match("page").getPageComponents().get(0).getUrl());
    }

    @Test
    public void testWildcardMatchBecauseOfOrder() throws Exception {
        assertEquals("urlForWildCardMatch", urlMatcher.match("whippet").getPageComponents().get(0).getUrl());
    }

    @Test
    public void testMatchingGroup() throws Exception {
        assertEquals("http://uk.practicallaw.com/page2", urlMatcher.match("page2").getPageComponents().get(0).getUrl());
        assertEquals("http://component/page2", urlMatcher.match("page2").getPageComponents().get(1).getUrl());
    }

    @Test
    public void testComponentUrl() throws Exception {
        assertEquals("componentUrl", urlMatcher.match("bbc").getPageComponents().get(1).getUrl());
    }

    @Test
    public void testNull() throws Exception {
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.iterator()).thenReturn(EmptyIterator.INSTANCE);
        urlMatcher = new UrlMatcherImpl(jsonNode);

        assertEquals(null, urlMatcher.match(NO_SPECIFIC_MATCH));
    }

}
