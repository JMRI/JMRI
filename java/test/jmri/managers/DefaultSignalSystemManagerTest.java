package jmri.managers;

import java.util.HashSet;
import java.util.Set;

import jmri.InstanceManager;
import jmri.SignalSystem;
import jmri.implementation.SignalSystemTestUtil;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.managers.DefaultSignalSystemManager class.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class DefaultSignalSystemManagerTest extends AbstractManagerTestBase<jmri.SignalSystemManager,jmri.SignalSystem> {

    @Test
    public void testGetListOfNames() {
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;
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
            DefaultSignalSystemManager d = new DefaultSignalSystemManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
            java.util.List<String> l = d.getListOfNames();
            Assert.assertTrue(l.contains(SignalSystemTestUtil.getMockSystemName()));

        } finally {
            SignalSystemTestUtil.deleteMockSystem();
        }
    }

    @Test
    public void testLoadBasicAspects() {
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;
        d.makeBean("basic");
    }

    @Test
    @SuppressWarnings("deprecation") // getSystemNameList references
    public void testLoad() {
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;

        // Remove all beans in the manager
        Set<SignalSystem> set = new HashSet<>(d.getNamedBeanSet());
        set.forEach((b) -> {
            d.deregister(b);
        });
        
        Assert.assertTrue(d.getNamedBeanSet().isEmpty());

        d.load();
        Assert.assertTrue(d.getSystemNameList().size() >= 2);
        Assert.assertTrue(d.getNamedBeanSet().size() >= 2);
        
        jmri.util.JUnitAppender.suppressWarnMessageStartsWith("getSystemNameList");
    }

    @Test
    public void testUniqueNames() {
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;
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
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;
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
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;
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
    
    // No manager-specific system name validation at present
    @Test
    @Override
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}
    
    // No manager-specific system name validation at present
    @Test
    @Override
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultSignalSystemManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

}
