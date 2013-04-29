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
    private static final String URL_MAPPINGS = "[{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":3,\"matchOrder\":0,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":1,\"componentUrl\":\"componentUrl\",\"name\":\"blank\",\"url\":\"http://news.bbc.co.uk\"},\"pageId\":1,\"pattern\":\"bbc\"}," +
            "{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":11,\"matchOrder\":10,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":1,\"componentUrl\":null,\"name\":\"blank\",\"url\":\"" + URL_FOR_WILD_CARD_MATCH + "\"},\"pageId\":1,\"pattern\":\".*\"}," +
            "{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":1,\"matchOrder\":1,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":2,\"componentUrl\":null,\"name\":\"home\",\"url\":\"http://uk.practicallaw.com\"},\"pageId\":2,\"pattern\":\"page\"}," +
            "{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":1,\"matchOrder\":1,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":2,\"componentUrl\":\"http://component/{0}\",\"name\":\"home\",\"url\":\"http://uk.practicallaw.com/{0}\"},\"pageId\":2,\"pattern\":\"page2\"}," +
            "{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":12,\"matchOrder\":11,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":1,\"componentUrl\":null,\"name\":\"blank\",\"url\":\"whippetUrl\"},\"pageId\":1,\"pattern\":\"whippet\"}]";
    private static final String NO_SPECIFIC_MATCH = "noSpecificMatch";

    private UrlMatcherImpl urlMatcher;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode jsonNode = mapper.readTree(URL_MAPPINGS);
        urlMatcher = new UrlMatcherImpl(jsonNode);
    }

    @Test
    public void testWildcardMatch() throws Exception {
        assertEquals(URL_FOR_WILD_CARD_MATCH, urlMatcher.match(NO_SPECIFIC_MATCH).getTemplateUrl());
    }

    @Test
    public void testBeforeWildcardMatch() throws Exception {
        assertEquals("http://news.bbc.co.uk", urlMatcher.match("bbc").getTemplateUrl());
    }

    @Test
    public void testNeedsSortingBeforeWildcardMatch() throws Exception {
        assertEquals("http://uk.practicallaw.com", urlMatcher.match("page").getTemplateUrl());
    }

    @Test
    public void testWildcardMatchBecauseOfOrder() throws Exception {
        assertEquals("urlForWildCardMatch", urlMatcher.match("whippet").getTemplateUrl());
    }

    @Test
    public void testMatchingGroup() throws Exception {
        assertEquals("http://uk.practicallaw.com/page2", urlMatcher.match("page2").getTemplateUrl());
        assertEquals("http://component/page2", urlMatcher.match("page2").getComponentUrl());
    }

    @Test
    public void testComponentUrl() throws Exception {
        assertEquals("componentUrl", urlMatcher.match("bbc").getComponentUrl());
    }

    @Test
    public void testComponentUrlNull() throws Exception {
        assertEquals("", urlMatcher.match(NO_SPECIFIC_MATCH).getComponentUrl());
    }

    @Test
    public void testNull() throws Exception {
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.iterator()).thenReturn(EmptyIterator.INSTANCE);
        urlMatcher = new UrlMatcherImpl(jsonNode);

        assertEquals(null, urlMatcher.match(NO_SPECIFIC_MATCH));
    }

}
