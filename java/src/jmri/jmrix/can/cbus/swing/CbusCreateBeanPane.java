package jmri.jmrix.can.cbus.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.*;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrix.can.cbus.CbusAddress;
import jmri.jmrix.can.swing.CanPanel;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusNameService;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Panel to Create Turnouts, Sensor and Lights from CBUS event via drop action.
 * 
 * @author Steve Young Copyright (C) 2020
 */
public class CbusCreateBeanPane extends JPanel {
    
    private final CanPanel _mainPane;
    private JLabel beanLabel;
    private JPanel editPanel;
    private JTextField beanUsername;
    private final CbusNameService nameservice;
    private NamedBean bean;
    private JButton editUserNameButton;
    private final Class<?>[] classTypes = new Class<?>[]{
        jmri.TurnoutManager.class,jmri.SensorManager.class,jmri.LightManager.class};
    protected CbTransferHandler[] transferArray;
    
    public CbusCreateBeanPane(CanPanel mainPane){
        super();
        _mainPane = mainPane;
        init();
        nameservice = new CbusNameService(mainPane.getMemo());        
    }
    
    private void init() {
    
        transferArray = new CbTransferHandler[3];
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("CreateNew"))); // NOI18N
        
        add(getNewDragPanel());
        add(getEditPane());
    }
    
    private JPanel getNewDragPanel(){
        
        JPanel dragContainer = new JPanel();
        dragContainer.setToolTipText(Bundle.getMessage("DragHereForNew")); // NOI18N
        dragContainer.setLayout(new BoxLayout(dragContainer, BoxLayout.X_AXIS));
        
        for (int i =0; i<3; i++){
            StringBuilder sb = new StringBuilder();
            sb.append("<html><h2>")
            .append(((jmri.Manager) InstanceManager.getDefault(classTypes[i])).getBeanTypeHandled() )
            .append("</h2></html>"); // NOI18N
            JLabel newBealLabel = new JLabel(sb.toString());
            newBealLabel.setBorder(BorderFactory.createEtchedBorder());
            newBealLabel.setHorizontalAlignment(SwingConstants.CENTER);
            transferArray[i] = new CbTransferHandler();
            newBealLabel.setTransferHandler(transferArray[i]);
            dragContainer.add(newBealLabel);
        }
        return dragContainer;
    }
    
    private JPanel getEditPane(){
    
        editPanel = new JPanel();
        editPanel.setLayout(new BoxLayout(editPanel, BoxLayout.X_AXIS));
        editPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("TslDetails"))); // NOI18N
        
        beanUsername = new JTextField(20);
        beanUsername.setDropTarget(null);
        beanUsername.setMinimumSize(new java.awt.Dimension(60,16));
        
        beanLabel = new JLabel();
        
        editUserNameButton = new JButton(Bundle.getMessage("EditUserName")); // NOI18N
        editUserNameButton.setToolTipText(Bundle.getMessage("EditUNameTip")); // NOI18N
        editUserNameButton.addActionListener((ActionEvent e) -> {
            String newUname = beanUsername.getText();
            bean.setUserName(newUname);
            showSaveReminder();
        });
        
        setEditPaneActive(false);
        
        editPanel.add(beanLabel);
        editPanel.add(beanUsername);
        editPanel.add(editUserNameButton);
        return editPanel;
    }
    
    private void setEditPaneActive( boolean enabled){
        editPanel.setEnabled(enabled);
        beanUsername.setEnabled(enabled);
        editUserNameButton.setEnabled(enabled);
    }
    
    /**
     * Handles drop actions containing CBUS event.
     */
    protected class CbTransferHandler extends javax.swing.TransferHandler {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canImport(JComponent c, DataFlavor[] transferFlavors) {
            for (DataFlavor flavor : transferFlavors) {
                if (DataFlavor.stringFlavor.equals(flavor)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean importData(JComponent c, Transferable t) {
            if (canImport(c, t.getTransferDataFlavors())) {
                
                String validatedAddr;
                Class<?> mgrClass;
                
                // do some validation on the input string
                // processed in the same way as a sensor, turnout or light so less chance of breaking in future
                // and can also accept the Hex "X9001020304;X9101020304" format
                try {
                    validatedAddr = CbusAddress.validateSysName(
                        (String) t.getTransferData(DataFlavor.stringFlavor) );
                    mgrClass = classInString(((JLabel)c).getText());
                } catch (UnsupportedFlavorException | IOException | IllegalArgumentException | ClassCastException e) {
                    return false;
                }
                
                int nn = CbusMessage.getNodeNumber(new CbusAddress(validatedAddr).makeMessage(0));
                int en = CbusMessage.getEvent(new CbusAddress(validatedAddr).makeMessage(0));
                
                boolean dirty = false;
                
                bean = ((jmri.Manager) InstanceManager.getDefault(mgrClass)).getBySystemName(validatedAddr);
                if ( bean == null) {
                    dirty = true;
                    bean = ((jmri.ProvidingManager) InstanceManager.getDefault(mgrClass)).provide(validatedAddr);
                    bean.setUserName(nameservice.getEventName(nn, en));
                }
                setEditPaneActive(true);
                resetBeanText(bean, dirty, nn, en );
                return true;
            }
            return false;
        }
    }
    
    private Class<?> classInString(String dropLocation){
        for (int i =0; i<3; i++){
            if (dropLocation.contains(
                ((jmri.Manager) InstanceManager.getDefault(classTypes[i])).getBeanTypeHandled())){
                return classTypes[i];
            }
        }
        throw new IllegalArgumentException("Unable to get Bean Type");
    }
    
    private void resetBeanText(NamedBean bean, boolean tableChanged, int nn, int en ){
    
        beanLabel.setText(bean.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME));
        editPanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(), nameservice.getEventNodeString(nn,en)));
        beanUsername.setText(bean.getUserName());
        
        if (tableChanged) {
            showSaveReminder();
        }
    }
    
    private boolean showReminder = true;
    
    /**
     * Show reminder to save Bean Table.
     */
    private void showSaveReminder() {
        if (showReminder) {
            JCheckBox checkbox = new JCheckBox(Bundle.getMessage("HideFurtherDialog"));
            Object[] params = {Bundle.getMessage("SavePanelReminder"), checkbox};
            JOptionPane.showMessageDialog (_mainPane, params, 
          Bundle.getMessage("ReminderTitle"), JOptionPane.INFORMATION_MESSAGE); // NOI18N
            showReminder=!checkbox.isSelected();
        }
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusCreateBeanPane.class);
}
