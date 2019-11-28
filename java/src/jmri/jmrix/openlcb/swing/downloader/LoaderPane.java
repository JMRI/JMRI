package jmri.jmrix.openlcb.swing.downloader;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.MemoryContents;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.openlcb.Connection;
import org.openlcb.LoaderClient;
import org.openlcb.LoaderClient.LoaderStatusReporter;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.NodeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for downloading firmware files files to OpenLCB devices which support
 * firmware updates according to the Firmware Upgrade Protocol.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2005, 2015 (from the LocoNet version by B.
 * Milhaupt Copyright (C) 2013, 2014) David R Harris (C) 2016 Balazs Racz (C)
 * 2016
 */
public class LoaderPane extends jmri.jmrix.AbstractLoaderPane
        implements jmri.jmrix.can.swing.CanPanelInterface {

    protected CanSystemConnectionMemo memo;
    Connection connection;
    MemoryConfigurationService mcs;
    DatagramService dcs;
    MimicNodeStore store;
    NodeSelector nodeSelector;
    JPanel selectorPane;
    JTextField spaceField;
    JCheckBox lockNode;
    LoaderClient loaderClient;
    NodeID nid;

    public String getTitle(String menuTitle) {
        return Bundle.getMessage("TitleLoader");
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.connection = memo.get(Connection.class);
        this.mcs = memo.get(MemoryConfigurationService.class);
        this.dcs = memo.get(DatagramService.class);
        this.store = memo.get(MimicNodeStore.class);
        this.nodeSelector = new NodeSelector(store);
        this.loaderClient = memo.get(LoaderClient.class);
        this.nid = memo.get(NodeID.class);
        // We can add to GUI here
        loadButton.setText("Load");
        loadButton.setToolTipText("Start Load Process");
        JPanel p;

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Target Node ID: "));
        p.add(nodeSelector);
        selectorPane.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Address Space: "));
        p.add(spaceField = new JTextField("" + 0xEF));
        selectorPane.add(p);
        spaceField.setToolTipText("The decimal number of the address space, e.g. 239");

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(lockNode = new JCheckBox("Lock Node"));
        selectorPane.add(p);

        // Verify not an option
        verifyButton.setVisible(false);
    }

    @Override
    protected void addChooserFilters(JFileChooser chooser) {
    }

    @Override
    public void doRead(JFileChooser chooser) {
        String fn = chooser.getSelectedFile().getPath();
        readFile(fn);
        bar.setValue(0);
        loadButton.setEnabled(true);
    }

    public LoaderPane() {
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.openlcb.swing.downloader.LoaderFrame";
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("TitleLoader"));
    }

    @Override
    protected void addOptionsPanel() {
        selectorPane = new JPanel();
        selectorPane.setLayout(new BoxLayout(selectorPane, BoxLayout.Y_AXIS));

        add(selectorPane);
    }

    @Override
    protected void handleOptionsInFileContent(MemoryContents inputContent) {
    }

    @Override
    protected void doLoad() {
        super.doLoad();
        setOperationAborted(false);
        abortButton.setEnabled(false);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));
        Integer ispace = Integer.valueOf(spaceField.getText());
        long addr = 0;
        loaderClient.doLoad(nid, destNodeID(), ispace, addr, fdata, new LoaderStatusReporter() {
            @Override
            public void onProgress(float percent) {
                updateGUI(Math.round(percent));
            }

            @Override
            public void onDone(int errorCode, String errorString) {
                if (errorCode == 0) {
                    updateGUI(100); //draw bar to 100%
                    if (errorString.isEmpty()) {
                        status.setText(Bundle.getMessage("StatusDownloadOk"));
                    } else {
                        status.setText(Bundle.getMessage("StatusDownloadOkWithMessage", errorString));
                    }
                    setOperationAborted(false);
                } else {
                    String msg = Bundle.getMessage("StatusDownloadFailed", Integer.toHexString(errorCode), errorString);
                    status.setText(msg);
                    setOperationAborted(true);
                    log.info(msg);
                }
                enableDownloadVerifyButtons();
            }
        });
    }

    void updateGUI(final int value) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("updateGUI with " + value);
                }
                // update progress bar
                bar.setValue(value);
            }
        });
    }

    /**
     * Get NodeID from the GUI
     *
     * @return selected node id
     */
    NodeID destNodeID() {
        return nodeSelector.getSelectedItem();
    }

    @Override
    protected void setDefaultFieldValues() {
        // currently, doesn't do anything, as just loading raw hex files.
        log.debug("setDefaultFieldValues leaves fields unchanged");
    }

    byte[] fdata;

    public void readFile(String filename) {
        File file = new File(filename);
        try (FileInputStream fis = new FileInputStream(file)) {

            log.info("Total file size to read (in bytes) : "
                    + fis.available());
            fdata = new byte[fis.available()];
            int i = 0;
            int content;
            while ((content = fis.read()) != -1) {
                fdata[i++] = (byte) content;
            }

        } catch (IOException e) {
            log.error("Unable to read {}", filename, e);
        }
    }

    /**
     * Checks the values in the GUI text boxes to determine if any are invalid.
     * Intended for use immediately after reading a firmware file for the
     * purpose of validating any key/value pairs found in the file. Also
     * intended for use immediately before a "verify" or "download" operation to
     * check that the user has not changed any of the GUI text values to ones
     * that are unsupported.
     * <p>
     * Note that this method cannot guarantee that the values are suitable for
     * the hardware being updated and/or for the particular firmware information
     * which was read from the firmware file.
     *
     * @return false if one or more GUI text box contains an invalid value
     */
    @Override
    protected boolean parametersAreValid() {
        return true;
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("Openlcb Firmware Download",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    LoaderAction.class.getName(),
                    jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LoaderPane.class);
}
