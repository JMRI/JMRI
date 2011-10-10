// AbstractSerialConnectionConfig.java

package jmri.jmrix;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.*;
import javax.swing.*;

import javax.swing.JOptionPane;

import java.awt.Color;
import java.util.Vector;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gnu.io.CommPortIdentifier;

import at.jta.*;

/**
 * Abstract base class for common implementation of the ConnectionConfig
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */

//
abstract public class AbstractSerialConnectionConfig extends AbstractConnectionConfig implements jmri.jmrix.ConnectionConfig {

    /**
     * Ctor for an object being created during load process
     */
    public AbstractSerialConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        adapter = p;
        addToActionList();
        getWindowsSerialPortNames();
    }
    
    public jmri.jmrix.SerialPortAdapter getAdapter() { return adapter; }
    
    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass setInstance() will fill the adapter member.
     */
    public AbstractSerialConnectionConfig() {
        adapter = null;
        addToActionList();
        getWindowsSerialPortNames();
    }

    boolean init = false;
    
    protected void checkInitDone() {
    	if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;

        baudBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureBaudRate((String)baudBox.getSelectedItem());
                p.addComboBoxLastSelection(adapter.getClass().getName()+".baud", (String) portBox.getSelectedItem());
                pref.disallowSave();
            }
        });
        opt1Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureOption1((String)opt1Box.getSelectedItem());
                pref.disallowSave();
            }
        });
        opt2Box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureOption2((String)opt2Box.getSelectedItem());
                pref.disallowSave();
            }
        });
        if(adapter.getSystemConnectionMemo()!=null){
            systemPrefixField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                    pref.disallowSave();
                }
            });
            systemPrefixField.addFocusListener( new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                    pref.disallowSave();
                }
                public void focusGained(FocusEvent e){ }
            });
            connectionNameField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())){
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                    pref.disallowSave();
                }
            });
            connectionNameField.addFocusListener( new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())){
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                    pref.disallowSave();
                }
                public void focusGained(FocusEvent e){ }
            });
        }
        portBox.addFocusListener( new FocusListener() {
            public void focusGained(FocusEvent e){
                refreshPortBox();
            }
            public void focusLost(FocusEvent e){}
        
        });
        init = true;
    }
    jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    //protected ComboBoxRenderer portBoxRenderer = new ComboBoxRenderer();
    protected JComboBox portBox = new JComboBox();
    protected JLabel portBoxLabel;
    protected JComboBox baudBox = new JComboBox();
    protected JLabel baudBoxLabel;
    protected String[] baudList;
    protected jmri.jmrix.SerialPortAdapter adapter = null;

    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    public String getInfo() {
        String t = (String)portBox.getSelectedItem();
        if (t!=null){
            return getPortFromName(t);
            //return t;
        }
        else return JmrixConfigPane.NONE;
    }
    
    Vector<String> v;
    Vector<String> originalList;
    String invalidPort=null;
    
    public void refreshPortBox() {
        if (!init){
            v = getPortNames();
            portBox.setRenderer(new ComboBoxRenderer());
        }
        else {
            Vector<String> v2 = getPortNames();
            if (v2.equals(originalList)){
                log.debug("List of valid Ports has not changed, therefore we will not refresh the port list");
                return;
            }
            log.debug("List of valid Ports has been changed, therefore we will refresh the port list");
            v = new Vector<String>();
            v.setSize(v2.size());
            Collections.copy(v, v2); 
        }
        
        if(v==null){
            log.error("port name Vector v is null!");
        	return;
        }

        /* as we make amendments to the list of port in vector v, we keep a copy of it before
        modification, this copy is then used to validate against any changes in the port lists.
        */
        originalList = new Vector<String>();
        originalList.setSize(v.size());
        Collections.copy(originalList, v); 
        if(portBox.getActionListeners().length >0)
        	portBox.removeActionListener(portBox.getActionListeners()[0]);
        portBox.removeAllItems();
        log.debug("getting fresh list of available Serial Ports");
        
        if (v.size()==0)
        	v.add(0,rb.getString("noPortsFound"));
        String portName = adapter.getCurrentPortName();
        if (portName != null && !portName.equals(rb.getString("noneSelected")) && !portName.equals(rb.getString("noPortsFound"))){
            if(!v.contains(portName)){
                v.add(0,portName);
                invalidPort=portName;
                portBox.setForeground(Color.red);
            } else if (invalidPort!=null && invalidPort.equals(portName)) {
                invalidPort=null;
            }
        } else {
            if (!v.contains(portName)){
                v.add(0,rb.getString("noneSelected"));
            } else if (p.getComboBoxLastSelection(adapter.getClass().getName()+".port")==null){
                v.add(0,rb.getString("noneSelected"));
            }
        }
        updateSerialPortNames(portName, portBox, v);

        portBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String port = getPortFromName((String)portBox.getSelectedItem());
                adapter.setPort(port);
                p.addComboBoxLastSelection(adapter.getClass().getName()+".port", (String) portBox.getSelectedItem());
            }
        });
    }
    
    String value;
   
    public void loadDetails(final JPanel details) {
        _details = details;
        setInstance();

        try {
            v = getPortNames();
    	    if (log.isDebugEnabled()) {
    		    log.debug("loadDetails called in class "+this.getClass().getName());
    		    log.debug("adapter class: "+adapter.getClass().getName());
    		    log.debug("loadDetails called for "+name());
        	    if (v!=null) log.debug("Found "+v.size()+" ports");
        	    else log.debug("Zero-length port vector");
            }
        } catch (java.lang.UnsatisfiedLinkError e1) {
            log.error("UnsatisfiedLinkError - the gnu.io library has not been installed properly");
            log.error("java.library.path="+System.getProperty("java.library.path","<unknown>"));
            javax.swing.JOptionPane.showMessageDialog(null, "Failed to load comm library.\nYou have to fix that before setting preferences.");
            return;
        }
        
        if(adapter.getSystemConnectionMemo()!=null){
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
            NUMOPTIONS=NUMOPTIONS+2;
        }

        refreshPortBox();
        
        baudList = adapter.validBaudRates();
        // need to remove ActionListener before addItem() or action event will occur
        if(baudBox.getActionListeners().length >0)
        	baudBox.removeActionListener(baudBox.getActionListeners()[0]);
        baudBox.removeAllItems();
    	if (log.isDebugEnabled()) log.debug("after remove, "+baudBox.getItemCount()+" items, first is "
    											+baudBox.getItemAt(0));        
        for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
    	if (log.isDebugEnabled()) log.debug("after reload, "+baudBox.getItemCount()+" items, first is "
    											+baudBox.getItemAt(0));        
    	
        opt1List = adapter.validOption1();
        opt1BoxLabel = new JLabel(adapter.option1Name());
        // need to remove ActionListener before addItem() or action event will occur
        if(opt1Box.getActionListeners().length >0)
        	opt1Box.removeActionListener(opt1Box.getActionListeners()[0]);
        opt1Box.removeAllItems();
        for (int i=0; i<opt1List.length; i++) opt1Box.addItem(opt1List[i]);
        opt2BoxLabel = new JLabel(adapter.option2Name());
        opt2List = adapter.validOption2();
        // need to remove ActionListener before addItem() or action event will occur
        if(opt2Box.getActionListeners().length >0)
        	opt2Box.removeActionListener(opt2Box.getActionListeners()[0]);
        opt2Box.removeAllItems();
        for (int i=0; i<opt2List.length; i++) opt2Box.addItem(opt2List[i]);

        if (baudList.length>1) {
            baudBox.setToolTipText("Must match the baud rate setting of your hardware");
            baudBox.setEnabled(true);
        } else {
            baudBox.setToolTipText("The baud rate is fixed for this protocol");
            baudBox.setEnabled(false);
        }
        if (opt1List.length>1) {
            NUMOPTIONS++;
            opt1Box.setToolTipText("The first option is strongly recommended. See README for more info.");
            opt1Box.setEnabled(true);
            opt1Box.setSelectedItem(adapter.getCurrentOption1Setting());
        } else {
            opt1Box.setToolTipText("There are no options for this protocol");
            opt1Box.setEnabled(false);
        }
        if (opt2List.length>1) {
            NUMOPTIONS++;
            opt2Box.setToolTipText("");
            opt2Box.setEnabled(true);
            opt2Box.setSelectedItem(adapter.getCurrentOption2Setting());
        } else {
            opt2Box.setToolTipText("There are no options for this protocol");
            opt2Box.setEnabled(false);
        }

        portBoxLabel = new JLabel("Serial port: ");
        
        baudBoxLabel = new JLabel("Baud rate:");
        baudBox.setSelectedItem(adapter.getCurrentBaudRate());        
        showAdvanced.setFont(showAdvanced.getFont().deriveFont(9f));
        showAdvanced.setForeground(Color.blue);
        showAdvanced.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e){
                    showAdvancedItems();
                }
            });
        showAdvancedItems();
        init = false;		// need to reload action listeners
        checkInitDone();
    }
    
    protected void showAdvancedItems(){
        _details.removeAll();
        int stdrows = 0;
        boolean incAdvancedOptions=true;
        if(!isBaudAdvanced()) stdrows++;
        if(!isPortAdvanced()) stdrows++;
        if ((!isOptList1Advanced())&&(opt1List.length>1)) stdrows++;
        if ((!isOptList2Advanced())&&(opt2List.length>1)) stdrows++;
        if(adapter.getSystemConnectionMemo()!=null) stdrows=stdrows+2;
        if (stdrows == NUMOPTIONS){
            incAdvancedOptions=false;
        } else{
            stdrows++;
        }
        if (showAdvanced.isSelected()) {
            int advrows = stdrows;
            if(isBaudAdvanced()) advrows++;
            if(isPortAdvanced()) advrows++;
            if ((isOptList1Advanced())&&(opt1List.length>1)) advrows++;
            if ((isOptList2Advanced())&&(opt2List.length>1)) advrows++;
            _details.setLayout(new GridLayout(advrows,2));
            addStandardDetails(incAdvancedOptions);
            if(isPortAdvanced()){
                _details.add(portBoxLabel);
                _details.add(portBox);
            }
            
            if(isBaudAdvanced()){
                _details.add(baudBoxLabel);
                _details.add(baudBox);
            }
            if ((isOptList1Advanced())&&(opt1List.length>1)) {
                _details.add(opt1BoxLabel);
                _details.add(opt1Box);
            }
            if ((isOptList2Advanced())&&(opt2List.length>1)) {
                _details.add(opt2BoxLabel);
                _details.add(opt2Box);
            }
        } else {
            _details.setLayout(new GridLayout(stdrows,2));
            addStandardDetails(incAdvancedOptions);
        }
        _details.validate();
        if (_details.getTopLevelAncestor()!=null){
            ((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).setSize(((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).getPreferredSize());
            ((jmri.util.JmriJFrame)_details.getTopLevelAncestor()).pack();
        }
        _details.repaint();
    }
    
    protected void addStandardDetails(boolean incAdvanced){
        if(!isPortAdvanced()){
            _details.add(portBoxLabel);
            _details.add(portBox);
        }
        
        if(!isBaudAdvanced()){
            _details.add(baudBoxLabel);
            _details.add(baudBox);
        }
        
        if ((!isOptList1Advanced())&&(opt1List.length>1)){
            _details.add(opt1BoxLabel);
            _details.add(opt1Box);
        }
        
        if ((!isOptList2Advanced())&&(opt2List.length>1)) {
            _details.add(opt2BoxLabel);
            _details.add(opt2Box);
        }
        if(adapter.getSystemConnectionMemo()!=null){
            _details.add(systemPrefixLabel);
            _details.add(systemPrefixField);
            _details.add(connectionNameLabel);
            _details.add(connectionNameField);
        }
        if (incAdvanced){
            _details.add(new JLabel(" "));
            _details.add(showAdvanced);
        }
    }
    
    public boolean isPortAdvanced() { return false; }
    public boolean isBaudAdvanced() { return true; }
    
    public String getManufacturer() { return adapter.getManufacturer(); }
        
    public void setManufacturer(String manufacturer) { 
        if (adapter!=null){
            adapter.setManufacturer(manufacturer); 
        }
    }
    
    public boolean getDisabled() {
        if (adapter==null) return true;
        return adapter.getDisabled();
    }
    
    public void setDisabled(boolean disabled) {
        if(adapter!=null)
            adapter.setDisabled(disabled);
    }
    
    public String getConnectionName() { 
        if((adapter!=null) && (adapter.getSystemConnectionMemo()!=null))
            return adapter.getSystemConnectionMemo().getUserName();
        else return null;
    }
    
    public void dispose(){
        if (adapter!=null){
            adapter.dispose();
            adapter=null;
        }
        removeFromActionList();
    }
    
    class ComboBoxRenderer extends JLabel
                       implements ListCellRenderer {
                       
        public ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(
                                           JList list,
                                           Object value,
                                           int index,
                                           boolean isSelected,
                                           boolean cellHasFocus) {

            String displayName = value.toString();
            setForeground(Color.black);
            list.setSelectionForeground(Color.black);
            if(isSelected){
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
            if(invalidPort!=null){
                String port = getPortFromName(displayName);
                if (port.equals(invalidPort)){
                    list.setSelectionForeground(Color.red);
                    setForeground(Color.red);
                }
            }

            setText(displayName);

            return this;
        }
    }
    
    static boolean portsRetrieved = false;
    /*
    * We only go through the windows registry once looking for friendly names
    * if a new device is added then the new friendly name will not be picked up.
    */
    static synchronized void getWindowsSerialPortNames(){
        if(portsRetrieved)
            return;
        /* Retrieving the friendly name is only available to windows clients 
           so if the OS is not windows, we make the portsRetrieved as completed
           and exit out.
        */
        if(!System.getProperty("os.name").toLowerCase().contains("windows")){
            portsRetrieved = true;
            return;
        }
        
        try {
            Regor reg = new Regor();
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\FTDIBUS\\", reg);
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\USB\\", reg);
            //some modems are assigned in the HDAUDIO se we retrieve these
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\HDAUDIO\\", reg);
            //some PCI software devices are located here
            getDetailsFromWinRegistry("SYSTEM\\CurrentControlSet\\Enum\\PCI\\", reg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        portsRetrieved = true;
    }
    
    
    
    private static void getDetailsFromWinRegistry(String path, Regor reg){
        ArrayList<String> friendlyName = new ArrayList<String>();
        try {
            List regentry = reg.listKeys(Regor.HKEY_LOCAL_MACHINE,path);
            if(regentry==null)
                return;
            for(int i = 0; i<regentry.size(); i++){
                List regSubEntry = reg.listKeys(Regor.HKEY_LOCAL_MACHINE, path + regentry.get(i));
                if(regSubEntry!=null){
                    if(regSubEntry.size()>0){
                        String name = null;
                        String port = null;
                        List values = reg.listValueNames(Regor.HKEY_LOCAL_MACHINE,path + regentry.get(i)+"\\"+regSubEntry.get(0));
                        if(values.contains("Class")){
                            Key pathKey = reg.openKey(Regor.HKEY_LOCAL_MACHINE, path + regentry.get(i)+"\\"+regSubEntry.get(0), Regor.KEY_READ);
                            String deviceClass = reg.readValueAsString(pathKey,"Class");
                            if(deviceClass.equals("Ports") || deviceClass.equals("Modem")){
                                name = reg.readValueAsString(pathKey,"FriendlyName");
                                Key pathKey2 = reg.openKey(Regor.HKEY_LOCAL_MACHINE, path + regentry.get(i)+"\\"+regSubEntry.get(0)+"\\Device Parameters", Regor.KEY_READ);
                                port = reg.readValueAsString(pathKey2, "PortName");
                                reg.closeKey(pathKey2);
                            }
                            reg.closeKey(pathKey);
                        }
                        if((name!=null) && (port!=null)){
                            serialPortNames.put(port, new SerialPortFriendlyName(port, name));
                        } else if(name!=null)
                            friendlyName.add(name);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i<friendlyName.size(); i++){
            int commst = friendlyName.get(i).lastIndexOf('(')+1;
            int commls = friendlyName.get(i).lastIndexOf(')');
            String commPort = friendlyName.get(i).substring(commst, commls);
            serialPortNames.put(commPort, new SerialPortFriendlyName(commPort, friendlyName.get(i)));
        }
    }
    
    protected synchronized static void updateSerialPortNames(String portName, JComboBox portCombo, Vector<String> portList){
        for(Entry<String, SerialPortFriendlyName> en : serialPortNames.entrySet()){
            en.getValue().setValidPort(false);
        }
        
        for (int i=0; i<portList.size(); i++) {
            String commPort = portList.elementAt(i);
            SerialPortFriendlyName port = serialPortNames.get(commPort);
            if(port==null){
                port = new SerialPortFriendlyName(commPort, null);
                serialPortNames.put(commPort, port);
            }
            port.setValidPort(true);
            portCombo.addItem(port.getDisplayName());
            if(commPort.equals(portName)){
                portCombo.setSelectedIndex(i);
            }
        }
    }
    
    static String getPortFromName(String name){
        for(Entry<String, SerialPortFriendlyName> en : serialPortNames.entrySet()){
            if(en.getValue().getDisplayName().equals(name))
                return en.getKey();
        }
        return "";
    }
    
    @SuppressWarnings("unchecked")
	protected Vector<String> getPortNames() {
    	//reloadDriver(); // Refresh the list of communication ports
        // first, check that the comm package can be opened and ports seen
        Vector<String> portNameVector = new Vector<String>();
        Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = portIDs.nextElement();
            // filter out line printers 
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL)
            	// accumulate the names in a vector
            	portNameVector.addElement(id.getName());
		  }
         return portNameVector;
    }
    
    static HashMap<String, SerialPortFriendlyName> serialPortNames = new HashMap<String, SerialPortFriendlyName>();
    
    static class SerialPortFriendlyName{
        
        String serialPortFriendly = "";
        boolean valid = false;
        
        SerialPortFriendlyName(String port, String Friendly){
            serialPortFriendly = Friendly;
            if(serialPortFriendly==null)
                serialPortFriendly=port;
            else if(!serialPortFriendly.contains(port)){
                serialPortFriendly = Friendly+" ("+port+")";
            }
        }
        
        String getDisplayName(){
            return serialPortFriendly;
        }
        
        boolean isValidPort(){ return valid; }
        
        void setValidPort(boolean boo) { valid = boo; }
    
    }
    
    /**
    * This is purely here for systems that do not impliement the SystemConnectionMemo
    * Acela, CAN BUS, CMRI, Grapevine, QSI, Zimo & RPS and can be removed one they have been migrated
    */
    protected ResourceBundle getActionModelResourceBundle(){
        return null;
    }
    
    protected void addToActionList(){
        apps.CreateButtonModel bm = jmri.InstanceManager.getDefault(apps.CreateButtonModel.class);
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb==null || bm==null)
            return;
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                bm.addAction(key, rb.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class "+key);
            }
        }
    }
    
    protected void removeFromActionList(){
        apps.CreateButtonModel bm = jmri.InstanceManager.getDefault(apps.CreateButtonModel.class);
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb==null || bm==null)
            return;
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                bm.removeAction(key);
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class "+key);
            }
        }
    }

    final static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractSerialConnectionConfig.class.getName());
}