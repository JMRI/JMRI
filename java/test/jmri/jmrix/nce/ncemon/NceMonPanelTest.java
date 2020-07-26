package jmri.jmrix.nce.ncemon;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.AbstractMonPaneScaffold;
import jmri.jmrix.nce.NceInterfaceScaffold;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

import org.assertj.swing.edt.GuiActionRunner;
import org.junit.Assume;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * JUnit tests for the NceProgrammer class
 *
 * @author Bob Jacobsen
 */
public class NceMonPanelTest extends jmri.jmrix.AbstractMonPaneTestBase {

    private NceSystemConnectionMemo memo = null;

    @Test
    @Disabled("Ignore due to timing-specific, occasionally fail")
    public void testMsg() {
        NceMessage m = new NceMessage(3);
        m.setBinary(false);
        m.setOpCode('L');
        m.setElement(1, '0');
        m.setElement(2, 'A');

        ((NceMonPanel) pane).message(m);

        // The following assertions need to be re-written.  There is no
        // current method for retrieving the text panel from the NceMonPanel.
        //Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), ((NceMonPanel)pane).getPanelText().length()); 
        //Assert.assertEquals("display", "cmd: \"L0A\"\n", ((NceMonPanel)pane).getPanelText()); 
    }

    @Test
    @Disabled("Ignore due to timing-specific, occasionally fail")
    public void testReply() {
        NceReply m = new NceReply(memo.getNceTrafficController());
        m.setBinary(false);
        m.setOpCode('C');
        m.setElement(1, 'o');
        m.setElement(2, ':');

        ((NceMonPanel) pane).reply(m);

        // The following assertions need to be re-written.  There is no
        // current method for retrieving the text panel from the NceMonPanel.
        //Assert.assertEquals("display", "rep: \"Co:\"\n", ((NceMonPanel)pane).getPanelText()); 
        //Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), ((NceMonPanel)pane).getPanelText().length());
    }

    // Test checking the AutoScroll checkbox.
    // for some reason the NceMonPane has the checkbox value reversed on
    // startup compared to other AbstractMonPane derivatives.
    @Override
    @Test
    public void checkAutoScrollCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractMonPaneScaffold s = new AbstractMonPaneScaffold(pane);

        // for Jemmy to work, we need the pane inside of a frame
        JmriJFrame f = new JmriJFrame();
        Throwable thrown = catchThrowable( () -> GuiActionRunner.execute( () ->  pane.initComponents()));
        assertThat(thrown).withFailMessage("could not load pane: {}",thrown).isNull();
        ThreadingUtil.runOnGUI( () -> {
            f.add(pane);
            // set title if available
            if (pane.getTitle() != null) {
                f.setTitle(pane.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });
        assertThat(s.getAutoScrollCheckBoxValue()).isTrue();
        s.checkAutoScrollCheckBox();
        assertThat(s.getAutoScrollCheckBoxValue()).isFalse();
        ThreadingUtil.runOnGUI( () -> {
            f.setVisible(false);
            f.dispose();
        });
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        NceInterfaceScaffold tc = new NceInterfaceScaffold();
        memo = tc.getAdapterMemo();
        // pane for AbstractMonPaneTestBase, panel for JmriPanelTest
        panel = pane = new NceMonPanel();
        ThreadingUtil.runOnGUI( () -> ((NceMonPanel) pane).initContext(memo));
        title = "NCE: Command Monitor";
    }

    @Override
    @AfterEach
    public void tearDown() {
        panel = pane = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
