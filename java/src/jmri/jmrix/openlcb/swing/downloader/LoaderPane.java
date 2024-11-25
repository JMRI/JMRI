package jmri.jmrix.openlcb.swing.downloader;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.MemoryContents;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.swing.NodeSpecificFrame;
import jmri.util.swing.WrapLayout;

import org.openlcb.Connection;
import org.openlcb.LoaderClient;
import org.openlcb.LoaderClient.LoaderStatusReporter;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.NodeSelector;
import org.openlcb.swing.MemorySpaceSelector;

/**
 * Pane for downloading firmware files files to OpenLCB devices which support
 * firmware updates according to the Firmware Upgrade Protocol.
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
    MemorySpaceSelector spaceField;
    JCheckBox lockNode;
    LoaderClient loaderClient;
    NodeID nid;
    OlcbInterface iface;

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
        this.nodeSelector = new NodeSelector(store, Integer.MAX_VALUE);  // display all ID terms available
        this.loaderClient = memo.get(LoaderClient.class);
        this.nid = memo.get(NodeID.class);
        this.iface = memo.get(OlcbInterface.class);
        
        // We can add to GUI here
        loadButton.setText("Load");
        loadButton.setToolTipText("Start Load Process");
        JPanel p;

        p = new JPanel();
        p.setLayout(new WrapLayout());
        p.add(new JLabel("Target Node ID: "));
        p.add(nodeSelector);
        selectorPane.add(p);

        p = new JPanel();
        p.setLayout(new WrapLayout());
        p.add(new JLabel("Address Space: "));

        spaceField = new MemorySpaceSelector(0xEF);
        p.add(spaceField);
        selectorPane.add(p);
        spaceField.setToolTipText("The number of the address space, e.g. 239 or 0xEF");

        p = new JPanel();
        p.setLayout(new WrapLayout());
        lockNode = new JCheckBox("Lock Node");
        p.add(lockNode);
        selectorPane.add(p);

        // Verify not an option
        verifyButton.setVisible(false);
    }

    @Override
    protected void addChooserFilters(JFileChooser chooser) {
    }

    @Override
    public void doRead(JFileChooser chooser) {
        // has a file been selected? Might not been if Chooser was cancelled
        if (chooser == null || chooser.getSelectedFile() == null) return;

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
        if (memo != null) {
            return (memo.getUserName() + " Firmware Downloader");
        }
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
        
        // if window referencing this node is open, close it
        var frames = jmri.util.JmriJFrame.getFrames();
        for (var frame : frames) {
            if (frame instanceof NodeSpecificFrame) {
                if ( ((NodeSpecificFrame)frame).getNodeID() == destNodeID() ) {
                    // This window references the node and should be closed
                    
                    // Notify the user to handle any prompts before continuing.
                    jmri.util.swing.JmriJOptionPane.showMessageDialog(this, 
                        Bundle.getMessage("OpenWindowMessage")
                    );
                    
                    // Depending on the state of the window, and how the user handles
                    // a prompt to discard changes or cancel, this might be 
                    // presented multiple times until the user finally
                    // allows the window to close. See the message in the Bundle.properties
                    // file for how we handle this.

                    // Close this window - force onto the queue before a possible next modal dialog
                    jmri.util.ThreadingUtil.runOnGUI(() -> {
                        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    });
                    
                }
            }
        }

        // de-cache CDI information so next window opening will reload
        iface.dropConfigForNode(destNodeID());

        // start firmware load operation
        setOperationAborted(false);
        abortButton.setEnabled(false);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));
        int ispace = spaceField.getMemorySpace();
        long addr = 0;
        loaderClient.doLoad(nid, destNodeID(), ispace, addr, fdata, new LoaderStatusReporter() {
            @Override
            public void onProgress(float percent) {
                updateGUI(Math.round(percent));
            }

            @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "SLF4J_FORMAT_SHOULD_BE_CONST",
                justification = "message String also used in status JLabel")
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
        javax.swing.SwingUtilities.invokeLater(() -> {
            log.debug("updateGUI with {}",value);
            // update progress bar
            bar.setValue(value);
        });
    }

    /**
     * Get NodeID from the GUI
     *
     * @return selected node id
     */
    NodeID destNodeID() {
        return nodeSelector.getSelectedNodeID();
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

            log.info("Total file size to read (in bytes) : {}",fis.available());
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
    public static class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("Openlcb Firmware Download",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    LoaderAction.class.getName(),
                    jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoaderPane.class);
}
