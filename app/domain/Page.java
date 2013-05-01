package domain;

import java.util.List;

public class Page {
    private List<ComponentUrl> componentUrls;

    public Page(List<ComponentUrl> componentUrls) {
        this.componentUrls = componentUrls;
    }

    public List<ComponentUrl> getComponentUrls() {
        return componentUrls;
    }
}
