package util;

import domain.ComponentUrl;
import domain.Page;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

//todo: this is seriously UNDER tested!!!
public class ResponseComposerImplTest {

    @Test
    public void testComposeBody() throws Exception {
        final ResponseComposerImpl responseComposer = new ResponseComposerImpl();
        final String output = responseComposer.composeBody(TestFixtures.page, "before <div location> </div> middle <div location> </div> after", "component1", "component2");

        assertEquals("before component1 middle component2 after", output);
    }

    @Test
    public void testComposeBody2Templates() throws Exception {
        final ResponseComposerImpl responseComposer = new ResponseComposerImpl();
        ArrayList<ComponentUrl> componentUrls = new ArrayList<ComponentUrl>();

        componentUrls.add(new ComponentUrl("comp1 url", false, true, new HashMap()));
        componentUrls.add(new ComponentUrl("comp2 url", false, true, new HashMap()));
        componentUrls.add(new ComponentUrl("comp3 url", false, false, new HashMap()));
        componentUrls.add(new ComponentUrl("comp4 url", false, false, new HashMap()));
        componentUrls.add(new ComponentUrl("comp5 url", false, false, new HashMap()));

        final String output = responseComposer.composeBody(new Page(componentUrls),
                "before <div location> </div> middle <div location> </div> after",
                "before2 <div location> </div> middle2 <div location> </div> after2",
                "component3",
                "component4",
                "component5");

        assertEquals("before before2 component3 middle2 component4 after2 middle component5 after", output);
    }
}
