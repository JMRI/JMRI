package jmri.jmrit.operations.rollingstock.engines.tools;

import jmri.jmrit.operations.rollingstock.engines.tools.NceConsistEngines;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class NceConsistEnginesTest {

    @Test
    public void testCTor(){
       // this class currently requires an NCE traffic controller to function.
       jmri.jmrix.nce.NceTrafficController tc = new jmri.jmrix.nce.NceTrafficControlScaffold();
       Assert.assertNotNull("NceConsistEngines exists",new NceConsistEngines(tc));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
       JUnitUtil.tearDown();
    }


}
