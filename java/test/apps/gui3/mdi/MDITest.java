package apps.gui3.mdi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;


/**
 *
 * Description: Tests for the MDI application.
 *
 * @author  Paul Bender Copyright (C) 2016
 */
public class MDITest {

    @Test
    @Ignore("functions locally,fails on travis/appveyor")
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        String[] args = {"DecoderProConfig3.xml"};
        apps.AppsBase a = new MDI(args);
        Assert.assertNotNull(a);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }


}
