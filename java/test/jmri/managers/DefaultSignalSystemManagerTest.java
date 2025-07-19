package jmri.managers;

import java.util.HashSet;
import java.util.List;
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
        List<String> list = d.getListOfNames();
        Assert.assertTrue(list.contains("basic"));
        Assert.assertTrue(list.contains("AAR-1946"));
        Assert.assertTrue(list.contains("SPTCO-1960"));
    }

    @Test
    public void testSearchOrder() throws Exception {
        try {  // need try-finally to ensure junk deleted from user area
            SignalSystemTestUtil.createMockSystem();

            // check that mock (test directory) system is present
            DefaultSignalSystemManager d = new DefaultSignalSystemManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
            List<String> list = d.getListOfNames();
            Assert.assertTrue(list.contains(SignalSystemTestUtil.getMockSystemName()));

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
    public void testLoad() {
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;

        // Remove all beans in the manager
        Set<SignalSystem> set = new HashSet<>(d.getNamedBeanSet());
        set.forEach((b) -> {
            d.deregister(b);
        });

        Assert.assertTrue(d.getNamedBeanSet().isEmpty());

        d.load();
        Assert.assertTrue(d.getNamedBeanSet().size() >= 2);

        jmri.util.JUnitAppender.suppressWarnMessageStartsWith("getSystemNameList");
    }

    @Test
    public void testUniqueNames() {
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;
        List<String> list = d.getListOfNames();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if ((i != j) && (list.get(i).equals(list.get(j)))) {
                    Assert.fail("Found " + list.get(i) + " at " + i + " and " + j);
                }
            }
        }
    }

    @Test
    public void testUniqueSystemNames() {
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;
        List<String> list = d.getListOfNames();
        for (int i = 0; i < list.size(); i++) {
            SignalSystem si = d.getSystem(list.get(i));
            Assertions.assertNotNull(si);
            for (int j = 0; j < list.size(); j++) {
                SignalSystem sj = d.getSystem(list.get(j));
                Assertions.assertNotNull(sj);
                if ((i != j) && (si.getSystemName().equals(sj.getSystemName()))) {
                    Assert.fail("Found system name " + si.getSystemName() + " at " + i + " and " + j);
                }
            }
        }
    }

    @Test
    public void testUniqueUserNames() {
        DefaultSignalSystemManager d = (DefaultSignalSystemManager)l;
        List<String> list = d.getListOfNames();
        for (int i = 0; i < list.size(); i++) {
            SignalSystem si = d.getSystem(list.get(i));
            Assertions.assertNotNull(si);
            for (int j = 0; j < list.size(); j++) {
                SignalSystem sj = d.getSystem(list.get(j));
                Assertions.assertNotNull(sj);
                String siUserName = si.getUserName();
                if ((i != j) && (siUserName != null) && (siUserName.equals(sj.getUserName()))) {
                    Assert.fail("Found user name " + si.getUserName() + " at " + i + " and " + j);
                }
            }
        }
    }

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
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
