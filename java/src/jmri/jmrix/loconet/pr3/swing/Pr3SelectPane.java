package jmri.jmrix.loconet.pr3.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for downloading software updates to PRICOM products
 *
 * @author Bob Jacobsen Copyright (C) 2005
 */
public class Pr3SelectPane extends jmri.jmrix.loconet.swing.LnPanel implements LocoNetListener {

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.pr3.swing.Pr3Select"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemPr3ModeSelect"));
    }

    public Pr3SelectPane() {

        // first build GUI
        setLayout(new FlowLayout());

        JButton b = new JButton(Bundle.getMessage("ButtonPr2Mode"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                selectPR2mode();
            }
        });
        add(b);

        b = new JButton(Bundle.getMessage("ButtonMs100Mode"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                selectMS100mode();
            }
        });
        add(b);
        add(status);

    }

    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        // listen for LocoNet messages
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        } else {
            log.error("No LocoNet connection available, can't function");
        }

        // request status
        LocoNetMessage msg = new LocoNetMessage(2);
        msg.setOpCode(LnConstants.OPC_GPBUSY);
        memo.getLnTrafficController().sendLocoNetMessage(msg);
    }

    JLabel status = new JLabel(Bundle.getMessage("StatusUnknown"));

    void selectPR2mode() {
        // set to PR2 mode
        status.setText(Bundle.getMessage("StatusPr2"));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(0xD3);
        msg.setElement(1, 0x10);
        msg.setElement(2, 1);  // set PR2
        msg.setElement(3, 0);
        msg.setElement(4, 0);
        memo.getLnTrafficController().sendLocoNetMessage(msg);
    }

    void selectMS100mode() {
        // set to MS100 mode
        status.setText(Bundle.getMessage("StatusMs100"));
        LocoNetMessage msg = new LocoNetMessage(6);
        msg.setOpCode(0xD3);
        msg.setElement(1, 0x10);
        msg.setElement(2, 0);  // set MS100
        msg.setElement(3, 0);
        msg.setElement(4, 0);
        memo.getLnTrafficController().sendLocoNetMessage(msg);
    }

    @Override
    public void message(LocoNetMessage msg) {
        if ((msg.getOpCode() == LnConstants.OPC_PEER_XFER)
                && (msg.getElement(1) == 0x10)
                && (msg.getElement(2) == 0x22)
                && (msg.getElement(3) == 0x22)
                && (msg.getElement(4) == 0x01)) {  // Digitrax form, check PR2/PR3 or MS100/PR3 mode
            int mode = msg.getElement(8) & 0x0C;
            if (mode == 0x00) {
                // PR2 format
                status.setText(Bundle.getMessage("StatusPr2"));
            } else {
                // MS100 format
                status.setText(Bundle.getMessage("StatusMs100"));
            }
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.loconet.swing.LnNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemPr3ModeSelect"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    Pr3SelectPane.class.getName(),
                    jmri.InstanceManager.getDefault(LocoNetSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Pr3SelectPane.class);

}
