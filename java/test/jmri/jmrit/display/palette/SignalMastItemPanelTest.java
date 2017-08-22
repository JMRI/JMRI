package jmri.jmrit.display.palette;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import jmri.SignalMast;
import jmri.util.JmriJFrame;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SignalMastItemPanelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PickListModel tableModel = PickListModel.signalMastPickModelInstance(); // N11N
        JmriJFrame jf = new JmriJFrame("SignalMast Item Panel Test");
        Editor editor = new EditorScaffold();
        SignalMastItemPanel t = new SignalMastItemPanel(jf,"IM01","",tableModel,editor);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastItemPanelTest.class.getName());

}
