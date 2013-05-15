package domain;

import java.util.Map;

public class PageComponent {
    private String url;
    private boolean acceptPost;
    private boolean template;
    private Map<String, String> substitutionVariables;

    public PageComponent(String url, boolean acceptPost, boolean template, Map<String, String> substitutionVariables) {
        this.url = url;
        this.acceptPost = acceptPost;
        this.template = template;
        this.substitutionVariables = substitutionVariables;
    }

    public String getUrl() {
        return url;
    }

    public boolean isAcceptPost() {
        return acceptPost;
    }

    public boolean isTemplate() {
        return template;
    }

    public Map<String, String> getSubstitutionVariables() {
        return substitutionVariables;
    }
}
