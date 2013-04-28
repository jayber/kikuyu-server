package util;

import domain.Page;

public interface UrlMatcher {
    Page match(String path);
}
