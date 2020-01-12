package jmri;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the NamedBeanHandleManager class
 *
 * @author	Kevin Dickerson Copyright (C) 2006
 * 
 */
public class NamedBeanHandleManagerTest {

    @Test
    public void testCreate() {
    }

    @Test
    public void testNameBeanManager() throws JmriException {
        SensorManager sm = jmri.InstanceManager.sensorManagerInstance();
        TurnoutManager tm = jmri.InstanceManager.turnoutManagerInstance();
        MemoryManager mm = jmri.InstanceManager.memoryManagerInstance();

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

        Assert.assertTrue("Sensor NamedBean1 should equal Sensor 1", ns1.getBean() == s1);
        Assert.assertTrue("Sensor NamedBean2 should equal Sensor 2", ns2.getBean() == s2);

        Assert.assertTrue("Turnout NamedBean1 should equal Turnout 1", nt1.getBean() == t1);
        Assert.assertTrue("Turnout NamedBean2 should equal Turnout 2", nt2.getBean() == t2);

        Assert.assertTrue("Memory NamedBean1 should equal Memory 1", nm1.getBean() == m1);
        Assert.assertTrue("Memory NamedBean2 should equal Memory 2", nm2.getBean() == m2);

        s1.setUserName(name);
        nbhm.updateBeanFromSystemToUser(s1);

        Assert.assertTrue("Sensor NamedBean1 should have a the user name set against it " + name, ns1.getName().equals(name));
        Assert.assertTrue("Sensor NamedBean2 should have a the system name IS2 set against it ", ns2.getName().equals("IS2"));

        Assert.assertTrue("Turnout NamedBean1 should have a the system name IT1 set against it ", nt1.getName().equals("IT1"));
        Assert.assertTrue("Turnout NamedBean2 should have a the system name IT2 set against it ", nt2.getName().equals("IT2"));

        Assert.assertTrue("Memory NamedBean1 should have a the system name IM1 set against it ", nm1.getName().equals("IM1"));
        Assert.assertTrue("Memory NamedBean2 should have a the system name IM2 set against it ", nm2.getName().equals("IM2"));

        m1.setUserName(name);
        nbhm.updateBeanFromSystemToUser(m1);

        Assert.assertTrue("Memory NamedBean1 should have a the user name set against it " + name, nm1.getName().equals(name));

        s1.setUserName(null);
        s2.setUserName(name);
        nbhm.moveBean(s1, s2, name);

        Assert.assertTrue("Sensor NamedBean1 should both sensor 2", ns1.getBean() == s2);
        Assert.assertTrue("Sensor NamedBean2 should both sensor 2", ns2.getBean() == s2);
        Assert.assertTrue("Sensor NamedBean1 should have a the user name set against it " + name, ns1.getName().equals(name));
        Assert.assertTrue("Sensor NamedBean2 should have a the system name IS2 set against it ", ns2.getName().equals("IS2"));
        Assert.assertTrue("Memory NamedBean1 should have a the user name set against it " + name, nm1.getName().equals(name));

        s2.setUserName("NewName");
        nbhm.renameBean(name, "NewName", s2);
        Assert.assertTrue("Sensor NamedBean1 should have a the user name set against it NewName", ns1.getName().equals("NewName"));
        Assert.assertTrue("Sensor NamedBean2 should have a the system name IS2 set against it ", ns2.getName().equals("IS2"));
        Assert.assertTrue("Memory NamedBean1 should have a the user name set against it " + name, nm1.getName().equals(name));

        NamedBeanHandle<Sensor> checkRename = nbhm.getNamedBeanHandle("ISno_user_name", sm.provideSensor("ISno_user_name"));
        nbhm.updateBeanFromUserToSystem(checkRename.getBean());
        jmri.util.JUnitAppender.assertWarnMessage("updateBeanFromUserToSystem requires non-blank user name: \"ISno_user_name\" not renamed");
    }

    jmri.NamedBeanHandleManager nbhm;

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
