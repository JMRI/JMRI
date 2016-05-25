// DuplexGroupTabbedPanel.java
package jmri.jmrix.loconet.duplexgroup.swing;

import java.util.ResourceBundle;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnPanel;
import jmri.util.JmriJFrame;

/**
 * Implements a panel with tabs which allows: . configuration of Duplex Group
 * identity information and . scanning of Duplex channels for interfering signal
 * sources
 *
 * This tool works equally well with UR92 and UR92CE devices. The UR92 and
 * UR92CE behave identically with respect to this tool. For the purpose of
 * clarity, only the term UR92 is used herein.
 *
 * @author B. Milhaupt, Copyright 2011
 * @version	$Revision: 1.0 $
 */
public class DuplexGroupTabbedPanel extends LnPanel {

    /**
     *
     */
    private static final long serialVersionUID = 5891070481222820538L;
    javax.swing.JTabbedPane tabbedPane = null;
    DuplexGroupInfoPanel dgip = null;
    DuplexGroupScanPanel dgsp = null;
    DuplexGroupTabbedPanel thisone = null;

    public DuplexGroupTabbedPanel() {
        super();
        thisone = this;

    }

    @Override
    public void initComponents() throws Exception {
        super.initComponents();
        tabbedPane = new javax.swing.JTabbedPane();
        dgsp = new DuplexGroupScanPanel();
        dgip = new DuplexGroupInfoPanel();
        tabbedPane.addTab(rb.getString("TabTextGroupIdentity"), null,
                dgip, rb.getString("TabToolTipGroupIdentity"));
        tabbedPane.addTab(rb.getString("TabTextChannelScan"), null,
                dgsp, rb.getString("TabToolTipChannelScan"));
        add(tabbedPane);
        dgip.initComponents();
        dgsp.initComponents();
        // uses swing operations
    }

    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        dgip.initComponents(memo);
        dgsp.initComponents(memo);

    }
    javax.swing.Timer tmr = null;

    @Override
    public void initContext(Object context) {

        tmr = new javax.swing.Timer(10, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                tmr.stop();
                ((JmriJFrame) thisone.getRootPane().getParent()).setPreferredSize(null);
                ((JmriJFrame) thisone.getRootPane().getParent()).pack();
            }
        });
        // need to trigger first delay to get first channel to be scanned
        tmr.setInitialDelay(10);
        tmr.setRepeats(false);
        tmr.start();
        return;
    }
    private static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbed");

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.duplexgroup.DuplexGroupTabbedPanel";
    }

    @Override
    public String getTitle() {
        return rb.getString("Title");
    }

    @Override
    public void dispose() {
        dgip.dispose();
        dgsp.dispose();

    }
}
