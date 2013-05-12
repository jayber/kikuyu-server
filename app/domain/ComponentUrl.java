package domain;

import java.util.Map;

public class ComponentUrl {
    private String url;
    private boolean acceptPost;
    private boolean template;
    private Map<String, String> substitutionVariables;

    public ComponentUrl(String url, boolean acceptPost, boolean template, Map<String, String> substitutionVariables) {
        this.url = url;
        this.acceptPost = acceptPost;
        this.template = template;
        this.substitutionVariables = substitutionVariables;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAcceptPost() {
        return acceptPost;
    }

    public void setAcceptPost(boolean acceptPost) {
        this.acceptPost = acceptPost;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public Map<String, String> getSubstitutionVariables() {
        return substitutionVariables;
    }
}
