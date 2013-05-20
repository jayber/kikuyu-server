package util;

import domain.Page;
import domain.PageComponent;
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
        ArrayList<PageComponent> pageComponents = new ArrayList<PageComponent>();

        pageComponents.add(new PageComponent("comp1 url", false, true, new HashMap()));
        pageComponents.add(new PageComponent("comp2 url", false, true, new HashMap()));
        pageComponents.add(new PageComponent("comp3 url", false, false, new HashMap()));
        pageComponents.add(new PageComponent("comp4 url", false, false, new HashMap()));
        pageComponents.add(new PageComponent("comp5 url", false, false, new HashMap()));

        final String output = responseComposer.composeBody(new Page(pageComponents),
                "before <div location> </div> middle <div location> </div> after",
                "before2 <div location> </div> middle2 <div location> </div> after2",
                "component3",
                "component4",
                "component5");

        assertEquals("before before2 component3 middle2 component4 after2 middle component5 after", output);
    }

    @Test
    public void testSubstitutionVariables() throws Exception {
        final HashMap<String, String> substitutionVariables = new HashMap<>();
        substitutionVariables.put("var1", "value1");
        substitutionVariables.put("var2", "value2");
        final PageComponent pageComponent = new PageComponent("comp1 url", false, false, substitutionVariables);
        final ArrayList<PageComponent> pageComponents = new ArrayList<>();
        pageComponents.add(pageComponent);

        final ResponseComposerImpl responseComposer = new ResponseComposerImpl();
        final String output = responseComposer.composeBody(new Page(pageComponents), "before #{var1} middle #{var2} after #{var3}");

        assertEquals("before value1 middle value2 after ", output);

    }
}
