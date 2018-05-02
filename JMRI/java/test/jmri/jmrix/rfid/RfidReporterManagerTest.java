package jmri.jmrix.rfid;

import jmri.Reporter;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * RfidReporterManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class RfidReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    RfidTrafficController tc = null;

    @Override
    public String getSystemName(String i) {
        return "RR" + i;
    }


    @Test
    @Override
    @Ignore("Abstract Class under test, test does not apply")
    public void testSingleObject(){
    }

    @Test
    @Override
    @Ignore("Abstract Class under test, test does not apply")
    public void testDefaultSystemName(){
    }

    @Test
    @Override
    @Ignore("Abstract Class under test, test does not apply")
    public void testReporterProvideReporter(){
    }

    @Test
    @Override
    @Ignore("Abstract Class under test, test does not apply")
    public void testReporterGetByDisplayName(){
    }

    @Test
    @Override
    @Ignore("Abstract Class under test, test does not apply")
    public void testReporterGetBySystemName(){
    }

    @Test
    @Override
    @Ignore("Abstract Class under test, test does not apply")
    public void testReporterGetByUserName(){
    }

    @Test
    @Override
    @Ignore("Abstract Class under test, test does not apply")
    public void testRename(){
    }

    @Test
    @Override
    @Ignore("Abstract Class under test, test does not apply")
    public void testUpperLower(){
    }

    @Override
    @Test
    @Ignore("Not supported by this manager at this time")
    public void testReporterProvideByNumber() {
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new RfidTrafficController(){
           @Override
           public void sendInitString(){
           }
        };
        l = new RfidReporterManager("R"){
            @Override
            protected Reporter createNewReporter(String systemName, String userName){
               return null;
            }
            @Override
            public void message(RfidMessage m){}

            @Override
            public void reply(RfidReply m){}

        };
    }

    @After
    public void tearDown() {
        tc = null;
        JUnitUtil.tearDown();
    }

}
