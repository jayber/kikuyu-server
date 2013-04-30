package util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

//todo: this is seriously UNDER tested!!!
public class ResponseComposerImplTest {

    @Test
    public void testComposeBody() throws Exception {
        final ResponseComposerImpl responseComposer = new ResponseComposerImpl();
        final String output = responseComposer.composeBody("before <div location> </div> middle <div location> </div> after", "component1", "component2");

        assertEquals("before component1 middle component2 after", output);
    }
}
