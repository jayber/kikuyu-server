package util;

import domain.Page;

public interface ResponseComposer {
    String composeBody(Page page, String... body);
}
