// HexFileFrame.java

package jmri.jmrix.loconet.hexfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.*;
import jmri.util.JmriJFrame;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * Frame to inject LocoNet messages from a hex file
 * This is a sample frame that drives a test App.  It controls reading from
 * a .hex file, feeding the information to a LocoMonFrame (monitor) and
 * connecting to a LocoGenFrame (for sending a few commands).
 * @author			Bob Jacobsen  Copyright 2001, 2002
 * @version                     $Revision$
 */
public class HexFileFrame extends JmriJFrame {

    // member declarations
    javax.swing.JButton openHexFileButton = new javax.swing.JButton();
    javax.swing.JButton filePauseButton = new javax.swing.JButton();
    javax.swing.JButton jButton1 = new javax.swing.JButton();
    javax.swing.JTextField delayField = new javax.swing.JTextField(5);
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

    // to find and remember the log file
    final javax.swing.JFileChooser inputFileChooser = 
            jmri.jmrit.XmlFile.userFileChooser("Hex files", "hex");

    public HexFileFrame() {
        super();
        //adaptermemo = new LocoNetSystemConnectionMemo();
    }
    
    //LocoNetSystemConnectionMemo adaptermemo = null;

    public void initComponents() throws Exception {
        if (port==null){
            log.error("initComponents called before adapter has been set");
        }
        // the following code sets the frame's initial state

        openHexFileButton.setText("Open file");
        openHexFileButton.setVisible(true);
        openHexFileButton.setToolTipText("run from hex file");

        filePauseButton.setText("Pause");
        filePauseButton.setVisible(true);
        filePauseButton.setToolTipText("pauses the trace at the source");

        jButton1.setText("Continue");
        jButton1.setVisible(true);
        jButton1.setToolTipText("continues the trace at the source");

        delayField.setText("200");
        delayField.setVisible(true);
        delayField.setToolTipText("delay (in milliseconds) between commands");

        jLabel1.setText("Delay:");
        jLabel1.setVisible(true);

        setTitle("LocoNet Simulator");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(openHexFileButton);
        pane1.add(new JPanel()); // dummy
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        pane2.add(jLabel1);
        pane2.add(delayField);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        pane3.add(filePauseButton);
        pane3.add(jButton1);
        getContentPane().add(pane3);


        openHexFileButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    openHexFileButtonActionPerformed(e);
                }
            });
        filePauseButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    filePauseButtonActionPerformed(e);
                }
            });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    jButton1ActionPerformed(e);
                }
            });
        delayField.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    delayFieldActionPerformed(e);
                }
            });

        // create a new Hex file handler, set its delay
        //port = new LnHexFilePort();
        //port.setDelay(Integer.valueOf(delayField.getText()).intValue());

        // and make the connections
        //configure();
    }

    boolean connected = false;

    public void dispose() {
        // leaves the LocoNet Packetizer (e.g. the simulated connection)
        // running.
        super.dispose();

    }
    
    LnPacketizer packets = null;

    public void openHexFileButtonActionPerformed(java.awt.event.ActionEvent e) {
        // select the file
        // start at current file, show dialog
        inputFileChooser.rescanCurrentDirectory();
        int retVal = inputFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected

        // call load to process the file
        port.load(inputFileChooser.getSelectedFile());

        // wake copy
        sourceThread.interrupt();

        // reach here while file runs.  Need to return so GUI still acts,
        // but that normally lets the button go back to default.

    }

    public void configure() {
        if (port==null){
            log.error("initComponents called before adapter has been set");
            return;
        }
        // connect to a packetizing LnTrafficController
        packets = new LnPacketizer();
        packets.connectPort(port);
        connected = true;

        // create memo
        port.getAdapterMemo().setSlotManager(new SlotManager(packets));
        port.getAdapterMemo().setLnTrafficController(packets);

        // do the common manager config
        port.getAdapterMemo().configureCommandStation(true, false, "<unknown>",   // full featured by default
                                            false, false);
        port.getAdapterMemo().configureManagers();

        // Install a debug programmer, replacing the existing LocoNet one
        port.getAdapterMemo().setProgrammerManager(
                new jmri.progdebugger.DebugProgrammerManager(port.getAdapterMemo()));
        jmri.InstanceManager.setProgrammerManager(
                port.getAdapterMemo().getProgrammerManager());

        // Install a debug throttle manager, replacing the existing LocoNet one
        port.getAdapterMemo().setThrottleManager(new jmri.jmrix.debugthrottle.DebugThrottleManager(port.getAdapterMemo()));
        jmri.InstanceManager.setThrottleManager(
                port.getAdapterMemo().getThrottleManager());

        // start operation of packetizer
        packets.startThreads();
        sourceThread = new Thread(port);
        sourceThread.start();
            
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }

    @SuppressWarnings("deprecation")
    public void filePauseButtonActionPerformed(java.awt.event.ActionEvent e) {
        sourceThread.suspend();
        // sinkThread.suspend(); // allow sink to catch up
    }

    @SuppressWarnings("deprecation")
    public void jButton1ActionPerformed(java.awt.event.ActionEvent e) {  // resume button
	sourceThread.resume();
        // sinkThread.resume();
    }

    public void delayFieldActionPerformed(java.awt.event.ActionEvent e) {
        // if the hex file has been started, change its delay
        if (port!=null) port.setDelay(Integer.valueOf(delayField.getText()).intValue());
    }



    private Thread sourceThread;
    //private Thread sinkThread;
    
    public void setAdapter(LnHexFilePort adapter) { port = adapter; }
    public LnHexFilePort getAdapter() { return port; }
    private LnHexFilePort port = null;

    static Logger log = LoggerFactory.getLogger(HexFileFrame.class.getName());

}
