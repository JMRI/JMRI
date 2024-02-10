package jmri.jmrit.signalling;

import java.util.Locale;

import javax.swing.UIManager;

import jmri.InstanceManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class SignallingSourcePanelTest {

    @Test
    public void testCTor() {
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        SignallingSourcePanel t = new SignallingSourcePanel(new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)"));
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testEnableBlockRoutingDialogDisplays(){

        InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(false);

        SignallingSourcePanel t = new SignallingSourcePanel(new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)"));
        Assert.assertNotNull("exists",t);
        javax.swing.JFrame f = new javax.swing.JFrame("testEnableBlockRoutingDialogDisplays");
        f.getContentPane().add(t);
        f.pack();
        jmri.util.ThreadingUtil.runOnGUI( () -> f.setVisible(true));

        JFrameOperator jfo = new JFrameOperator(f.getTitle());

        // click discover button.
        // click no in dialog to NOT enable advanced routing.
        // click OK to acknowledge no LE panel.

        JButtonOperator jbo = new JButtonOperator(jfo,Bundle.getMessage("ButtonDiscover"));
        Thread discoverDialogThread = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"));
        Thread noLayoutEditorDialogThread = JemmyUtil.createModalDialogOperatorThread(
            UIManager.getString("OptionPane.messageDialogTitle", Locale.getDefault()), 
                Bundle.getMessage("ButtonOK"));

        jbo.doClick();
        JUnitUtil.waitFor( () -> !discoverDialogThread.isAlive(), "Discover dialog finished");
        JUnitUtil.waitFor( () -> !noLayoutEditorDialogThread.isAlive(), "No LE dialog finished");

        jfo.requestClose();
        jfo.waitClosed();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultSignalMastManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignallingSourcePanelTest.class);

}
