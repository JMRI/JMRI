package jmri.jmrix.acela;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.Light;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.acela.AcelaLightManager class.
 *
 * @author Bob Coleman Copyright 2008
 */
public class AcelaLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private AcelaSystemConnectionMemo _memo = null;
    private AcelaTrafficControlScaffold tcis = null;

    @Override
    public String getSystemName(int i) {
        return "AL" + i;
    }

    @Test
    public void testConstructor(){
        AcelaLightManager alm = new AcelaLightManager(_memo);
        assertNotNull( alm, "Light Manager Creation");
    }

    @Test
    public void testAsAbstractFactory() {
        Light tl = l.newLight("AL21", "my name");

        assertNotNull( tl);
        assertInstanceOf( AcelaLight.class, tl);

        // make sure loaded into tables

        assertNotNull( l.getBySystemName("AL21"));
        assertNotNull( l.getByUserName("my name"));

    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new AcelaTrafficControlScaffold();
        _memo = new AcelaSystemConnectionMemo(tcis);
        // create and register the manager object
        l = new AcelaLightManager(_memo);
        jmri.InstanceManager.setLightManager(l);
        AcelaNode a0 = new AcelaNode(0, AcelaNode.AC,tcis);
        a0.initNode();
        AcelaNode a1 = new AcelaNode(1, AcelaNode.TB,tcis);
        a1.initNode();
        AcelaNode a2 = new AcelaNode(2, AcelaNode.D8,tcis);
        a2.initNode();
        AcelaNode a3 = new AcelaNode(3, AcelaNode.D8,tcis);
        a3.initNode();
        AcelaNode a4 = new AcelaNode(4, AcelaNode.D8,tcis);
        a4.initNode();
    }

    @AfterEach
    public void tearDown() {

        tcis.terminateThreads();
        tcis = null;
        _memo.dispose();
        _memo = null;

        //JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AcelaLightManagerTest.class);

}
