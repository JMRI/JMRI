package jmri.jmrix.loconet.duplexgroup.swing;

import java.util.ResourceBundle;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnPanel;
import jmri.util.JmriJFrame;

/**
 * Implements a panel with tabs which allows:
 * <ul>
 *     <li>configuration of Duplex Group identity information
 *     <li>scanning of Duplex channels for interfering signal sources
 * </ul>
 * This tool works equally well with UR92 and UR92CE devices. The UR92 and
 * UR92CE behave identically with respect to this tool. For the purpose of
 * clarity, only the term UR92 is used herein.
 *
 * @author B. Milhaupt, Copyright 2011
 */
public class DuplexGroupTabbedPanel extends LnPanel {

    javax.swing.JTabbedPane tabbedPane = null;
    DuplexGroupInfoPanel dgip = null;
    DuplexGroupScanPanel dgsp = null;
    DuplexGroupTabbedPanel thisone = null;

    public DuplexGroupTabbedPanel() {
        super();
        thisone = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        tabbedPane = new javax.swing.JTabbedPane();
        dgsp = new DuplexGroupScanPanel();
        dgip = new DuplexGroupInfoPanel();
        tabbedPane.addTab(Bundle.getMessage("TabTextGroupIdentity"), null,
                dgip, Bundle.getMessage("TabToolTipGroupIdentity"));
        tabbedPane.addTab(Bundle.getMessage("TabTextChannelScan"), null,
                dgsp, Bundle.getMessage("TabToolTipChannelScan"));
        add(tabbedPane);
        dgip.initComponents();
        dgsp.initComponents();
        // uses swing operations
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        dgip.initComponents(memo);
        dgsp.initComponents(memo);
    }
    javax.swing.Timer tmr = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {

        tmr = new javax.swing.Timer(10, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                tmr.stop();
                java.awt.Container f = thisone.getRootPane().getParent();
                if (f != null) {
                    if (f instanceof JmriJFrame) {
                        JmriJFrame jf = (JmriJFrame) f;
                        jf.setPreferredSize(null);
                        jf.pack();
                    }
                }
            }
        });
        // need to trigger first delay to get first channel to be scanned
        tmr.setInitialDelay(10);
        tmr.setRepeats(false);
        tmr.start();
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbedPanel"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("TabbedTitle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        dgip.dispose();
        dgsp.dispose();
    }

}
