package domain;

import java.util.List;

public class Page {
    private List<String> componentUrls;

    public Page(List componentUrls) {
        this.componentUrls = componentUrls;
    }

    public List<String> getComponentUrls() {
        return componentUrls;
    }
}
