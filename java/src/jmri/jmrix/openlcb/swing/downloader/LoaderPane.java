package jmri.jmrix.openlcb.swing.downloader;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.MemoryContents;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.NodeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for downloading .hex files files to OpenLCB devices which
 * support firmware updates.
 *<p>
 * This version relies on the file contents interpretation mechanisms built into
 * the readHex() methods found in class jmri.jmrit.MemoryContents to
 * automatically interpret the file's addressing type - either 16-bit or 24-bit
 * addressing. The interpreted addressing type is reported in the pane after a
 * file is read. The user cannot select the addressing type.
 *<P>
 * This version relies on the file contents checking mechanisms built into the
 * readHex() methods found in class jmri.jmrit.MemoryContents to check for a
 * wide variety of possible issues in the contents of the firmware update file.
 * Any exception thrown by at method is used to select an error message to
 * display in the status line of the pane.
 *
 * @author	Bob Jacobsen Copyright (C) 2005, 2015 (from the LocoNet version by B. Milhaupt Copyright (C) 2013, 2014)
 */
public class LoaderPane extends jmri.jmrix.AbstractLoaderPane
        implements ActionListener, jmri.jmrix.can.swing.CanPanelInterface {

    protected CanSystemConnectionMemo memo;
    MemoryConfigurationService mcs;
    DatagramService dcs;
    MimicNodeStore store;
    NodeSelector nodeSelector;
    JPanel selectorPane;
    JTextField spaceField;
    JCheckBox lockNode;
    public String getTitle(String menuTitle) { return Bundle.getMessage("TitleLoader"); }

    public void initComponents(CanSystemConnectionMemo memo) throws Exception {
        this.memo = memo;
        this.mcs = memo.get(MemoryConfigurationService.class);
        this.dcs = memo.get(DatagramService.class);
        this.store = memo.get(MimicNodeStore.class);
        this.nodeSelector = new NodeSelector(store);
        
        // We can add to GUI here       
        JPanel p;
        
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Target Node ID: "));
        p.add(nodeSelector);
        selectorPane.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Address Space: "));
        p.add(spaceField = new JTextField(""+0xFD));
        selectorPane.add(p);
        spaceField.setToolTipText("The decimal number of the address space, e.g. 239");

        p = new JPanel();
        p.setLayout(new FlowLayout());        
        p.add(lockNode = new JCheckBox("Lock Node"));
        selectorPane.add(p);
        
        // Verify not an option
        verifyButton.setVisible(false);
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
    protected void handleOptionsInFileContent(MemoryContents inputContent){
    }

    @Override
    protected void doLoad() {
        super.doLoad();
        
        setOperationAborted(false);
        space = Integer.valueOf(spaceField.getText());

        // start the download itself
        sendSequence();
    }

    @Override
    protected void doVerify() {
        super.doVerify();

        // start the download itself
        //operation = PXCT2VERIFYDATA;
        //sendSequence();
    }


    int totalmsgs;
    int sentmsgs;

    int startaddr;
    int endaddr;
    
    int location; // current working location
    int space;
    
    final int SIZE = 64;
    
    // start sending sequence
    private void sendSequence() {
        // define range to be checked for download
        startaddr = 0x000000;
        endaddr = 0xFFFFFF;

        // fast scan to count messages to send for progress bar
        location = inputContent.nextContent(startaddr);
        totalmsgs = 0;
        sentmsgs = 0;

        do {
            // we're assuming that data is pretty dense,
            // so we can jump through in SIZE-sized chunks
            location = location + SIZE;
            totalmsgs++;
            // update to the next location for data
            int next = inputContent.nextContent(location);
            if (next < 0) {
                break;   // no data left
            }
            location = next;
            
        } while (location <= endaddr);

        log.info("Expect downloading to send {} write messages", totalmsgs);
        
        // Start write sequence:
        // find the initial location with data
        location = inputContent.nextContent(startaddr);

        // queue start up messages
        // sendFreeze();
        
        // instead of above, go directly to start data loop
        sendNext();

        // rest of operation if via callbacks inside sendNext();

    }

    /**
     * Do an OpenLCB write operation for up to 64 bytes from the current
     * memory location.
     *<p>
     * Contains call-back for next message.
     */
    void sendNext() {
        // ensure threading
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                byte[] temp = new byte[SIZE];
                int count;
                for (count = 0; count < SIZE; count++) {
                    if (!inputContent.locationInUse(location+count)) {
                        break;
                    }
                    temp[count] = (byte)inputContent.getLocation(location+count);
                }
                byte[] data = new byte[count];
                System.arraycopy(temp, 0, data, 0, count);

                int addr = location; // next call back might be instantaneous
                location = location + count; 
                log.info("Sending write to 0x{} length {}", Integer.toHexString(location).toUpperCase(), count);
                mcs.request(new MemoryConfigurationService.McsWriteMemo(destNodeID(), space, addr, data) {
                    public void handleWriteReply(int code) { 
                        log.debug("Start of handleWriteReply "+code);
                        // update GUI intermittently
                        sentmsgs++;
                        if ((sentmsgs % 20) == 0) {
                            // update progress bar via the queue to ensure synchronization
                            updateGUI(100 * sentmsgs / totalmsgs);
                        }
                
                        if (code == 0 && !isOperationAborted()) {
                            // normal reply - queue next
                            location = inputContent.nextContent(location);
                            if (location < 0) {
                                log.info("   Download completed normally");
                                sendDataDone(true);
                            } else {
                                if (log.isDebugEnabled()) log.debug("   Continue to 0x{}", Integer.toHexString(location).toUpperCase());
                                sendNext();
                            }
                        } else {
                            // non-normal reply
                            log.info("   Done abnormal code:{}", code);
                            sendDataDone(false);
                        }
                    }
                });
            }
        });
    }

    void sendDataDone(boolean OK) {
        // report OK or not to GUI
        setOperationAborted(!OK);
        
        // send end (after wait)
        // ...

        this.updateGUI(100); //draw bar to 100%

        // signal end to GUI via the queue to ensure synchronization
        Runnable r = new Runnable() {
            @Override
            public void run() {
                enableGUI();
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }
    
    /**
     * Signal GUI that it's the end of the download
     * <P>
     * Should be invoked on the Swing thread
     */
    void enableGUI() {
        LoaderPane.this.enableDownloadVerifyButtons();
    }

    /**
     * Update the GUI for progress
     */
    void updateGUI(final int value) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("updateGUI with " + value);
                }
                // update progress bar
                bar.setValue(100 * sentmsgs / totalmsgs);
            }
        });
    }

    void sendFreeze() {
        dcs.sendData(new DatagramService.DatagramServiceTransmitMemo(destNodeID(),new int[]{0x20, 0xA1, space}) {
                public void handleReply(int code) { 
                    log.info("freeze reply");
                    sendNext();
                }
            });
    }
    void sendUnfreeze() {
         dcs.sendData(new DatagramService.DatagramServiceTransmitMemo(destNodeID(),new int[]{0x20, 0xA0, space}) {
                public void handleReply(int code) { 
                    log.info("unfreeze reply");
                }
            });
    }

    /**
     * Get NodeID from the GUI
     */
    NodeID destNodeID() {
        return (NodeID) nodeSelector.getSelectedItem();
    }
    
    @Override
    protected void setDefaultFieldValues() {
        // currently, doesn't do anything, as just loading raw hex files.
        log.debug("setDefaultFieldValues leaves fields unchanged");
    }

    /**
     * Checks the values in the GUI text boxes to determine if any are invalid.
     * Intended for use immediately after reading a firmware file for the
     * purpose of validating any key/value pairs found in the file. Also
     * intended for use immediately before a "verify" or "download" operation to
     * check that the user has not changed any of the GUI text values to ones
     * that are unsupported.
     *
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

    private final static Logger log = LoggerFactory.getLogger(LoaderPane.class.getName());
}
