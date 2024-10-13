package jmri.jmrix.openlcb.swing.idtool;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.swing.WrapLayout;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.swing.*;


/**
 * Pane for identifying a physical node by
 * doing memory operations (hence lighting its activity lights)
 * until cancelled.
 *
 * @author Bob Jacobsen Copyright (C) 2024
 * @since 5.7.4
 */
public class IdToolPane extends jmri.util.swing.JmriPanel
        implements jmri.jmrix.can.swing.CanPanelInterface {

    protected CanSystemConnectionMemo memo;
    Connection connection;
    NodeID nid;

    MimicNodeStore store;
    MemoryConfigurationService service;
    NodeSelector nodeSelector;

    public String getTitle(String menuTitle) {
        return Bundle.getMessage("TitleIdTool");
    }

    static final int CHUNKSIZE = 64;

    JButton gb;
    JButton cb;
    boolean cancelled = false;
    boolean running = false;

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

        var bb = new JPanel();
        bb.setLayout(new WrapLayout());
        add(bb);

        gb = new JButton(Bundle.getMessage("ButtonId"));
        bb.add(gb);
        gb.addActionListener(this::pushedGetButton);

        cb = new JButton(Bundle.getMessage("ButtonCancel"));
        bb.add(cb);
        cb.addActionListener(this::pushedCancel);

        setRunning(false);
    }

    public IdToolPane() {
    }

    @Override
    public void dispose() {
        // and complete this
        super.dispose();
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.openlcb.swing.idtool.IdToolPane";
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " ID Tool");
        }
        return getTitle(Bundle.getMessage("TitleIdTool"));
    }

    void pushedCancel(ActionEvent e) {
        if (running) {
            cancelled = true;
        }
    }

    void setRunning(boolean t) {
        if (t) {
            gb.setEnabled(false);
            cb.setEnabled(true);
        } else {
            gb.setEnabled(true);
            cb.setEnabled(false);
        }
        running = t;
    }

    int space = 0xFF;

    NodeID farID = new NodeID("0.0.0.0.0.0");

    static final int PERIOD = 250;  // delay in milliseconds 
    
    MemoryConfigurationService.McsReadHandler cbr =
        new MemoryConfigurationService.McsReadHandler() {
            @Override
            public void handleFailure(int errorCode) {
                setRunning(false);
                if (errorCode == 0x1082) {
                    log.debug("Stopping read due to 0x1082 status");
                } if (errorCode == 0x1081) {
                    log.error("Read failed. Address space not known");
                } else {
                    log.error("Read failed. Error code is {}", String.format("%04X", errorCode));
                }
            }

            @Override
            public void handleReadData(NodeID dest, int readSpace, long readAddress, byte[] readData) {
                log.trace("read succeed with {} bytes at {}", readData.length, readAddress);
                // fire another from same address
                if (!cancelled) {
                    // send after a delay
                    jmri.util.ThreadingUtil.runOnLayoutDelayed(() -> {
                        service.requestRead(farID, space, 0,
                                        CHUNKSIZE,
                                        cbr);
                    }, PERIOD);
                } else {
                    setRunning(false);
                    cancelled = false;
                    log.debug("Complete");
                }
            }
        };


    /**
     * Starts reading from node and writing to file process
     * @param e not used
     */
    void pushedGetButton(ActionEvent e) {
        setRunning(true);
        farID = nodeSelector.getSelectedNodeID();
        service.requestRead(farID, space, 0, CHUNKSIZE, cbr);  // assume starting address is zero
    }

    byte[] bytes = new byte[CHUNKSIZE];
    int bytesRead;          // Number bytes read into the bytes[] array from the file. Used for put operation only.
    InputStream inputStream;
    int address;

    /**
     * Nested class to create one of these using old-style defaults
     */
    public static class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("Openlcb ID Tool",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    IdToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdToolPane.class);
}
