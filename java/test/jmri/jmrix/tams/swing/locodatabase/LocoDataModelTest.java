package jmri.jmrix.tams.swing.locodatabase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.tams.TamsSystemConnectionMemo;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoDataModelTest {

    @Test
    public void testCTor() {
        TamsSystemConnectionMemo memo = new TamsSystemConnectionMemo();
        LocoDataModel t = new LocoDataModel(128,16,memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LocoDataModelTest.class.getName());

}
