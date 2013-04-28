package util;

import domain.Page;
import org.codehaus.jackson.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlMatcherImpl implements UrlMatcher {
    private ArrayList<PatternPage> patterns = new ArrayList<PatternPage>();

    public UrlMatcherImpl(JsonNode urlMappings) {
        for (JsonNode urlMapping : urlMappings) {
            final PatternPage patternPage = new PatternPage(urlMapping.path("pattern").asText(),
                    urlMapping.path("page").path("url").asText(),
                    urlMapping.path("matchOrder").asInt(),
                    urlMapping.path("page").path("componentUrl").getTextValue());
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
        String realTemplateUrl = resolveUrlFromExpression(matcher, patternPage.destinationUrl);
        String realComponentUrl = resolveUrlFromExpression(matcher, patternPage.componentUrl);
        return new Page(realTemplateUrl, realComponentUrl);
    }

    private String resolveUrlFromExpression(Matcher matcher, final String url) {
        return url.replace("{0}", matcher.group(0));
    }

    private class PatternPage {
        private final Pattern pattern;
        private final String destinationUrl;
        private int matchOrder;
        private String componentUrl;

        public PatternPage(String pattern, String destinationUrl, int matchOrder, String componentUrl) {
            this.matchOrder = matchOrder;
            this.pattern = Pattern.compile(pattern);
            this.destinationUrl = destinationUrl;
            this.componentUrl = componentUrl == null ? "" : componentUrl;
        }

        @Override
        public String toString() {
            return "PatternPage{" +
                    "pattern=" + pattern +
                    ", destinationUrl='" + destinationUrl + '\'' +
                    '}';
        }
    }
}
