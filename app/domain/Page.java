package domain;

import java.util.List;

public class Page {
    private List<PageComponent> pageComponents;

    public Page(List<PageComponent> pageComponents) {
        this.pageComponents = pageComponents;
    }

    public List<PageComponent> getPageComponents() {
        return pageComponents;
    }
}
