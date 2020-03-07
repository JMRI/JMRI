package jmri.jmrix.acela;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AcelaLightTest.java
 *
 * Description: tests for the AcelaLight class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaLightTest {

    private AcelaSystemConnectionMemo _memo = null;
    private AcelaLightManager l = null;
    private AcelaTrafficControlScaffold tcis = null;


    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaLight Constructor", new AcelaLight("AL2", _memo ));
    }

    @Test
    public void testUserNameCtor(){
      Assert.assertNotNull("AcelaLight Constructor",new AcelaLight("AL2","Test Turnout", _memo ));
    }

   // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new AcelaTrafficControlScaffold();
        _memo = new jmri.jmrix.acela.AcelaSystemConnectionMemo(tcis);
        // create and register the manager object
        l = new AcelaLightManager(_memo);
        jmri.InstanceManager.setLightManager(l);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
