package jmri.jmrit.symbolicprog;

import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.decoderdefn.IdentifyDecoder;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide GUI controls to select a decoder for a new loco and/or copy an
 * existing config.
 * <p>
 * The user can select either a loco to copy, or a new decoder type or both.
 * <p>
 * When the "open programmer" button is pushed, i.e. the user is ready to
 * continue, the startProgrammer method is invoked. This should be overridden
 * (e.g. in a local anonymous class) to create the programmer frame you're
 * interested in.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Howard G. Penny Copyright (C) 2005
 * @see jmri.jmrit.decoderdefn.IdentifyDecoder
 * @see jmri.jmrit.roster.IdentifyLoco
 */
public class NewLocoSelPane extends jmri.util.swing.JmriPanel {

    public NewLocoSelPane(JLabel s, ProgModeSelector selector) {
        _statusLabel = s;
        this.selector = selector;
        init();
    }

    public NewLocoSelPane() {
        init();
    }

    ProgModeSelector selector;

    public void init() {
        JLabel last;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(last = new JLabel(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("NewLocoProgTrack")));
        last.setBorder(new EmptyBorder(6, 0, 6, 0));
        add(new JLabel(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("CopySettings")));

        locoBox = new GlobalRosterEntryComboBox();
        locoBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("Locomotive selected changed");
                }
                matchDecoderToLoco();
            }
        });
        add(locoBox);

        JPanel pane1a = new JPanel();
        pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
        pane1a.add(new JLabel(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("LabelDecoderInstalled")));
        JButton iddecoder = new JButton(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("IdentifyDecoder"));
        iddecoder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("identify decoder pressed");
                }
                startIdentify();
            }
        });
        pane1a.add(iddecoder);
        pane1a.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        add(pane1a);

        decoderBox = InstanceManager.getDefault(DecoderIndexFile.class).matchingComboBox(null, null, null, null, null, null);
        add(decoderBox);

        // Open programmer button
        JButton go1 = new JButton(java.util.ResourceBundle.getBundle("jmri/jmrit/symbolicprog/SymbolicProgBundle").getString("IdentifyDecoder"));
        go1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("Open programmer pressed");
                }
                openButton();
            }
        });
        add(go1);
        setBorder(new EmptyBorder(6, 6, 6, 6));
    }

    JLabel _statusLabel = null;

    private void startIdentify() {
        // start identifying a decoder
        final NewLocoSelPane me = this;
        Programmer p = null;
        if (selector != null && selector.isSelected()) p = selector.getProgrammer();
        if (p == null) {
            log.warn("Selector did not provide a programmer, use default");
            p = jmri.InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        }
        IdentifyDecoder id = new IdentifyDecoder(p) {
            private NewLocoSelPane who = me;

            @Override
            protected void done(int mfg, int model, int productID) {
                // if Done, updated the selected decoder
                who.selectDecoder(mfg, model, productID);
            }

            @Override
            protected void message(String m) {
                if (_statusLabel != null) {
                    _statusLabel.setText(m);
                }
            }

            @Override
            public void error() {
            }
        };
        id.start();
    }

    private void selectDecoder(int mfgID, int modelID, int productID) {
        JComboBox<String> temp = null;

        // if productID present, try with that
        if (productID != -1) {
            String sz_productID = Integer.toString(productID);
            temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingComboBox(null, null, Integer.toString(mfgID), Integer.toString(modelID), sz_productID, null);
            if (temp.getItemCount() == 0) {
                log.debug("selectDecoder found no items with product ID " + productID);
                temp = null;
            } else {
                log.debug("selectDecoder found " + temp.getItemCount() + " matches with productID " + productID);
            }
        }

        // try without product ID if needed
        if (temp == null) {  // i.e. if no match previously
            temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingComboBox(null, null, Integer.toString(mfgID), Integer.toString(modelID), null, null);
            if (log.isDebugEnabled()) {
                log.debug("selectDecoder without productID found " + temp.getItemCount() + " matches");
            }
        }

        // install all those in the JComboBox in place of the longer, original list
        if (temp.getItemCount() > 0) {
            decoderBox.setModel(temp.getModel());
            decoderBox.setSelectedIndex(0);
        } else {
            log.warn("Decoder says " + mfgID + " " + modelID + " decoder, but no such decoder defined");
        }
    }

    private void matchDecoderToLoco() {
        if (((String) locoBox.getSelectedItem()).equals("<none>")) {
            return;
        }
        RosterEntry r = Roster.getDefault().entryFromTitle((String) locoBox.getSelectedItem());
        String decoderModel = r.getDecoderModel();
        String decoderFamily = r.getDecoderFamily();
        if (log.isDebugEnabled()) {
            log.debug("selected loco uses decoder " + decoderFamily + " " + decoderModel);
        }
        // locate a decoder like that.
        List<DecoderFile> l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        if (log.isDebugEnabled()) {
            log.debug("found " + l.size() + " matches");
        }
        if (l.size() > 0) {
            DecoderFile d = l.get(0);
            String title = d.titleString();
            if (log.isDebugEnabled()) {
                log.debug("Decoder file title " + title);
            }
            for (int i = 0; i < decoderBox.getItemCount(); i++) {
                if (title.equals(decoderBox.getItemAt(i))) {
                    decoderBox.setSelectedIndex(i);
                }
            }
        } else {
            log.warn("Loco uses " + decoderFamily + " " + decoderModel + " decoder, but no such decoder defined");
        }
    }

    private JComboBox<Object> locoBox = null;
    private JComboBox<String> decoderBox = null;

    /**
     * Handle pushing the open programmer button by finding names, then calling
     * a template method
     */
    protected void openButton() {

        // find the decoderFile object
        DecoderFile decoderFile = InstanceManager.getDefault(DecoderIndexFile.class).fileFromTitle((String) decoderBox.getSelectedItem());
        if (log.isDebugEnabled()) {
            log.debug("decoder file: " + decoderFile.getFileName());
        }

        // create a dummy RosterEntry with the decoder info
        RosterEntry re = new RosterEntry();
        re.setDecoderFamily(decoderFile.getFamily());
        re.setDecoderModel(decoderFile.getModel());
        re.setId(Bundle.getMessage("LabelNewDecoder"));
        // note we're leaving the filename information as null
        // add the new roster entry to the in-memory roster
        Roster.getDefault().addEntry(re);

        startProgrammer(decoderFile, re);
    }

    /**
     * Meant to be overridden to start the desired type of programmer
     *
     * @param decoderFile selected file, passed to eventual implementation
     * @param r           RosterEntry defining this locomotive, to be filled in
     *                    later
     */
    protected void startProgrammer(DecoderFile decoderFile, RosterEntry r) {
        log.error("startProgrammer method in NewLocoSelPane should have been overridden");
    }

    private final static Logger log = LoggerFactory.getLogger(NewLocoSelPane.class);

}
