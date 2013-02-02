// AbstractSerialConnectionConfig.java

package jmri.jmrix;

import org.apache.log4j.Logger;
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
import java.util.Map.Entry;
import java.util.Hashtable;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gnu.io.CommPortIdentifier;

import jmri.util.PortNameMapper;
import jmri.util.PortNameMapper.SerialPortFriendlyName;

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
    public AbstractSerialConnectionConfig(jmri.jmrix.PortAdapter p){
        this((jmri.jmrix.SerialPortAdapter)p);
    }
    public AbstractSerialConnectionConfig(jmri.jmrix.SerialPortAdapter p){
        adapter = p;
        addToActionList();
    }
    
    public jmri.jmrix.SerialPortAdapter getAdapter() { return adapter; }
    
    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass setInstance() will fill the adapter member.
     */
    public AbstractSerialConnectionConfig() {
        adapter = null;
        addToActionList();
    }

    protected boolean init = false;
    
    protected void checkInitDone() {
    	if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;

        baudBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.configureBaudRate((String)baudBox.getSelectedItem());
                p.addComboBoxLastSelection(adapter.getClass().getName()+".baud", (String) baudBox.getSelectedItem());
            }
        });

        if(adapter.getSystemConnectionMemo()!=null){
            systemPrefixField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
            });
            systemPrefixField.addFocusListener( new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
                public void focusGained(FocusEvent e){ }
            });
            connectionNameField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())){
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
            });
            connectionNameField.addFocusListener( new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())){
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
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
        
        for(String i:options.keySet()){
            final String item = i;
            if(options.get(i).getComponent() instanceof JComboBox){
                ((JComboBox)options.get(i).getComponent()).addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adapter.setOptionState(item, options.get(item).getItem());
                    }
                });
            }
        }

        init = true;
    }

    public void updateAdapter(){
        adapter.setPort(PortNameMapper.getPortFromName((String)portBox.getSelectedItem()));
        adapter.configureBaudRate((String)baudBox.getSelectedItem());
        for(String i:options.keySet()){
            adapter.setOptionState(i, options.get(i).getItem());
        }

        if(adapter.getSystemConnectionMemo()!=null && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
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
            return PortNameMapper.getPortFromName(t);
            //return t;
        }
        else if ((adapter!=null) && (adapter.getCurrentPortName()!=null)){
            return adapter.getCurrentPortName();
        }
        
        return JmrixConfigPane.NONE;
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
        if (portName == null || portName.equals(rb.getString("noneSelected")) || portName.equals(rb.getString("noPortsFound"))){
            for (int i=0; i<portBox.getItemCount(); i++) {
                outerloop:
                for(String friendlyName: getPortFriendlyNames()){
                    if(((String)portBox.getItemAt(i)).contains(friendlyName)){
                        portBox.setSelectedIndex(i);
                        adapter.setPort(PortNameMapper.getPortFromName((String)portBox.getItemAt(i)));
                        break outerloop;
                    }
                }
            }
        }

        portBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String port = PortNameMapper.getPortFromName((String)portBox.getSelectedItem());
                adapter.setPort(port);
            }
        });
    }
    
    String value;
   
    public void loadDetails(final JPanel details) {
        _details = details;
        setInstance();
        if(!init){
            //Build up list of options
            String[] optionsAvailable = adapter.getOptions();
            options = new Hashtable<String, Option>();
            for(String i:optionsAvailable){
                JComboBox opt = new JComboBox(adapter.getOptionChoices(i));
                opt.setSelectedItem(adapter.getOptionState(i));
                options.put(i, new Option(adapter.getOptionDisplayName(i), opt, adapter.isOptionAdvanced(i)));
            }
        }
        
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

        if (baudList.length>1) {
            baudBox.setToolTipText("Must match the baud rate setting of your hardware");
            baudBox.setEnabled(true);
        } else {
            baudBox.setToolTipText("The baud rate is fixed for this protocol");
            baudBox.setEnabled(false);
        }
        
        NUMOPTIONS = NUMOPTIONS+options.size();

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
        cL.anchor = GridBagConstraints.WEST;
        cL.insets = new Insets(2, 5, 0, 5);
        cR.insets = new Insets(2, 0, 0, 5);
        cR.anchor = GridBagConstraints.WEST;
        cR.gridx = 1;
        cL.gridx = 0;
        int i = 0;
        int stdrows = 0;
        boolean incAdvancedOptions=true;
        if(!isBaudAdvanced()) stdrows++;
        if(!isPortAdvanced()) stdrows++;
        for(String item:options.keySet()){
            if(!options.get(item).isAdvanced())
                stdrows++;
        }

        if(adapter.getSystemConnectionMemo()!=null) stdrows=stdrows+2;
        if (stdrows == NUMOPTIONS){
            incAdvancedOptions=false;
        }
        _details.setLayout(gbLayout);
        i = addStandardDetails(incAdvancedOptions, i);
        if (showAdvanced.isSelected()) {

            if(isPortAdvanced()){
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(portBoxLabel, cL);
                gbLayout.setConstraints(portBox, cR);
                
                //panel.add(row1Label);
                _details.add(portBoxLabel);
                _details.add(portBox);
                i++;
            }
            
            if(isBaudAdvanced()){
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(baudBoxLabel, cL);
                gbLayout.setConstraints(baudBox, cR);
                _details.add(baudBoxLabel);
                _details.add(baudBox);
                i++;
            }
            for(String item:options.keySet()){
                if(options.get(item).isAdvanced()){
                    cR.gridy = i;
                    cL.gridy = i;
                    gbLayout.setConstraints(options.get(item).getLabel(), cL);
                    gbLayout.setConstraints(options.get(item).getComponent(), cR);
                    _details.add(options.get(item).getLabel());
                    _details.add(options.get(item).getComponent());
                    i++;
                }
            }
        }
        cL.gridwidth=2;
        for(JComponent item: additionalItems){
            cL.gridy = i;
            gbLayout.setConstraints(item, cL);
            _details.add(item);
            i++;
        }
        cL.gridwidth=1;
        
        if (_details.getParent()!=null && _details.getParent() instanceof javax.swing.JViewport){
            javax.swing.JViewport vp = (javax.swing.JViewport)_details.getParent();
            vp.validate();
            vp.repaint();
        }
    }
    
    protected int addStandardDetails(boolean incAdvanced, int i){
        if(!isPortAdvanced()){
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(portBoxLabel, cL);
            gbLayout.setConstraints(portBox, cR);
            _details.add(portBoxLabel);
            _details.add(portBox);
            i++;
        }
        
        if(!isBaudAdvanced()){
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(baudBoxLabel, cL);
            gbLayout.setConstraints(baudBox, cR);
            _details.add(baudBoxLabel);
            _details.add(baudBox);
            i++;
        }
        
        return addStandardDetails(adapter, incAdvanced, i);
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
        else return name();
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
                String port = PortNameMapper.getPortFromName(displayName);
                if (port.equals(invalidPort)){
                    list.setSelectionForeground(Color.red);
                    setForeground(Color.red);
                }
            }

            setText(displayName);

            return this;
        }
    }
    
    protected synchronized static void updateSerialPortNames(String portName, JComboBox portCombo, Vector<String> portList){
        for(Entry<String, SerialPortFriendlyName> en : PortNameMapper.getPortNameMap().entrySet()){
            en.getValue().setValidPort(false);
        }
        for (int i=0; i<portList.size(); i++) {
            String commPort = portList.elementAt(i);
            SerialPortFriendlyName port = PortNameMapper.getPortNameMap().get(commPort);
            if(port==null){
                port = new SerialPortFriendlyName(commPort, null);
                PortNameMapper.getPortNameMap().put(commPort, port);
            }
            port.setValidPort(true);
            portCombo.addItem(port.getDisplayName());
            if(commPort.equals(portName)){
                portCombo.setSelectedIndex(i);
            }
        }

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
    
    /**
    * This provides a method to return potentially meaningfull names that are used 
    * in OS to help identify ports againsts Hardware.
    */
    protected String[] getPortFriendlyNames() {
        return new String[]{};
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

    final static protected Logger log = Logger.getLogger(AbstractSerialConnectionConfig.class.getName());
}
