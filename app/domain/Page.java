package domain;

public class Page {
    private String templateUrl;
    private String componentUrl;

    public Page(String realTemplateUrl, String realComponentUrl) {
        templateUrl = realTemplateUrl;
        componentUrl = realComponentUrl;
    }

    public String getTemplateUrl() {
        return templateUrl;
    }

    public String getComponentUrl() {
        return componentUrl;
    }
}
