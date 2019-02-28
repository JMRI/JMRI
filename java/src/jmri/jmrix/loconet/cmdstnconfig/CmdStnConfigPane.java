package jmri.jmrix.loconet.cmdstnconfig;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for Command Station Option Programming.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Alex Shepherd Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class CmdStnConfigPane extends LnPanel implements LocoNetListener {

    int CONFIG_SLOT = 127;
    int MIN_OPTION = 1;
    int MAX_OPTION = 96;

    String labelT;
    String labelC;
    String labelTop;
    String read;
    String write;

    int[] oldcontent = new int[10];

    JCheckBox optionBox;

    ResourceBundle rb;
    // internal members to hold widgets
    JButton readButton;
    JButton writeButton;

    JRadioButton[] closedButtons = new JRadioButton[MAX_OPTION];
    JRadioButton[] thrownButtons = new JRadioButton[MAX_OPTION];
    JLabel[] labels = new JLabel[MAX_OPTION];
    boolean[] isReserved = new boolean[MAX_OPTION];

    /**
     * Create a new instance of a Command Station Configuration Pane
     */
    public CmdStnConfigPane() {
        super();
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        String uName = "";
        if (memo != null) {
            uName = memo.getUserName();
            if (!"LocoNet".equals(uName)) { // NOI18N
                uName = uName + ": "; // NOI18N
            } else {
                uName = "";
            }
        }
        return uName + Bundle.getMessage("MenuItemCmdStnConfig");
    }

    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        // set up constants from properties file, if possible
        String name = "<unchanged>"; // NOI18N
        try {
            name = memo.getSlotManager().getCommandStationType().getName();
            // get first token
            if (name.indexOf(' ') != -1) {
                name = name.substring(0, name.indexOf(' '));
            }
            log.debug("match /{}/", name); // NOI18N
            rb = ResourceBundle.getBundle("jmri.jmrix.loconet.cmdstnconfig." + name + "options"); // NOI18N
        } catch (Exception e) { // use standard option set
            log.warn("Failed to find properties for /{}/ command station type", name, e); // NOI18N
            rb = ResourceBundle.getBundle("jmri.jmrix.loconet.cmdstnconfig.Defaultoptions"); // NOI18N
            // Localized strings common to all LocoNet command station models are fetched using Bundle.getMessage()
        }

        try {
            CONFIG_SLOT = Integer.parseInt(rb.getString("CONFIG_SLOT"));
            MIN_OPTION = Integer.parseInt(rb.getString("MIN_OPTION"));
            MAX_OPTION = Integer.parseInt(rb.getString("MAX_OPTION"));
        } catch (NumberFormatException e) {
            log.error("Failed to load values from /{}/ properties", name); // NOI18N
        }
        log.debug("Constants: {} {} {}", CONFIG_SLOT, MIN_OPTION, MAX_OPTION); // NOI18N

        labelT = Bundle.getMessage("StateThrownShort");
        labelC = Bundle.getMessage("StateClosedShort");
        labelTop = rb.getString("LabelTop");
        read = Bundle.getMessage("ButtonRead");
        write = Bundle.getMessage("ButtonWrite");
        String tooltip = Bundle.getMessage("CmdStnConfigFxToolTip");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        {
            // start with the CS title
            add(new JLabel(labelTop));

            // section holding buttons
            readButton = new JButton(read);
            writeButton = new JButton(write);

            JPanel pane = new JPanel();
            pane.setLayout(new FlowLayout());
            pane.add(readButton);
            pane.add(writeButton);
            if (CONFIG_SLOT == -1) { // disable reading/writing for non-configurable CS types, ie. Intellibox-I/-II
                readButton.setEnabled(false);
                writeButton.setEnabled(false);
            }
            add(pane);

            optionBox = new JCheckBox(Bundle.getMessage("CheckBoxReserved"));
            add(optionBox);

            // heading
            add(new JLabel(Bundle.getMessage("HeadingText")));

            // section holding options
            JPanel options = new JPanel();
            GridBagConstraints gc = new GridBagConstraints();
            GridBagLayout gl = new GridBagLayout();
            gc.gridy = 0;
            gc.ipady = 0;

            options.setLayout(gl);
            for (int i = MIN_OPTION; i <= MAX_OPTION; i++) {
                JPanel p2 = new JPanel();
                p2.setLayout(new FlowLayout());
                ButtonGroup g = new ButtonGroup();
                JRadioButton c = new JRadioButton(labelC);
                JRadioButton t = new JRadioButton(labelT);
                g.add(c);
                g.add(t);

                p2.add(t);
                p2.add(c);

                closedButtons[i - MIN_OPTION] = c;
                thrownButtons[i - MIN_OPTION] = t;
                gc.weightx = 1.0;
                gc.gridx = 0;
                gc.anchor = GridBagConstraints.CENTER;
                gl.setConstraints(p2, gc);
                options.add(p2);
                gc.gridx = 1;
                gc.weightx = GridBagConstraints.REMAINDER;
                gc.anchor = GridBagConstraints.WEST;
                String label;
                try {
                    label = rb.getString("Option" + i); // model specific Option descriptions NOI18N
                    isReserved[i - MIN_OPTION] = false;
                } catch (java.util.MissingResourceException e) {
                    label = "" + i + ": " + Bundle.getMessage("Reserved");
                    isReserved[i - MIN_OPTION] = true;
                }
                JLabel l = new JLabel(label);
                if (i > 20 && i < 24) {
                    log.debug("CS name: {}", name);
                    if (name.startsWith("DB150")) {
                        // DB150 is the only model using different OpSw 21-23 combos than the common tooltip, which is stored in LocoNetBundle
                        tooltip = rb.getString("DB150ConfigFxToolTip");
                    }
                    t.setToolTipText(tooltip);
                    c.setToolTipText(tooltip);
                    l.setToolTipText(tooltip);
                }
                labels[i - MIN_OPTION] = l;
                gl.setConstraints(l, gc);
                options.add(l);
                gc.gridy++;
            }
            JScrollPane js = new JScrollPane(options);
            js.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            js.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            add(js);

        }

        optionBox.addActionListener((ActionEvent e) -> {
            updateVisibility(optionBox.isSelected());
        });
        readButton.addActionListener((ActionEvent e) -> {
            readButtonActionPerformed(e);
        });
        writeButton.addActionListener((ActionEvent e) -> {
            writeButtonActionPerformed(e);
        });

        updateVisibility(optionBox.isSelected());

        // connect to the LnTrafficController
        memo.getLnTrafficController().addLocoNetListener(~0, this);

        // and start
        start();
    }

    void updateVisibility(boolean show) {
        for (int i = MIN_OPTION; i <= MAX_OPTION; i++) {
            if (isReserved[i - MIN_OPTION]) {
                closedButtons[i - MIN_OPTION].setVisible(show);
                thrownButtons[i - MIN_OPTION].setVisible(show);
                labels[i - MIN_OPTION].setVisible(show);
            }
        }
        revalidate();
    }

    public void readButtonActionPerformed(java.awt.event.ActionEvent e) {
        // format and send request
        start();
    }

    public void writeButtonActionPerformed(java.awt.event.ActionEvent e) {
        LocoNetMessage msg = new LocoNetMessage(14);
        msg.setElement(0, LnConstants.OPC_WR_SL_DATA);
        msg.setElement(1, 0x0E);
        msg.setElement(2, CONFIG_SLOT);

        // load last seen contents into message
        for (int i = 0; i < 10; i++) {
            msg.setElement(3 + i, oldcontent[i]);
        }

        // load contents to message
        for (int i = 0; i <= (MAX_OPTION - MIN_OPTION); i++) {
            // i indexes over closed buttons
            int byteIndex = i / 8; // byteIndex = 0 is the first payload byte
            if (byteIndex > 3) {
                byteIndex++; // Skip the 4th payload byte for some reason
            }
            byteIndex += 3; // Add base offset into slot message to first data byte

            int bitIndex = i % 8;
            int bitMask = 0x01 << bitIndex;

            if (closedButtons[i].isSelected()) {
                msg.setElement(byteIndex, msg.getElement(byteIndex) | bitMask);
            } else {
                msg.setElement(byteIndex, msg.getElement(byteIndex) & ~bitMask);
            }
        }

        // send message
        memo.getLnTrafficController().sendLocoNetMessage(msg);
    }

    /**
     * Start the Frame operating by asking for a read.
     */
    public void start() {
        // format and send request for slot contents
        LocoNetMessage l = new LocoNetMessage(4);
        l.setElement(0, LnConstants.OPC_RQ_SL_DATA);
        l.setElement(1, CONFIG_SLOT);
        l.setElement(2, 0);
        l.setElement(3, 0);
        memo.getLnTrafficController().sendLocoNetMessage(l);
    }

    /**
     * Process the incoming message to look for Slot 127 Read.
     */
    @Override
    public void message(LocoNetMessage msg) {
        if (msg.getOpCode() != LnConstants.OPC_SL_RD_DATA) {
            return;
        }
        if (msg.getElement(2) != CONFIG_SLOT) {
            return;
        }

        // save contents for later
        for (int i = 0; i < 10; i++) {
            oldcontent[i] = msg.getElement(3 + i);
        }

        // set the GUI
        for (int i = 0; i <= (MAX_OPTION - MIN_OPTION); i++) {
            // i indexes over closed/thrown buttons
            int byteIndex = i / 8; // index = 0 is the first payload byte
            if (byteIndex > 3) {
                byteIndex++; // Skip the 4th payload byte for some reason
            }
            byteIndex += 3; // Add base offset to first data byte

            int bitIndex = i % 8;
            int bitMask = 0x01 << bitIndex;

            int data = msg.getElement(byteIndex);  // data is the payload byte

            if ((data & bitMask) != 0) {
                closedButtons[i].setSelected(true);
            } else {
                thrownButtons[i].setSelected(true);
            }
        }

        log.debug("Config Slot Data: {}", msg);
    }

    @Override
    public void dispose() {
        // disconnect from LnTrafficController
        memo.getLnTrafficController().removeLocoNetListener(~0, this);
        super.dispose();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(CmdStnConfigPane.class);

}
