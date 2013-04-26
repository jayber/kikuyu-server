package util;

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
                    urlMapping.path("matchOrder").asInt());
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
    public String match(String path) {
        for (PatternPage patternPage : patterns) {
            final Matcher matcher = patternPage.pattern.matcher(path);
            if (matcher.matches()) {
                return patternPage.destinationUrl.replace("{0}", matcher.group(0));
            }
        }
        return null;
    }

    private class PatternPage {
        private final Pattern pattern;
        private final String destinationUrl;
        private int matchOrder;

        public PatternPage(String pattern, String destinationUrl, int matchOrder) {
            this.matchOrder = matchOrder;
            this.pattern = Pattern.compile(pattern);
            this.destinationUrl = destinationUrl;
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
