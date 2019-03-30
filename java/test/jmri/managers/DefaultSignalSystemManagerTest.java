package jmri.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import jmri.SignalSystem;
import jmri.implementation.SignalSystemTestUtil;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.managers.DefaultSignalSystemManager class.
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class DefaultSignalSystemManagerTest {

    @Test
    public void testGetListOfNames() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        java.util.List<String> l = d.getListOfNames();
        Assert.assertTrue(l.contains("basic"));
        Assert.assertTrue(l.contains("AAR-1946"));
        Assert.assertTrue(l.contains("SPTCO-1960"));
    }

    @Test
    public void testSearchOrder() throws Exception {
        try {  // need try-finally to ensure junk deleted from user area
            SignalSystemTestUtil.createMockSystem();

            // check that mock (test directory) system is present
            DefaultSignalSystemManager d = new DefaultSignalSystemManager();
            java.util.List<String> l = d.getListOfNames();
            Assert.assertTrue(l.contains(SignalSystemTestUtil.getMockSystemName()));

        } finally {
            SignalSystemTestUtil.deleteMockSystem();
        }
    }

    @Test
    public void testLoadBasicAspects() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        d.makeBean("basic");
    }

    @Test
    public void testLoad() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();

        // Remove all beans in the manager
        Set<SignalSystem> set = new HashSet<>(d.getNamedBeanSet());
        set.forEach((b) -> {
            d.deregister(b);
        });

        d.load();
        Assert.assertTrue(d.getSystemNameList().size() >= 2);
    }

    @Test
    public void testUniqueNames() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        java.util.List<String> l = d.getListOfNames();
        for (int i = 0; i < l.size(); i++) {
            for (int j = 0; j < l.size(); j++) {
                if ((i != j) && (l.get(i).equals(l.get(j)))) {
                    Assert.fail("Found " + l.get(i) + " at " + i + " and " + j);
                }
            }
        }
    }

    @Test
    public void testUniqueSystemNames() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        java.util.List<String> l = d.getListOfNames();
        for (int i = 0; i < l.size(); i++) {
            jmri.SignalSystem si = d.getSystem(l.get(i));
            for (int j = 0; j < l.size(); j++) {
                jmri.SignalSystem sj = d.getSystem(l.get(j));
                if ((i != j) && (si.getSystemName().equals(sj.getSystemName()))) {
                    Assert.fail("Found system name " + si.getSystemName() + " at " + i + " and " + j);
                }
            }
        }
    }

    @Test
    public void testUniqueUserNames() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        java.util.List<String> l = d.getListOfNames();
        for (int i = 0; i < l.size(); i++) {
            jmri.SignalSystem si = d.getSystem(l.get(i));
            for (int j = 0; j < l.size(); j++) {
                jmri.SignalSystem sj = d.getSystem(l.get(j));
                if ((i != j) && (si.getUserName().equals(sj.getUserName()))) {
                    Assert.fail("Found user name " + si.getUserName() + " at " + i + " and " + j);
                }
            }
        }
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
