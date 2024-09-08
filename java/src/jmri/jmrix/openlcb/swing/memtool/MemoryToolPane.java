package jmri.jmrix.openlcb.swing.memtool;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JmriJFrame;
import jmri.util.swing.WrapLayout;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.swing.*;
import org.openlcb.swing.memconfig.MemConfigDescriptionPane;
import org.openlcb.swing.MemorySpaceSelector;


/**
 * Pane for doing various memory operations
 *
 * @author Bob Jacobsen Copyright (C) 2023
 * @since 5.3.4
 */
public class MemoryToolPane extends jmri.util.swing.JmriPanel
        implements jmri.jmrix.can.swing.CanPanelInterface {

    protected CanSystemConnectionMemo memo;
    Connection connection;
    NodeID nid;

    MimicNodeStore store;
    MemoryConfigurationService service;
    NodeSelector nodeSelector;

    public String getTitle(String menuTitle) {
        return Bundle.getMessage("TitleMemoryTool");
    }

    static final int CHUNKSIZE = 64;

    MemorySpaceSelector spaceField;
    JLabel statusField;
    JButton gb;
    JButton pb;
    JButton cb;
    boolean cancelled = false;
    boolean running = false;

    /**
     * if checked (the default), the Address Space Status
     * reply will be used to set the length of the read.
     * The read will also stop on a short-data reply or ann
     * error reply, including the normal 0x1082 end of data message.
     * If unchecked, the Address Space Status is skipped
     * and the read ends on short-data reply or error reply.
     * <p>
     * We do not persist this as a preference, because
     8 we want the default to be trusted and the user to
     * reselect (or really unselect) as needed.
     */
    JCheckBox trustStatusReply;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.connection = memo.get(Connection.class);
        this.nid = memo.get(NodeID.class);

        store = memo.get(MimicNodeStore.class);
        EventTable stdEventTable = memo.get(OlcbInterface.class).getEventTable();
        if (stdEventTable == null) {
            log.error("no OLCB EventTable found");
            return;
        }
        service = memo.get(MemoryConfigurationService.class);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Add to GUI here
        var ns = new JPanel();
        ns.setLayout(new WrapLayout());
        add(ns);
        nodeSelector = new org.openlcb.swing.NodeSelector(store, Integer.MAX_VALUE);
        ns.add(nodeSelector);
        JButton check = new JButton("Check");
        ns.add(check);
        check.addActionListener(this::pushedCheckButton);

        var ms = new JPanel();
        ms.setLayout(new WrapLayout());
        add(ms);
        ms.add(new JLabel("Memory Space:"));
        spaceField = new MemorySpaceSelector(0xFF);
        ms.add(spaceField);

        trustStatusReply = new JCheckBox("Trust Status Info");
        trustStatusReply.setSelected(true);
        ms.add(trustStatusReply);

        var bb = new JPanel();
        bb.setLayout(new WrapLayout());
        add(bb);
        gb = new JButton(Bundle.getMessage("ButtonGet"));
        bb.add(gb);
        gb.addActionListener(this::pushedGetButton);
        pb = new JButton(Bundle.getMessage("ButtonPut"));
        bb.add(pb);
        pb.addActionListener(this::pushedPutButton);
        cb = new JButton(Bundle.getMessage("ButtonCancel"));
        bb.add(cb);
        cb.addActionListener(this::pushedCancel);

        bb = new JPanel();
        bb.setLayout(new WrapLayout());
        add(bb);
        statusField = new JLabel("                          ",SwingConstants.CENTER);
        bb.add(statusField);

        setRunning(false);
    }

    public MemoryToolPane() {
    }

    @Override
    public void dispose() {
        // and complete this
        super.dispose();
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.openlcb.swing.memtool.MemoryToolPane";
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Memory Tool");
        }
        return getTitle(Bundle.getMessage("TitleMemoryTool"));
    }

    void pushedCheckButton(ActionEvent e) {
        var node = nodeSelector.getSelectedNodeID();
        JmriJFrame f = new JmriJFrame();
        f.setTitle("Configuration Capabilities");

        var p = new JPanel();
        f.add(p);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel q = new JPanel();
        q.setLayout(new WrapLayout());
        p.add(q);
        q.add(new JLabel(node.toString()));

        p.add(new JSeparator(SwingConstants.HORIZONTAL));

        var nodeMemo = store.findNode(node);
        String name = "";
        if (nodeMemo != null) {
            var ident = nodeMemo.getSimpleNodeIdent();
                if (ident != null) {
                    name = ident.getUserName();
                    q = new JPanel();
                    q.setLayout(new WrapLayout());
                    q.add(new JLabel(name));
                    p.add(q);
                }
        }

        MemConfigDescriptionPane mc = new MemConfigDescriptionPane(node, store, service);
        p.add(mc);
        mc.initComponents();

        f.pack();
        f.setVisible(true);
    }

    void pushedCancel(ActionEvent e) {
        if (running) {
            cancelled = true;
        }
    }

    void setRunning(boolean t) {
        if (t) {
            gb.setEnabled(false);
            pb.setEnabled(false);
            cb.setEnabled(true);
        } else {
            gb.setEnabled(true);
            pb.setEnabled(true);
            cb.setEnabled(false);
        }
        running = t;
    }

    int space = 0xFF;

    NodeID farID = new NodeID("0.0.0.0.0.0");

    MemoryConfigurationService.McsReadHandler cbr =
        new MemoryConfigurationService.McsReadHandler() {
            @Override
            public void handleFailure(int errorCode) {
                setRunning(false);
                if (errorCode == 0x1082) {
                    statusField.setText("Done reading");
                    log.debug("Stopping read due to 0x1082 status");
                } if (errorCode == 0x1081) {
                    log.error("Read failed. Address space not known");
                    statusField.setText("Read failed. Address space not known");
                } else {
                    log.error("Read failed. Error code is {}", String.format("%04X", errorCode));
                    statusField.setText("Read failed. Error code is "+String.format("%04X", errorCode));
                }
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException ex) {
                    log.error("Error closing file", ex);
                    statusField.setText("Error closing output file");
                }
            }

            @Override
            public void handleReadData(NodeID dest, int readSpace, long readAddress, byte[] readData) {
                log.trace("read succeed with {} bytes at {}", readData.length, readAddress);
                statusField.setText("Read "+readAddress+" bytes");
                try {
                    outputStream.write(readData);
                } catch (IOException ex) {
                    log.error("Error writing data to file", ex);
                    statusField.setText("Error writing data to file");
                    setRunning(false);
                    return; // stop now
                }
                if (readData.length != CHUNKSIZE) {
                    // short read is another way to indicate end
                    statusField.setText("Done reading");
                    log.debug("Stopping read due to short reply");
                    setRunning(false);
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException ex) {
                        log.error("Error closing file", ex);
                        statusField.setText("Error closing output file");
                    }
                    return;
                }
                // fire another unless at endingAddress
                if (readAddress+readData.length-1 >= endingAddress) { // last address read is length-1 past starting address
                    // done
                    setRunning(false);
                    log.debug("Get operation ending on length");
                    statusField.setText("Done Reading");
                }
                if (!cancelled) {
                    service.requestRead(farID, space, readAddress+readData.length,
                                        (int)Math.min(CHUNKSIZE, endingAddress-(readAddress+readData.length-1)),
                                        cbr);
                } else {
                    setRunning(false);
                    cancelled = false;
                    log.debug("Get operation cancelled");
                    statusField.setText("Cancelled");
                }
            }
        };

    OutputStream outputStream;
    long endingAddress = 0x1000; // token 1MB max if decide not to enquire about it & other methods fail

    /**
     * Starts reading from node and writing to file process
     * @param e not used
     */
    void pushedGetButton(ActionEvent e) {
        setRunning(true);
        farID = nodeSelector.getSelectedNodeID();
        try {
            space = spaceField.getMemorySpace();
        } catch (NumberFormatException ex) {
            log.error("error parsing the space field value \"{}\"", spaceField.getText());
            statusField.setText("Error parsing the space value");
            setRunning(false);
            return;
        }

        log.debug("Start get");
        if (fileChooser == null) {
            fileChooser = new jmri.util.swing.JmriJFileChooser();
        }
        fileChooser.setDialogTitle("Read into binary file");
        fileChooser.rescanCurrentDirectory();
        fileChooser.setSelectedFile(new File("memory.bin"));

        int retVal = fileChooser.showSaveDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            setRunning(false);
            return;
        }

        // open file
        File file = fileChooser.getSelectedFile();
        log.debug("access {}", file);
        try {
            outputStream = new FileOutputStream(file);
        } catch (IOException ex) {
            log.error("Error opening file", ex);
            statusField.setText("Error opening file");
            setRunning(false);
            return;
        }

        if (trustStatusReply.isSelected()) {
            // request address space info; reply will start read operations.
            // Memo has to be created here to carry appropriate farID
            MemoryConfigurationService.McsAddrSpaceMemo cbq =
                new MemoryConfigurationService.McsAddrSpaceMemo(farID, space) {
                    @Override
                    public void handleWriteReply(int errorCode) {
                        log.error("Get failed with code {}", String.format("%04X", errorCode));
                        statusField.setText("Get failed with code"+String.format("%04X", errorCode));
                        setRunning(false);
                    }

                    @Override
                    public void handleAddrSpaceData(NodeID dest, int space, long hiAddress, long lowAddress, int flags, String desc) {
                        // check contents
                        log.debug("received high Address of {}, low address of {}", hiAddress, lowAddress);
                        endingAddress = hiAddress;
                        service.requestRead(farID, space, lowAddress, (int)Math.min(CHUNKSIZE, endingAddress-lowAddress+1), cbr);
                    }
                };
            // start the process by sending the address space request. It's
            // reply handler will do the first read.
            service.request(cbq);
        } else {
            // kick of read directly, relying on error reply and/or short read for end
            service.requestRead(farID, space, 0, CHUNKSIZE, cbr);  // assume starting address is zero
        }
    }

    MemoryConfigurationService.McsWriteHandler cbw =
        new MemoryConfigurationService.McsWriteHandler() {
            @Override
            public void handleFailure(int errorCode) {
                if (errorCode == 0x1081) {
                    log.error("Write failed. Address space not known");
                    statusField.setText("Write failed. Address space not known.");
                } else if (errorCode == 0x1083) {
                    log.error("Write failed. Address space not writable");
                    statusField.setText("Write failed. Address space not writeable.");
                } else {
                    log.error("Write failed. error code is {}", String.format("%04X", errorCode));
                    statusField.setText("Write failed. error code is "+String.format("%016X", errorCode));
                }
                setRunning(false);
                // return because we're done.
            }

            @Override
            public void handleSuccess() {
                log.trace("Write succeeded {} bytes", address+bytesRead);

                if (cancelled) {
                    log.debug("Cancelled");
                    statusField.setText("Cancelled");
                    setRunning(false);
                    cancelled = false;
                }
                // next operation
                address = address+bytesRead;

                byte[] dataRead;
                try {
                    dataRead = getBytes();
                    if (dataRead == null) {
                        // end of read present
                        setRunning(false);
                        log.debug("Completed");
                        statusField.setText("Completed.");
                        inputStream.close();
                        return;
                    }
                    bytesRead = dataRead.length;
                    log.trace("write {} bytes", bytesRead);
                } catch (IOException ex) {
                    log.error("Error reading file",ex);
                    return;
                }
                service.requestWrite(farID, space, address, dataRead, cbw);
            }
        };

    void pushedPutButton(ActionEvent e) {
        farID = nodeSelector.getSelectedNodeID();
        try {
            space = spaceField.getMemorySpace();
        } catch (NumberFormatException ex) {
            log.error("error parsing the space field value \"{}\"", spaceField.getText());
            statusField.setText("Error parsing the space value");
            setRunning(false);
            return;
        }
        log.debug("Start put");

        if (fileChooser == null) {
            fileChooser = new jmri.util.swing.JmriJFileChooser();
        }
        fileChooser.setDialogTitle("Upload binary file");
        fileChooser.rescanCurrentDirectory();
        fileChooser.setSelectedFile(new File("memory.bin"));

        int retVal = fileChooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) { return; }

        // open file and read first 64 bytes
        File file = fileChooser.getSelectedFile();
        log.debug("access {}", file);

        byte[] dataRead;
        try {
            inputStream = new FileInputStream(file);
            dataRead = getBytes();
            if (dataRead == null) {
                // end of read present
                log.debug("Completed");
                inputStream.close();
                return;
            }
            bytesRead = dataRead.length;
            log.trace("read {} bytes", bytesRead);
        } catch (IOException ex) {
            log.error("Error reading file",ex);
            return;
        }

        // do first memory write
        address = 0;
        setRunning(true);
        service.requestWrite(farID, space, address, dataRead, cbw);
    }

    byte[] bytes = new byte[CHUNKSIZE];
    int bytesRead;          // Number bytes read into the bytes[] array from the file. Used for put operation only.
    InputStream inputStream;
    int address;

    /**
     * Read the next bytes, using the 'bytes' member array.
     *
     * @return null if has reached end of File
     * @throws IOException from underlying file access
     */
    @SuppressFBWarnings(value="PZLA_PREFER_ZERO_LENGTH_ARRAYS", justification="null indicates end of file")
    byte[] getBytes() throws IOException {
        int bytesRead = inputStream.read(bytes); // returned actual number read
        if (bytesRead == -1) return null;  // file done
        if (bytesRead == CHUNKSIZE) return bytes;
        // less data received, have to adjust size of return array
        return Arrays.copyOf(bytes, bytesRead);
    }

    // static to remember choice from one use to another.
    static JFileChooser fileChooser = null;

    /**
     * Nested class to create one of these using old-style defaults
     */
    public static class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("Openlcb Memory Tool",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    MemoryToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemoryToolPane.class);
}
