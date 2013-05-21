package util;

import domain.Page;
import domain.PageComponent;

import java.util.ArrayList;
import java.util.HashMap;

public class TestFixtures {

    public static final ArrayList<PageComponent> PAGE_COMPONENTs = new ArrayList<PageComponent>();

    static {
        PAGE_COMPONENTs.add(new PageComponent("comp1 url", false, true, new HashMap()));
        PAGE_COMPONENTs.add(new PageComponent("comp2 url", false, false, new HashMap()));
        PAGE_COMPONENTs.add(new PageComponent("comp3 url", false, false, new HashMap()));
    }

    public static final Page page = new Page(PAGE_COMPONENTs);
}