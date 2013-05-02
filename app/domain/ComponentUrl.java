package domain;

public class ComponentUrl {
    private String url;
    private boolean acceptPost;
    private boolean template;

    public ComponentUrl(String url, boolean acceptPost, boolean template) {
        this.url = url;
        this.acceptPost = acceptPost;
        this.template = template;
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
}
