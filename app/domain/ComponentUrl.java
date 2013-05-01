package domain;

public class ComponentUrl {
    private String url;
    private boolean acceptPost;

    public ComponentUrl(String url, boolean acceptPost) {
        this.url = url;
        this.acceptPost = acceptPost;
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
}
