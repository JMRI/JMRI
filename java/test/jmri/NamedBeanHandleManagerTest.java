package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the NamedBeanHandleManager class
 *
 * @author Kevin Dickerson Copyright (C) 2006
 * 
 */
public class NamedBeanHandleManagerTest {

    @Test
    public void testNameBeanManager() throws JmriException {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        MemoryManager mm = InstanceManager.getDefault(MemoryManager.class);
        assertNotNull(nbhm);

        String name = "MyUserName";
        Sensor s1 = sm.provideSensor("IS1");
        Sensor s2 = sm.provideSensor("IS2");
        NamedBeanHandle<Sensor> ns1 = nbhm.getNamedBeanHandle("IS1", s1);
        NamedBeanHandle<Sensor> ns2 = nbhm.getNamedBeanHandle("IS2", s2);

        Turnout t1 = tm.provideTurnout("IT1");
        Turnout t2 = tm.provideTurnout("IT2");
        NamedBeanHandle<Turnout> nt1 = nbhm.getNamedBeanHandle("IT1", t1);
        NamedBeanHandle<Turnout> nt2 = nbhm.getNamedBeanHandle("IT2", t2);

        Memory m1 = mm.provideMemory("IM1");
        Memory m2 = mm.provideMemory("IM2");
        NamedBeanHandle<Memory> nm1 = nbhm.getNamedBeanHandle("IM1", m1);
        NamedBeanHandle<Memory> nm2 = nbhm.getNamedBeanHandle("IM2", m2);

        assertTrue( ns1.getBean() == s1, "Sensor NamedBean1 should equal Sensor 1");
        assertTrue( ns2.getBean() == s2, "Sensor NamedBean2 should equal Sensor 2");

        assertTrue( nt1.getBean() == t1, "Turnout NamedBean1 should equal Turnout 1");
        assertTrue( nt2.getBean() == t2, "Turnout NamedBean2 should equal Turnout 2");

        assertTrue( nm1.getBean() == m1, "Memory NamedBean1 should equal Memory 1");
        assertTrue( nm2.getBean() == m2, "Memory NamedBean2 should equal Memory 2");

        s1.setUserName(name);
        nbhm.updateBeanFromSystemToUser(s1);

        assertTrue( ns1.getName().equals(name), "Sensor NamedBean1 should have a the user name set against it " + name);
        assertTrue( ns2.getName().equals("IS2"), "Sensor NamedBean2 should have a the system name IS2 set against it ");

        assertTrue( nt1.getName().equals("IT1"), "Turnout NamedBean1 should have a the system name IT1 set against it ");
        assertTrue( nt2.getName().equals("IT2"), "Turnout NamedBean2 should have a the system name IT2 set against it ");

        assertTrue( nm1.getName().equals("IM1"), "Memory NamedBean1 should have a the system name IM1 set against it ");
        assertTrue( nm2.getName().equals("IM2"), "Memory NamedBean2 should have a the system name IM2 set against it ");

        m1.setUserName(name);
        nbhm.updateBeanFromSystemToUser(m1);

        assertTrue( nm1.getName().equals(name), "Memory NamedBean1 should have a the user name set against it " + name);

        s1.setUserName(null);
        s2.setUserName(name);
        nbhm.moveBean(s1, s2, name);

        assertTrue( ns1.getBean() == s2, "Sensor NamedBean1 should both sensor 2");
        assertTrue( ns2.getBean() == s2, "Sensor NamedBean2 should both sensor 2");
        assertTrue( ns1.getName().equals(name), "Sensor NamedBean1 should have a the user name set against it " + name);
        assertTrue( ns2.getName().equals("IS2"), "Sensor NamedBean2 should have a the system name IS2 set against it ");
        assertTrue( nm1.getName().equals(name), "Memory NamedBean1 should have a the user name set against it " + name);

        s2.setUserName("NewName");
        nbhm.renameBean(name, "NewName", s2);
        assertTrue( ns1.getName().equals("NewName"), "Sensor NamedBean1 should have a the user name set against it NewName");
        assertTrue( ns2.getName().equals("IS2"), "Sensor NamedBean2 should have a the system name IS2 set against it ");
        assertTrue( nm1.getName().equals(name), "Memory NamedBean1 should have a the user name set against it " + name);

        NamedBeanHandle<Sensor> checkRename = nbhm.getNamedBeanHandle("ISno_user_name", sm.provideSensor("ISno_user_name"));
        nbhm.updateBeanFromUserToSystem(checkRename.getBean());
        jmri.util.JUnitAppender.assertWarnMessage("updateBeanFromUserToSystem requires non-blank user name: \"ISno_user_name\" not renamed");
    }

    private NamedBeanHandleManager nbhm = null;

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);
        nbhm = InstanceManager.getDefault(NamedBeanHandleManager.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
