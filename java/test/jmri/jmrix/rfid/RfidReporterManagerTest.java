package jmri.jmrix.rfid;

import javax.annotation.Nonnull;
import jmri.Reporter;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.rfid.RfidReporterManager class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RfidReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    private RfidSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(String i) {
        return "RR" + i;
    }


    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testSingleObject(){
    }

    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testProvideName() {
    }

    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testDefaultSystemName(){
    }

    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testReporterProvideReporter(){
    }

    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testReporterGetByDisplayName(){
    }

    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testReporterGetBySystemName(){
    }

    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testReporterGetByUserName(){
    }

    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testRename(){
    }

    @Test
    @Override
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testUpperLower(){
    }

    @Override
    @Test
    @NotApplicable("Abstract Class under test, test does not apply")
    public void testReporterProvideByNumber() {
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RfidSystemConnectionMemo();
        l = new RfidReporterManager(memo){
            @Override
            protected Reporter createNewReporter(@Nonnull String systemName, String userName){
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
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
