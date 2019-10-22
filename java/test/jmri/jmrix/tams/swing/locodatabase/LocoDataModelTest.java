package jmri.jmrix.tams.swing.locodatabase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.TamsInterfaceScaffold;
import jmri.jmrix.tams.TamsTrafficController;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoDataModelTest {

    private TamsSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        LocoDataModel t = new LocoDataModel(128,16,memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        TamsTrafficController tc = new TamsInterfaceScaffold();
        memo = new TamsSystemConnectionMemo(tc);
    }

    @After
    public void tearDown() {
        memo = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoDataModelTest.class.getName());

}
