package apps;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * Description: Tests for the JmriFaceless application.
 *
 * @author  Paul Bender Copyright (C) 2016
 */
public class JmriFacelessTest {

    @Test
    @Ignore("Causes exception on CI engines, works locally")
    public void testCtor() {
        String Args[]={};
        AppsBase a = new JmriFaceless(Args);
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
