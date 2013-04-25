package controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import play.mvc.Result;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/application-context.xml")
public class KikuyuControllerTest {

    @Autowired
    private KikuyuController kikuyuController;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSiphon() throws Exception {
        Result result = kikuyuController.siphon("testPath");
    }
}
