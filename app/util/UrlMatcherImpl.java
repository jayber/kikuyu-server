package util;

import domain.ComponentUrl;
import domain.Page;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import play.Logger;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlMatcherImpl implements UrlMatcher {
    private ArrayList<PatternPage> patterns = new ArrayList<PatternPage>();

    public UrlMatcherImpl(JsonNode urlMappings) {
        for (JsonNode urlMapping : urlMappings) {
            final JsonNode pageComponents = urlMapping.path("page").path("pageComponents");
            List<ComponentUrl> componentUrls = new ArrayList();
            for (JsonNode pageComponent : pageComponents) {
                try {
                    componentUrls.add(new ComponentUrl(pageComponent.path("url").asText(), pageComponent.path("acceptPost").asBoolean(),
                            pageComponent.path("template").asBoolean(), new ObjectMapper().readValue(pageComponent.path("substitutionVariables"), Map.class)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            final PatternPage patternPage = new PatternPage(urlMapping.path("pattern").asText(),
                    urlMapping.path("matchOrder").asInt(), componentUrls);
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
                Logger.debug("Path: " + path + " matches pattern: " + patternPage.pattern.pattern() + ". " + patternPage.urls);
                return createPage(patternPage, matcher);
            }
        }
        return null;
    }

    private Page createPage(PatternPage patternPage, Matcher matcher) {
        List<ComponentUrl> realUrls = resolveUrlsFromExpressions(matcher, patternPage.urls);
        return new Page(realUrls);
    }

    private List<ComponentUrl> resolveUrlsFromExpressions(Matcher matcher, final List<ComponentUrl> urls) {
        ArrayList<ComponentUrl> results = new ArrayList<ComponentUrl>(urls.size());
        for (ComponentUrl componentUrl : urls) {
            results.add(new ComponentUrl(componentUrl.getUrl().replace("{0}", matcher.group(0)), componentUrl.isAcceptPost(), componentUrl.isTemplate(), componentUrl.getSubstitutionVariables()));
        }
        return results;
    }

    private class PatternPage {
        private final Pattern pattern;
        private int matchOrder;
        private final List<ComponentUrl> urls;

        public PatternPage(String pattern, int matchOrder, List<ComponentUrl> urls) {
            this.matchOrder = matchOrder;
            this.pattern = Pattern.compile(pattern);
            this.urls = urls;
        }
    }
}
