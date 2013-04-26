package util;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UrlMatcherImplTest {

    private static final String URL_FOR_WILD_CARD_MATCH = "urlForWildCardMatch";
    private static final String URL_MAPPINGS = "[{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":3,\"matchOrder\":0,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":1,\"name\":\"blank\",\"url\":\"http://news.bbc.co.uk\"},\"pageId\":1,\"pattern\":\"bbc\"}," +
            "{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":11,\"matchOrder\":10,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":1,\"name\":\"blank\",\"url\":\"" + URL_FOR_WILD_CARD_MATCH + "\"},\"pageId\":1,\"pattern\":\".*\"}," +
            "{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":1,\"matchOrder\":1,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":2,\"name\":\"home\",\"url\":\"http://uk.practicallaw.com\"},\"pageId\":2,\"pattern\":\"page\"}," +
            "{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":1,\"matchOrder\":1,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":2,\"name\":\"home\",\"url\":\"http://uk.practicallaw.com/{0}\"},\"pageId\":2,\"pattern\":\"page2\"}," +
            "{\"class\":\"kikuyu.domain.UrlMapping\",\"id\":12,\"matchOrder\":11,\"page\":{\"class\":\"kikuyu.domain.Page\",\"id\":1,\"name\":\"blank\",\"url\":\"whippetUrl\"},\"pageId\":1,\"pattern\":\"whippet\"}]";
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
        assertEquals(URL_FOR_WILD_CARD_MATCH, urlMatcher.match(NO_SPECIFIC_MATCH));
    }

    @Test
    public void testBeforeWildcardMatch() throws Exception {
        assertEquals("http://news.bbc.co.uk", urlMatcher.match("bbc"));
    }

    @Test
    public void testNeedsSortingBeforeWildcardMatch() throws Exception {
        assertEquals("http://uk.practicallaw.com", urlMatcher.match("page"));
    }

    @Test
    public void testWildcardMatchBecauseOfOrder() throws Exception {
        assertEquals("urlForWildCardMatch", urlMatcher.match("whippet"));
    }

    @Test
    public void testMatchingGroup() throws Exception {
        assertEquals("http://uk.practicallaw.com/page2", urlMatcher.match("page2"));
    }
}
