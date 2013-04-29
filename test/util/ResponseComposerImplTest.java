package util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

//todo: this is seriously UNDER tested!!!
public class ResponseComposerImplTest {

    @Test
    public void testComposeBody() throws Exception {
        final ResponseComposerImpl responseComposer = new ResponseComposerImpl();
        final String output = responseComposer.composeBody("before <div location> </div> after", "component");

        assertEquals("before component after", output);
    }
}
