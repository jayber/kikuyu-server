package util;

import domain.ComponentUrl;
import domain.Page;

import java.util.ArrayList;

public class TestFixtures {

    public static final ArrayList<ComponentUrl> componentUrls = new ArrayList<ComponentUrl>();

    {
        componentUrls.add(new ComponentUrl("comp1 url", false, true));
        componentUrls.add(new ComponentUrl("comp2 url", false, false));
        componentUrls.add(new ComponentUrl("comp3 url", false, false));
    }

    public static final Page page = new Page(componentUrls);
}