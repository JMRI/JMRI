package jmri.jmrix.powerline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrix.powerline.simulator.SpecificSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import java.beans.PropertyVetoException;

import org.junit.jupiter.api.*;

/**
 * SerialTurnoutManagerTest.java
 *
 * Test for the SerialTurnoutManager class
 *
 * @author Bob Jacobsen Copyright 2004, 2008 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private SerialTrafficControlScaffold nis = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        SpecificSystemConnectionMemo memo = new SpecificSystemConnectionMemo();
        // prepare an interface, register
        nis = new SerialTrafficControlScaffold();
        nis.setAdapterMemo(memo);
        memo.setTrafficController(nis);
        memo.setSerialAddress(new SerialAddress(memo));
        // create and register the manager object
        l = new SerialTurnoutManager(nis);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "PTB" + n;
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return "B2";
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("PTB1", "my name");
        assertNotNull( o );
        assertInstanceOf( SerialTurnout.class, o);

        // make sure loaded into tables
        assertNotNull( l.getBySystemName("PTB1"));
        assertNotNull( l.getByUserName("my name"));

    }

    @Override
    @Test
    public void testProvideName() {
        // create
        Turnout t = l.provide(getSystemName(getNumToTest1()));
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout(getSystemName(getNumToTest1()));
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Override
    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout(getSystemName(getNumToTest2()));

        assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }

    @Override
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String s1 = l.makeSystemName("B1");
        String s2 = l.makeSystemName("B2");
        testRegisterDuplicateSystemName(l, s1, s2);
    }

    @Override
    @Test
    public void testMakeSystemName() {
        NamedBean.BadSystemNameException ex = assertThrows(NamedBean.BadSystemNameException.class,
            () -> l.makeSystemName("1"),
            "Expected exception not thrown");
        assertEquals("\"PT1\" is not a recognized format.", ex.getMessage());
        JUnitAppender.assertErrorMessage("Invalid system name for Turnout: \"PT1\" is not a recognized format.");
        String s = l.makeSystemName("B1");
        assertNotNull(s);
        assertFalse(s.isEmpty());
    }

    @AfterEach
    public void tearDown() {
        nis.terminateThreads();
        nis = null;
        JUnitUtil.tearDown();

    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
