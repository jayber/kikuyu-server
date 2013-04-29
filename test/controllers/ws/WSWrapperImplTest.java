package controllers.ws;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.libs.WS;

import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WSWrapperImpl.class)
public class WSWrapperImplTest {
    @Test
    public void testUrl() throws Exception {
        final WSWrapperImpl wsWrapper = new WSWrapperImpl();

        PowerMockito.mockStatic(WS.class);
        wsWrapper.url("test");

        verifyStatic();
        WS.url("test");
    }
}
