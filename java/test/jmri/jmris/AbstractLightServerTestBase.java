package jmri.jmris;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Base set of tests for decendents of the jmri.jmris.AbstractLightServer class
 *
 * @author Paul Bender Copyright (C) 2017
 */
abstract public class AbstractLightServerTestBase {

    protected AbstractLightServer ls = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(ls);
    }

    @Test
    public void testInitLight(){
        ls.initLight("IL1");
    }

    @Before
    abstract public void setUp(); // must setup ls as a light server instance;

}
