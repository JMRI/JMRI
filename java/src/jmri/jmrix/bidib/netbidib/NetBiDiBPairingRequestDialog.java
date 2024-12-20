package jmri.jmrix.bidib.netbidib;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
//import javax.swing.Timer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import jmri.jmrix.bidib.BiDiBPortController;

import org.bidib.jbidibc.messages.helpers.Context;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.bidib.jbidibc.netbidib.NetBidibContextKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This dialog is presented to the user if netBiDiB pairing is required.
 * It informs the user about the remote device and waits until the pairing is
 * accepted on the remote side. In this case, the dialog should be removed by the
 * calling program. If the request timed out, is is closed an the calling program
 * is informed by an ActionListener.
 * 
 * @author Eckart Meyer Copyright (C) 2024
 */

public class NetBiDiBPairingRequestDialog {
    
    //private final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.bidib.BiDiBBundle"); // NOI18N

    private final Context context;
    private final BiDiBPortController portController;
    private final Long uniqueId;
    private Integer remainingTimeout;
    private JOptionPane optionPane;
    private final JDialog dialog = new JDialog();
    private final ActionListener actionListener;

    private final javax.swing.Timer updateTimer = new javax.swing.Timer(1000, e -> updateDialog());
    
    /**
     * Constructor.
     * 
     * @param context to transfer several parameters to show on the dialog
     * @param portController to transfer the port hostname and port number
     * @param listener listing to the cancel event
     */
    public NetBiDiBPairingRequestDialog(Context context, BiDiBPortController portController, ActionListener listener) {
        this.context = context;
        this.portController = portController;
        actionListener = listener;
        uniqueId = context.get(NetBidibContextKeys.KEY_DESCRIPTOR_UID, Long.class, null);
        remainingTimeout = context.get(NetBidibContextKeys.KEY_PAIRING_TIMEOUT, Integer.class, Integer.valueOf(30));
    }
    

    /**
     * Show the dialog, setup listeners for the button and for the window close event.
     * Immediately return, this is not a modal dialog.
     */
    public void show() {
        

        String[] options = new String[1];
        options[0] = Bundle.getMessage("netBiDiBPairingDialogCancel"); // NOI18N
        String title = Bundle.getMessage("netBiDiBPairingDialogTitle") ;  // NOI18N

        optionPane = new JOptionPane(makeMessageText(), JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, options, null);
        
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
                
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();
                //log.trace("property change event: {}", e);
                //log.trace("propertychange: name: {}, old value: {}, new value: {}", prop, e.getOldValue(), e.getNewValue());

                if (dialog.isVisible() 
                 && (e.getSource() == optionPane)
                 && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                    log.trace("propertychange new value: {}", e.getNewValue());
                    onCancel();
                }
            }
        });
        
        
        
        dialog.setTitle(title);
        dialog.setModal(false); //true: setVisible() would not return until dispose()
        dialog.setContentPane(optionPane);
        dialog.setLocationRelativeTo(null); // Centers on the screen
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); //would inhibit closing the window from the user
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                log.debug("user closes window.");
                onCancel();
            }
        });

        updateTimer.setRepeats(true);  // 1s tick

        //  timer removes dialog after iDelayInSeconds
        updateTimer.start();


        dialog.pack();
        dialog.setVisible(true);
    }
    
    /**
     * Stop timer and remove dialog
     */
    public void dispose() {
        updateTimer.stop();
        dialog.dispose();
    }
    
    /**
     * Stop timer and hide dialog
     */
    public void hide() {
        updateTimer.stop();
        dialog.setVisible(false);
    }
    
    /**
     * Call the listener on the cancel event
     */
    private void onCancel() {
        log.info("User cancelled netBiDiB pairing");
        dispose();
        if (actionListener != null) {
            actionListener.actionPerformed(new ActionEvent(this, 0, "Cancel"));
        }
    }
    
    /**
     * Update the dialog message.
     * The text changes every second to display the remaining time.
     */
    private void updateDialog() {
        remainingTimeout -= 1;
        if (remainingTimeout > 0) {
            optionPane.setMessage(makeMessageText());
        }
        else {
            dispose();
        }
    }

    /**
     * Setup the text to be presented to the user.
     * The data is taken from the Context given by the calling program.
     * 
     * @return the complete string
     */
    private String makeMessageText() {
        String remoteLinkData = Bundle.getMessage("netBiDiBPairingDialogText") + "\n";
        remoteLinkData += "\n" + Bundle.getMessage("netBiDiBPairingDialogTime") + ": " + remainingTimeout.toString() + Bundle.getMessage("netBiDiBPairingDialogSecondsShort");
        remoteLinkData += "\n" + Bundle.getMessage("netBiDiBPairingDialogProdName") + ": " + context.get(NetBidibContextKeys.KEY_DESCRIPTOR_PROD_STRING, String.class, "");
        remoteLinkData += "\n" + Bundle.getMessage("netBiDiBPairingDialogUserName") + ": " + context.get(NetBidibContextKeys.KEY_DESCRIPTOR_USER_STRING, String.class, "");
        remoteLinkData += "\n" + Bundle.getMessage("netBiDiBPairingDialogPortName") + ": " + portController.getRealPortName();
        remoteLinkData += "\n" + Bundle.getMessage("netBiDiBPairingDialogUniqueID") + ": " + ByteUtils.getUniqueIdAsString(uniqueId);
        
        return remoteLinkData;
    }
    
    private final static Logger log = LoggerFactory.getLogger(NetBiDiBPairingRequestDialog.class);

}
