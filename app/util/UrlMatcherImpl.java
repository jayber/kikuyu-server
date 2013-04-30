package util;

import domain.Page;
import org.codehaus.jackson.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlMatcherImpl implements UrlMatcher {
    private ArrayList<PatternPage> patterns = new ArrayList<PatternPage>();

    public UrlMatcherImpl(JsonNode urlMappings) {
        for (JsonNode urlMapping : urlMappings) {
            final PatternPage patternPage = new PatternPage(urlMapping.path("pattern").asText(),
                    urlMapping.path("matchOrder").asInt(), urlMapping.path("page").path("pageComponents").findValuesAsText("url")
            );
            patterns.add(patternPage);
        }
        Collections.sort(patterns, new Comparator<PatternPage>() {
            @Override
            public int compare(PatternPage patternPage1, PatternPage patternPage2) {
                return Integer.compare(patternPage1.matchOrder, patternPage2.matchOrder);
            }
        });
    }

    @Override
    public Page match(String path) {
        for (PatternPage patternPage : patterns) {
            final Matcher matcher = patternPage.pattern.matcher(path);
            if (matcher.matches()) {
                return createPage(patternPage, matcher);
            }
        }
        return null;
    }

    private Page createPage(PatternPage patternPage, Matcher matcher) {
        List realUrls = resolveUrlFromExpression(matcher, patternPage.urls);
        return new Page(realUrls);
    }

    private List resolveUrlFromExpression(Matcher matcher, final List<String> urls) {
        ArrayList<String> results = new ArrayList<String>(urls.size());
        for (String url : urls) {
            results.add(url.replace("{0}", matcher.group(0)));
        }
        return results;
    }

    private class PatternPage {
        private final Pattern pattern;
        private int matchOrder;
        private final List<String> urls;

        public PatternPage(String pattern, int matchOrder, List<String> urls) {
            this.matchOrder = matchOrder;
            this.pattern = Pattern.compile(pattern);
            this.urls = urls;
        }
    }
}
