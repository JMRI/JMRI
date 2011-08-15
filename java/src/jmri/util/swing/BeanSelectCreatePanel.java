package jmri.util.swing;

import jmri.Manager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.ConnectionNameFromSystemName;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import java.util.ArrayList;

public class BeanSelectCreatePanel extends JPanel{
    
    Manager _manager;
    NamedBean _defaultSelect;
    String _reference = null;
    JRadioButton existingItem = new JRadioButton();
    JRadioButton newItem;
    ButtonGroup selectcreate = new ButtonGroup();
    
    JmriBeanComboBox existingCombo;
    JTextField hardwareAddress = new JTextField(10);
    JComboBox prefixBox = new JComboBox();
    jmri.UserPreferencesManager p;
    String systemSelectionCombo = this.getClass().getName()+".SystemSelected";
    
    static java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.util.UtilBundle");
    
    /**
    * Create a JPanel, that provides the option to the user to either select an 
    * already created bean, or to create one on the fly.
    * This only currently works with Turnouts, Sensors, Memories and Blocks.
    */
    public BeanSelectCreatePanel(Manager manager, NamedBean defaultSelect){
        _manager = manager;
        _defaultSelect = defaultSelect;
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        existingItem = new JRadioButton(rb.getString("UseExisting"), true);
        newItem = new JRadioButton(rb.getString("CreateNew"));
        existingItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        newItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        
        selectcreate.add(existingItem);
        selectcreate.add(newItem);
        existingCombo = new JmriBeanComboBox(_manager, defaultSelect, JmriBeanComboBox.SYSTEMNAMEUSERNAME);
        existingCombo.setFirstItemBlank(true);
        JPanel radio = new JPanel();
        JPanel bean = new JPanel();
        radio.add(existingItem);
        radio.add(newItem);
        
        if (_manager instanceof jmri.managers.AbstractProxyManager){
            List<Manager> managerList = new ArrayList<Manager>();
            if(_manager instanceof jmri.TurnoutManager){
                jmri.managers.ProxyTurnoutManager proxy = (jmri.managers.ProxyTurnoutManager) InstanceManager.turnoutManagerInstance();
                managerList = proxy.getManagerList();
            } else if (_manager instanceof jmri.SensorManager){
                jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) InstanceManager.sensorManagerInstance();
                managerList = proxy.getManagerList();
            } else if (_manager instanceof jmri.LightManager){
                jmri.managers.ProxyLightManager proxy = (jmri.managers.ProxyLightManager) InstanceManager.lightManagerInstance();
                managerList = proxy.getManagerList();
            } else if (_manager instanceof jmri.ReporterManager){
                jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) InstanceManager.reporterManagerInstance();
                managerList = proxy.getManagerList();
            }
            for(int x = 0; x<managerList.size(); x++){
                String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                Boolean addToPrefix = true;
                //Simple test not to add a system with a duplicate System prefix
                for (int i = 0; i<prefixBox.getItemCount(); i++){
                    if(((String)prefixBox.getItemAt(i)).equals(manuName))
                        addToPrefix=false;
                }
                if (addToPrefix)
                    prefixBox.addItem(manuName);
            }
            if(p.getComboBoxLastSelection(systemSelectionCombo)!=null)
                prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
        }
        else {
            prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(_manager.getSystemPrefix()));
        }
        
        bean.add(existingCombo);
        bean.add(prefixBox);
        bean.add(hardwareAddress);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(radio);
        add(bean);
        update();
    }
    
    void update(){
        boolean select = true;
        if (newItem.isSelected()) select=false;
        prefixBox.setVisible(false);
        hardwareAddress.setVisible(false);
        existingCombo.setVisible(false);
        if(select) {
            existingCombo.setVisible(true);
        } else {
            prefixBox.setVisible(true);
            hardwareAddress.setVisible(true);
        }
    }
    
    public String getDisplayName(){
        if(existingItem.isSelected()){
            return existingCombo.getSelectedDisplayName();
        } else {
            try {
                NamedBean nBean = createBean();
                return nBean.getDisplayName();
            } catch (jmri.JmriException e){
                return "";
            }
        }
    }
    
    public NamedBean getNamedBean() throws jmri.JmriException {
        if(existingItem.isSelected()){
            return existingCombo.getSelectedBean();
        }
        try {
            return createBean();
        } catch (jmri.JmriException e){
            throw e;
        }
    }
    
    private NamedBean createBean() throws jmri.JmriException{
        String prefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        NamedBean nBean = null;
        if (_manager instanceof jmri.TurnoutManager){
            String sName = "";
            try {
                sName=InstanceManager.turnoutManagerInstance().createSystemName(hardwareAddress.getText(), prefix);
            } catch (jmri.JmriException e){
                throw e;
            }
            try {
                nBean = InstanceManager.turnoutManagerInstance().provideTurnout(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                throw new jmri.JmriException("ErrorTurnoutAddFailed");
            }
        } else if (_manager instanceof jmri.SensorManager){
            String sName = "";
            try {
                sName=InstanceManager.sensorManagerInstance().createSystemName(hardwareAddress.getText(), prefix);
            } catch (jmri.JmriException e){
                throw e;
            }
            try {
                nBean = InstanceManager.sensorManagerInstance().provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                throw new jmri.JmriException("ErrorSensorAddFailed");
            }
        } else {
            String sName = _manager.makeSystemName(hardwareAddress.getText());
            if(_manager instanceof jmri.MemoryManager) {
                try {
                    nBean = InstanceManager.memoryManagerInstance().provideMemory(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    throw new jmri.JmriException("ErrorMemoryAddFailed");
                }
            } else if (_manager instanceof jmri.Block) {
                try {
                    nBean = InstanceManager.blockManagerInstance().provideBlock(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    throw new jmri.JmriException("ErrorBlockAddFailed");
                }
            }
        }
        if (nBean==null)
            throw new jmri.JmriException("Unable to create bean");
        if (_reference!=null && (nBean.getUserName()==null || !nBean.getUserName().equals("")))
            nBean.setUserName(_reference);
        return nBean;
    }

    /**
    * Set a reference that can be set against the userName for a bean, only if
    * the bean has no previous username.
    */
    public void setReference(String ref){
        _reference = ref;
    }
    
    public void dispose(){
        existingCombo.dispose();
    }

}