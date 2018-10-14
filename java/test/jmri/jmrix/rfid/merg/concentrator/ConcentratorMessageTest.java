package jmri.jmrix.rfid.merg.concentrator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConcentratorMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.merge.concentrator.ConcentratorMessage class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class ConcentratorMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new ConcentratorMessage(20){
           @Override
           public String toMonitorString(){
               return "";
           }
        };
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

}
