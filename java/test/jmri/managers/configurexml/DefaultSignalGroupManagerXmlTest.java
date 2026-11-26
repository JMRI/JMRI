package jmri.managers.configurexml;

import java.util.List;

import jmri.InstanceManager;
import jmri.SignalGroup;
import jmri.SignalGroupManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.implementation.VirtualSignalHead;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultSignalGroupManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultSignalGroupManagerXml t = new DefaultSignalGroupManagerXml();
        Assert.assertNotNull("exists",t);
    }

    /**
     * OR (true) has always been stored as "AND", and AND (false) as "OR".
     * Files saved by earlier versions must keep their meaning, so this
     * stays: each setting is stored that way and loads back as it was.
     */
    @Test
    public void testAndOrStoredAsBeforeAndLoadedBack() {
        SignalGroupManager sgm = InstanceManager.getDefault(SignalGroupManager.class);
        SignalGroup g = sgm.provideSignalGroup("IG1", null);
        SignalMast mast = Mockito.mock(SignalMast.class);
        g.setSignalMast(mast, "IF$mast");
        SignalHead or = new VirtualSignalHead("IH1");
        SignalHead and = new VirtualSignalHead("IH2");
        InstanceManager.getDefault(SignalHeadManager.class).register(or);
        InstanceManager.getDefault(SignalHeadManager.class).register(and);
        g.addSignalHead(or);
        g.setSensorTurnoutOper(or, true);
        g.addSignalHead(and);
        g.setSensorTurnoutOper(and, false);

        DefaultSignalGroupManagerXml x = new DefaultSignalGroupManagerXml();
        Element e = x.store(sgm);
        Element group = e.getChild("signalgroup");
        List<Element> heads = group.getChildren("signalHead");
        Assert.assertEquals("OR stored as", "AND", heads.get(0).getAttributeValue("sensorTurnoutLogic"));
        Assert.assertEquals("AND stored as", "OR", heads.get(1).getAttributeValue("sensorTurnoutLogic"));

        group.getChild("systemName").setText("IG2");
        x.load(e, null);
        JUnitAppender.assertWarnMessage("did not find a Signal Mast named IF$mast");
        SignalGroup loaded = sgm.getBySystemName("IG2");
        Assert.assertNotNull("loaded", loaded);
        Assert.assertTrue("OR loads as OR", loaded.getSensorTurnoutOper(or));
        Assert.assertFalse("AND loads as AND", loaded.getSensorTurnoutOper(and));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(DefaultSignalGroupManagerXmlTest.class);

}
