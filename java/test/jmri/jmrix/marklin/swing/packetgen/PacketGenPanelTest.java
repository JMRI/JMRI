package jmri.jmrix.marklin.swing.packetgen;

import javax.swing.JTextField;

import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficControlScaffold;

import jmri.util.ThreadingUtil;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of PacketGenPanel
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2024
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class PacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    @Test
    public void testSendPacket() {
        MarklinTrafficControlScaffold tc = new MarklinTrafficControlScaffold();
        MarklinSystemConnectionMemo memo = new MarklinSystemConnectionMemo(tc);

        JmriJFrame f = new JmriJFrame();
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> panel.initContext(memo) ));
        assertDoesNotThrow( () -> ThreadingUtil.runOnGUI( () -> panel.initComponents() ));

        ThreadingUtil.runOnGUI( () -> {
            f.add(panel);
            // set title if available
            if (panel.getTitle() != null) {
                f.setTitle(panel.getTitle());
            }
            f.pack();
            f.setVisible(true);
        });
        JFrameOperator jfo = new JFrameOperator(panel.getTitle());
        Assertions.assertNotNull(jfo);

        ((PacketGenPanel)panel).reply(new jmri.jmrix.marklin.MarklinReply());
        ((PacketGenPanel)panel).message(jmri.jmrix.marklin.MarklinMessage.setLocoEmergencyStop(1));
        JUnitUtil.waitFor(() -> "0x0".equals(getReplyText(jfo)), "Reply text was "+getReplyText(jfo));

        sendText("0x01", jfo);
        JUnitUtil.waitFor( () -> !tc.getSentMessages().isEmpty(), "message sent ok to tc");
        var lastMsg = tc.getLastMessageSent();
        Assertions.assertNotNull(lastMsg);
        Assertions.assertEquals("01 00 00 00 00 00 00 00 00 00 00 00 00", lastMsg.toString());

        panel.dispose();
        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();
        tc.dispose();
        memo.dispose();
    }

    private String getReplyText( JFrameOperator jfo) {
        JLabelOperator jlo = new JLabelOperator(jfo, Bundle.getMessage("ReplyLabel"));
        return new JTextFieldOperator(((JTextField) jlo.getLabelFor())).getText();
    }

    private void sendText( String valueToSend, JFrameOperator jfo ) {
        JLabelOperator jlo = new JLabelOperator(jfo, Bundle.getMessage("CommandLabel"));
        new JTextFieldOperator(((JTextField) jlo.getLabelFor())).typeText(valueToSend);
        new JButtonOperator(jfo, 0).doClick();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        panel = new PacketGenPanel();
        helpTarget="package.jmri.jmrix.marklin.swing.packetgen.PacketGenFrame";
        title=Bundle.getMessage("SendCommandTitle");
    }

    @AfterEach
    @Override
    public void tearDown() {
        panel = null;
        JUnitUtil.tearDown();
    }

}
