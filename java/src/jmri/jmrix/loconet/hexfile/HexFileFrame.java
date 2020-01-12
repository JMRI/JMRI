package jmri.jmrix.loconet.hexfile;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import jmri.*;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.JmriJFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to inject LocoNet messages from a hex file. This is a sample frame that
 * drives a test App. It controls reading from a .hex file, feeding the
 * information to a LocoMonFrame (monitor) and connecting to a LocoGenFrame (for
 * sending a few commands).
 *
 * @author Bob Jacobsen Copyright 2001, 2002
 * @author i18n Egbert Broerse 2017
 */
public class HexFileFrame extends JmriJFrame {

    // member declarations
    javax.swing.JButton openHexFileButton = new javax.swing.JButton();
    javax.swing.JButton filePauseButton = new javax.swing.JButton();
    javax.swing.JButton jButton1 = new javax.swing.JButton();
    javax.swing.JTextField delayField = new javax.swing.JTextField(5);
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    
    private int connectedAddresses = 0;

    // to find and remember the log file
    final javax.swing.JFileChooser inputFileChooser;

    /**
     * Because this creates a FileChooser, this should be invoked on the
     * GUI frame.
     */
    @InvokeOnGuiThread
    public HexFileFrame() {
        super();
        inputFileChooser = jmri.jmrit.XmlFile.userFileChooser("Hex files", "hex"); // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @InvokeOnGuiThread
    @Override
    public void initComponents() {
        if (port == null) {
            log.error("initComponents called before adapter has been set");
        }
        // the following code sets the frame's initial state

        openHexFileButton.setText(Bundle.getMessage("OpenFile"));
        openHexFileButton.setVisible(true);
        openHexFileButton.setToolTipText(Bundle.getMessage("OpenFileTooltip"));

        filePauseButton.setText(Bundle.getMessage("ButtonPause"));
        filePauseButton.setVisible(true);
        filePauseButton.setToolTipText(Bundle.getMessage("ButtonPauseTooltip"));

        jButton1.setText(Bundle.getMessage("ButtonContinue"));
        jButton1.setVisible(true);
        jButton1.setToolTipText(Bundle.getMessage("ButtonContinueTooltip"));

        delayField.setText("200");
        delayField.setVisible(true);
        delayField.setToolTipText(Bundle.getMessage("DelayTooltip"));

        jLabel1.setText(Bundle.getMessage("FieldDelay"));
        jLabel1.setVisible(true);

        setTitle(Bundle.getMessage("TitleLocoNetSimulator"));
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
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openHexFileButtonActionPerformed(e);
            }
        });
        filePauseButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                filePauseButtonActionPerformed(e);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                jButton1ActionPerformed(e);
            }
        });
        delayField.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                delayFieldActionPerformed(e);
            }
        });
    }

    boolean connected = false;

    @Override
    @InvokeOnGuiThread
    public void dispose() {
        // leaves the LocoNet Packetizer (e.g. the simulated connection)
        // running so that the application can keep pretending to run with the window closed.
        super.dispose();
    }

    LnPacketizer packets = null;

    @InvokeOnGuiThread
    public void openHexFileButtonActionPerformed(java.awt.event.ActionEvent e) {
        // select the file
        // start at current file, show dialog
        inputFileChooser.rescanCurrentDirectory();
        int retVal = inputFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }
        // call load to process the file
        port.load(inputFileChooser.getSelectedFile());

        // wake copy
        sourceThread.interrupt();  // really should be using notifyAll instead....

        // reach here while file runs.  Need to return so GUI still acts,
        // but that normally lets the button go back to default.
    }

    @InvokeOnGuiThread
    public void configure() {
        if (port == null) {
            log.error("configure called before adapter has been set");
            return;
        }
        // connect to a packetizing LnTrafficController
        packets = new LnPacketizer(port.getSystemConnectionMemo());
        packets.connectPort(port);
        connected = true;

        // create memo
        port.getSystemConnectionMemo().setLnTrafficController(packets);

        // do the common manager config
        port.getSystemConnectionMemo().configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, // full featured by default
                false, false, false);
        port.getSystemConnectionMemo().configureManagers();
        jmri.SensorManager sm = port.getSystemConnectionMemo().getSensorManager();
        if (sm != null) {
            if ( sm instanceof LnSensorManager) {
                ((LnSensorManager) sm).setDefaultSensorState(port.getOptionState("SensorDefaultState")); // NOI18N
            } else {
                log.info("SensorManager referenced by port is not an LnSensorManager. Have not set the default sensor state.");
            }
        }

        // Install a debug programmer, replacing the existing LocoNet one
        DefaultProgrammerManager ep = port.getSystemConnectionMemo().getProgrammerManager();
        port.getSystemConnectionMemo().setProgrammerManager(
                new jmri.progdebugger.DebugProgrammerManager(port.getSystemConnectionMemo()));
        if (port.getSystemConnectionMemo().getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.store(port.getSystemConnectionMemo().getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (port.getSystemConnectionMemo().getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(port.getSystemConnectionMemo().getProgrammerManager(), GlobalProgrammerManager.class);
        }
        jmri.InstanceManager.deregister(ep, jmri.AddressedProgrammerManager.class);
        jmri.InstanceManager.deregister(ep, jmri.GlobalProgrammerManager.class);

        // Install a debug throttle manager and override 
        DebugThrottleManager tm = new DebugThrottleManager(port.getSystemConnectionMemo() ) {
            /**
             * Only address 128 and above can be a long address
             */
            @Override
            public boolean canBeLongAddress(int address) {
                return (address >= 128);
            }

            @Override
            public void requestThrottleSetup(LocoAddress a, boolean control) {
                if (!(a instanceof DccLocoAddress)) {
                    log.error("{} is not a DccLocoAddress",a);
                    failedThrottleRequest(a, "LocoAddress " + a + " is not a DccLocoAddress");
                    return;
                }
                connectedAddresses++;
                DccLocoAddress address = (DccLocoAddress) a;
                //create some testing situations
                if (connectedAddresses > 5) {
                    log.warn("SLOT MAX of 5 exceeded");
                    failedThrottleRequest(address, "SLOT MAX of 5 exceeded");
                    return;
                }
                // otherwise, continue with setup
                super.requestThrottleSetup(a, control);
            }

            @Override
            public boolean disposeThrottle(DccThrottle t, jmri.ThrottleListener l) {
                connectedAddresses--;
                return super.disposeThrottle(t, l);
            }    
        };

        port.getSystemConnectionMemo().setThrottleManager(tm);
        jmri.InstanceManager.setThrottleManager(
                port.getSystemConnectionMemo().getThrottleManager());

        // start operation of packetizer
        packets.startThreads();
        sourceThread = new Thread(port, "LocoNet HexFileFrame");
        sourceThread.start();
    }

    @SuppressWarnings("deprecation")  // Thread.suspend() not being removed
    public void filePauseButtonActionPerformed(java.awt.event.ActionEvent e) {
        sourceThread.suspend();
    }

    @SuppressWarnings("deprecation")  // Thread.resume() not being removed
    public void jButton1ActionPerformed(java.awt.event.ActionEvent e) {  // resume button
        sourceThread.resume();
    }

    public void delayFieldActionPerformed(java.awt.event.ActionEvent e) {
        // if the hex file has been started, change its delay
        if (port != null) {
            port.setDelay(Integer.parseInt(delayField.getText()));
        }
    }

    Thread sourceThread;  // tests need access

    public void setAdapter(LnHexFilePort adapter) {
        port = adapter;
    }

    public LnHexFilePort getAdapter() {
        return port;
    }
    private LnHexFilePort port = null;

    private final static Logger log = LoggerFactory.getLogger(HexFileFrame.class);

}
