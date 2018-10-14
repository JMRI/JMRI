package jmri.jmrix.grapevine.nodetable;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame lets user renumber a Grapevine node.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class RenumberFrame extends jmri.util.JmriJFrame {

    private GrapevineSystemConnectionMemo memo = null;

    /**
     * Create new RenumberFrame instance.
     *
     * @param _memo the {@link jmri.jmrix.grapevine.GrapevineSystemConnectionMemo} for this frame
     */
    public RenumberFrame(GrapevineSystemConnectionMemo _memo) {
        super();
        memo = _memo;
    }

    JSpinner fromSpinner;
    JSpinner toSpinner;
    JButton renumberButton;

    /**
     * Initialize the window.
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("WindowTitleRenumber"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        contentPane.add(p);

        p.add(new JLabel(Bundle.getMessage("LabelFrom")));
        fromSpinner = new JSpinner(new SpinnerNumberModel(1,1,127,1));
        p.add(fromSpinner);
        fromSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                numberSet();
            }
        });

        p.add(new JLabel(Bundle.getMessage("LabelTo")));
        toSpinner = new JSpinner(new SpinnerNumberModel(1,1,127,1));
        p.add(toSpinner);
        toSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                numberSet();
            }
        });

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        contentPane.add(p);

        renumberButton = new JButton(Bundle.getMessage("ButtonExec"));
        p.add(renumberButton);
        renumberButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                execute();
            }
        });
        renumberButton.setEnabled(false); // start as disabled, with old and new both set to 1

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.grapevine.nodetable.RenumberFrame", true);

        // pack for display
        pack();
    }

    /**
     * Send the message to change the address.
     */
    void execute() {
        // get addresses
        int f = (Integer) fromSpinner.getValue();
        int t = (Integer) toSpinner.getValue();
        if (f >= t) {
            return; // no use if old == new
        }
        // format the message
        SerialMessage m = new SerialMessage();
        m.setElement(0, 0x80 + (f & 0x7F));
        m.setElement(1, (t & 0x7F));
        m.setElement(2, 0x80 + (f & 0x7F));
        m.setElement(3, 0x60);
        m.setParity();
        memo.getTrafficController().sendSerialMessage(m, null);
        log.debug("RenumberFrame sent {}", 0x60);
    }

    /**
     * Check values set for old and new numbers.
     */
    void numberSet() {
        int f = (Integer) fromSpinner.getValue();
        int t = (Integer) toSpinner.getValue();
        if (f == t) {
            renumberButton.setEnabled(false);
        } else {
            renumberButton.setEnabled(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RenumberFrame.class);

}
