package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Page;
import domain.PageComponent;
import play.Logger;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlMatcherImpl implements UrlMatcher {
    private ArrayList<PatternPage> patterns = new ArrayList<PatternPage>();

    public UrlMatcherImpl(JsonNode urlMappings) {
        for (JsonNode urlMapping : urlMappings) {
            final JsonNode jsonPageComponents = urlMapping.path("page").path("pageComponents");
            List<PageComponent> componentUrls = new ArrayList();
            for (JsonNode jsonPageComponent : jsonPageComponents) {
                try {
                    componentUrls.add(new PageComponent(jsonPageComponent.path("url").asText(), jsonPageComponent.path("acceptPost").asBoolean(),
                            jsonPageComponent.path("template").asBoolean(), new ObjectMapper().treeToValue(jsonPageComponent.path("substitutionVariables"), Map.class)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            final PatternPage patternPage = new PatternPage(urlMapping.path("pattern").asText(),
                    urlMapping.path("matchOrder").asInt(), urlMapping.path("page").path("name").asText(), componentUrls);
            patterns.add(patternPage);
        }
        Collections.sort(patterns, new Comparator<PatternPage>() {
            @Override
            public int compare(PatternPage patternPage1, PatternPage patternPage2) {
                return patternPage1.matchOrder - patternPage2.matchOrder;
            }
        });
    }

    @Override
    public Page match(String path) {
        for (PatternPage patternPage : patterns) {
            final Matcher matcher = patternPage.pattern.matcher(path);
            if (matcher.matches()) {
                Logger.debug("Path: " + path + " matches pattern: " + patternPage.pattern.pattern() + " - page: " + patternPage.name);
                return createPage(patternPage, matcher);
            }
        }
        return null;
    }

    private Page createPage(PatternPage patternPage, Matcher matcher) {
        List<PageComponent> realUrls = resolveUrlsFromExpressions(matcher, patternPage.urls);
        return new Page(realUrls);
    }

    private List<PageComponent> resolveUrlsFromExpressions(Matcher matcher, final List<PageComponent> urls) {
        ArrayList<PageComponent> results = new ArrayList<PageComponent>(urls.size());
        for (PageComponent pageComponent : urls) {
            results.add(new PageComponent(pageComponent.getUrl().replace("{0}", matcher.group(0)), pageComponent.isAcceptPost(), pageComponent.isTemplate(), pageComponent.getSubstitutionVariables()));
        }
        return results;
    }

    private class PatternPage {
        private final Pattern pattern;
        private int matchOrder;
        private String name;
        private final List<PageComponent> urls;

        public PatternPage(String pattern, int matchOrder, String name, List<PageComponent> urls) {
            this.matchOrder = matchOrder;
            this.name = name;
            this.pattern = Pattern.compile(pattern);
            this.urls = urls;
        }
    }
}
