package util;

import domain.ComponentUrl;
import domain.Page;

import java.util.ArrayList;
import java.util.HashMap;

public class TestFixtures {

    public static final ArrayList<ComponentUrl> componentUrls = new ArrayList<ComponentUrl>();

    static {
        componentUrls.add(new ComponentUrl("comp1 url", false, true, new HashMap()));
        componentUrls.add(new ComponentUrl("comp2 url", false, false, new HashMap()));
        componentUrls.add(new ComponentUrl("comp3 url", false, false, new HashMap()));
    }

    public static final Page page = new Page(componentUrls);
}