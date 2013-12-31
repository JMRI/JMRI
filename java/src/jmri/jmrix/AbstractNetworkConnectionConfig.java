// AbstractNetworkConnectionConfig.java

package jmri.jmrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import java.util.Hashtable;

/**
 * Abstract base class for common implementation of the ConnectionConfig
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */
abstract public class AbstractNetworkConnectionConfig extends AbstractConnectionConfig implements jmri.jmrix.ConnectionConfig {

    protected JCheckBox showAutoConfig = new JCheckBox("Automatic Configuration");

    /**
     * Ctor for an object being created during load process
     */

    public AbstractNetworkConnectionConfig(jmri.jmrix.NetworkPortAdapter p){
        adapter = p;
    }

    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass setInstance() will fill the adapter member.
     */
    public AbstractNetworkConnectionConfig() {
    }

    protected boolean init = false;
    
    protected void checkInitDone() {
    	if (log.isDebugEnabled()) log.debug("init called for "+name());
        if (init) return;
        hostNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                adapter.setHostName(hostNameField.getText());
                p.addComboBoxLastSelection(adapter.getClass().getName()+".hostname", hostNameField.getText());
            }
        });
        hostNameField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
               adapter.setHostName(hostNameField.getText());
               p.addComboBoxLastSelection(adapter.getClass().getName()+".hostname", hostNameField.getText());
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        portField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    adapter.setPort(Integer.parseInt(portField.getText()));
                } catch (java.lang.NumberFormatException ex) {
                    log.warn("Could not parse port attribute");
                }
            }
        });

        portField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
               try{
                    adapter.setPort(Integer.parseInt(portField.getText()));
                } catch (java.lang.NumberFormatException ex) {
                    log.warn("Could not parse port attribute");
                }
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
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
        }        init = true;
    }

    public void updateAdapter(){
        if(adapter.getMdnsConfigure()) {
           // get the host name and port number
           // via mdns
           adapter.autoConfigure();
        } else {
           adapter.setHostName(hostNameField.getText());
           adapter.setPort(Integer.parseInt(portField.getText()));
        }
        for(String i:options.keySet()){
            adapter.setOptionState(i, options.get(i).getItem());
        }
        if(adapter.getSystemConnectionMemo()!=null && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())){
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    protected JTextField hostNameField = new JTextField(15);
    protected JLabel hostNameFieldLabel;
    protected JTextField portField = new JTextField(10);
    protected JLabel portFieldLabel;
    protected jmri.jmrix.NetworkPortAdapter adapter = null;

    public jmri.jmrix.NetworkPortAdapter getAdapter() { return adapter; }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    public String getInfo() {
        return adapter.getCurrentPortName();
    }
    
    public void loadDetails(final JPanel details) {
    	_details = details;
        setInstance();
        if(!init){
            //Build up list of options
            //Hashtable<String, AbstractPortController.Option> adapterOptions = ((AbstractPortController)adapter).getOptionList();
            String[] optionsAvailable = adapter.getOptions();
            options = new Hashtable<String, Option>();
            for(String i:optionsAvailable){
                JComboBox opt = new JComboBox(adapter.getOptionChoices(i));
                opt.setSelectedItem(adapter.getOptionState(i));
                options.put(i, new Option(adapter.getOptionDisplayName(i), opt, adapter.isOptionAdvanced(i)));
            }
        }
 

        if(hostNameField.getActionListeners().length >0)
        	hostNameField.removeActionListener(hostNameField.getActionListeners()[0]);

        if(adapter.getSystemConnectionMemo()!=null){
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
            NUMOPTIONS=NUMOPTIONS+2;
        }
        NUMOPTIONS = NUMOPTIONS+options.size();

        portField.setToolTipText("Port address setting of the TCP Connection");
        portField.setEnabled(true);
        
        hostNameField.setText(adapter.getHostName());
        hostNameFieldLabel = new JLabel("IP Address: ");
        if(adapter.getHostName()==null || adapter.getHostName().equals("") ){
            hostNameField.setText(p.getComboBoxLastSelection(adapter.getClass().getName()+".hostname"));
            adapter.setHostName(hostNameField.getText());
        }
        portField.setText(""+adapter.getPort());
        
        portFieldLabel = new JLabel("TCP/UDP Port:");

        showAutoConfig.setFont(showAdvanced.getFont().deriveFont(9f));
        showAutoConfig.setForeground(Color.blue);
        showAutoConfig.addItemListener(
            new ItemListener() {
                public void itemStateChanged(ItemEvent e){
                    setAutoNetworkConfig();
                }
            });
        showAutoConfig.setSelected(adapter.getMdnsConfigure());
        setAutoNetworkConfig();

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
        if(!isPortAdvanced()) stdrows++;
        if(!isHostNameAdvanced()) stdrows++;
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
            if(isHostNameAdvanced()){
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(hostNameFieldLabel, cL);
                gbLayout.setConstraints(hostNameField, cR);
                _details.add(hostNameFieldLabel);
                _details.add(hostNameField);
                i++;
            }
            
            if(isPortAdvanced()){
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(portFieldLabel, cL);
                gbLayout.setConstraints(portField, cR);
                _details.add(portFieldLabel);
                _details.add(portField);
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

        if(isAutoConfigPossible()) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(showAutoConfig, cR);
            _details.add(showAutoConfig);
            _details.add(showAutoConfig);
            i++;
        }

        if(!isHostNameAdvanced()){
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(hostNameFieldLabel, cL);
            gbLayout.setConstraints(hostNameField, cR);
            _details.add(hostNameFieldLabel);
            _details.add(hostNameField);
            i++;
        }
        
        if(!isPortAdvanced()){
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(portFieldLabel, cL);
            gbLayout.setConstraints(portField, cR);
            _details.add(portFieldLabel);
            _details.add(portField);
            i++;
        }
        return addStandardDetails(adapter, incAdvanced, i);
    }
    
    public boolean isHostNameAdvanced() { return false; }
    public boolean isPortAdvanced() { return true; }

    public boolean isAutoConfigPossible() { return false; }

    public void setAutoNetworkConfig(){
       if(showAutoConfig.isSelected()) {
          hostNameField.setEnabled(false);
          hostNameFieldLabel.setEnabled(false);
          portField.setEnabled(false);
          portFieldLabel.setEnabled(false);
          adapter.setMdnsConfigure(true);
       } else { 
          hostNameField.setEnabled(true);
          hostNameFieldLabel.setEnabled(true);
          portField.setEnabled(true);
          portFieldLabel.setEnabled(true);
          adapter.setMdnsConfigure(false);
       }
    }
    
    public String getManufacturer() { return adapter.getManufacturer(); }
    public void setManufacturer(String manufacturer) { adapter.setManufacturer(manufacturer); }

    public boolean getDisabled() {
        if (adapter==null) return true;
        return adapter.getDisabled();
    }
    public void setDisabled(boolean disabled) {
        if(adapter!=null)
            adapter.setDisabled(disabled);
    }
    
    public String getConnectionName() { 
        if(adapter.getSystemConnectionMemo()!=null)
            return adapter.getSystemConnectionMemo().getUserName();
        else return name();
    }
    
    public void dispose() { 
        if (adapter!=null){
            adapter.dispose();
            adapter=null;
        }
    }

    static Logger log = LoggerFactory.getLogger(AbstractNetworkConnectionConfig.class.getName());

}

