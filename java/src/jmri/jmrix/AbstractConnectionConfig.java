// AbstractSerialConnectionConfig.java

package jmri.jmrix;

import org.apache.log4j.Logger;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.util.Hashtable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import jmri.UserPreferencesManager;
import jmri.InstanceManager;

/**
 * Abstract base class for common implementation of the ConnectionConfig
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision$
 */
abstract public class AbstractConnectionConfig implements jmri.jmrix.ConnectionConfig {

    /**
     * Ctor for a functional object with no prexisting adapter.
     * Expect that the subclass setInstance() will fill the adapter member.
     */
    public AbstractConnectionConfig() {
    }
    
    protected final UserPreferencesManager pref = InstanceManager.getDefault(UserPreferencesManager.class);
    
    abstract void checkInitDone();
    
    abstract public void updateAdapter();
    
    protected int NUMOPTIONS = 2;
    
    protected JCheckBox showAdvanced = new JCheckBox("Additional Connection Settings");
    
    protected JLabel systemPrefixLabel = new JLabel("Connection Prefix");
    protected JLabel connectionNameLabel = new JLabel("Connection Name");
    protected JTextField systemPrefixField = new JTextField(10);
    protected JTextField connectionNameField = new JTextField(15);
    protected String systemPrefix;
    protected String connectionName;
    
    protected JPanel _details;
    
    protected Hashtable<String, Option> options = new Hashtable<String, Option>();
    
    protected static class Option {
        
        String optionDisplayName;
        JComponent optionSelection;
        Boolean advanced = true;
        JLabel label = null;
        
        Option(String name, JComponent optionSelection, Boolean advanced){
            this.optionDisplayName = name;
            this.optionSelection = optionSelection;
            this.advanced = advanced;
        }
        
        protected String getDisplayName(){
            return optionDisplayName;
        }
        
        public JLabel getLabel(){
            if(label == null)
                label = new JLabel(getDisplayName(), JLabel.LEFT);
            return label;
        }
        
        public JComponent getComponent(){
            return optionSelection;
        }
        
        protected Boolean isAdvanced(){
            return advanced;
        }
        
        protected void setAdvanced(Boolean boo){
            advanced = boo;
        }
        
        public String getItem(){
            if(optionSelection instanceof JComboBox){
                return (String)((JComboBox)optionSelection).getSelectedItem();
            } else if (optionSelection instanceof JTextField){
                return ((JTextField)optionSelection).getText();
            }
            return null;
        }
    }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();
    
    abstract public String getInfo();
    
    protected ArrayList<JComponent> additionalItems = new ArrayList<JComponent>(0);
    
    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
	abstract public void loadDetails(final JPanel details) ;
    
    GridBagLayout gbLayout = new GridBagLayout();
    GridBagConstraints cL = new GridBagConstraints();
    GridBagConstraints cR = new GridBagConstraints();
    
    abstract void showAdvancedItems();
    
    protected int addStandardDetails(PortAdapter adapter, boolean incAdvanced, int i){
        for(String item:options.keySet()){
            if(!options.get(item).isAdvanced()){
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(options.get(item).getLabel(), cL);
                gbLayout.setConstraints(options.get(item).getComponent(), cR);
                _details.add(options.get(item).getLabel());
                _details.add(options.get(item).getComponent());
                i++;
            }
        }
        
        if(adapter.getSystemConnectionMemo()!=null){
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(systemPrefixLabel, cL);
            gbLayout.setConstraints(systemPrefixField, cR);
            _details.add(systemPrefixLabel);
            _details.add(systemPrefixField);
            i++;
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(connectionNameLabel, cL);
            gbLayout.setConstraints(connectionNameField, cR);
            _details.add(connectionNameLabel);
            _details.add(connectionNameField);
            i++;
        }
        if (incAdvanced){
            cL.gridwidth=2;
            cL.gridy = i;
            cR.gridy = i;
            gbLayout.setConstraints(showAdvanced, cL);
            _details.add(showAdvanced);
            cL.gridwidth=1;
            i++;
        }
        return i;
    }
        
    abstract public String getManufacturer();
    abstract public void setManufacturer(String manufacturer);
    
    abstract public String getConnectionName();
    
    abstract public boolean getDisabled();
    abstract public void setDisabled(boolean disable);
    
    static protected Logger log = Logger.getLogger(AbstractConnectionConfig.class.getName());
    
}
