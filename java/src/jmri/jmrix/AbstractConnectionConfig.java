// AbstractSerialConnectionConfig.java

package jmri.jmrix;

import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

    protected JComboBox opt1Box = new JComboBox();
    protected JLabel opt1BoxLabel = new JLabel();
    protected String[] opt1List;
    public boolean isOptList1Advanced() { return true; }
    
    protected JComboBox opt2Box = new JComboBox();
    protected JLabel opt2BoxLabel = new JLabel();
    protected String[] opt2List;
    public boolean isOptList2Advanced() { return true; }

    protected JComboBox opt3Box = new JComboBox();
    protected JLabel opt3BoxLabel = new JLabel();
    protected String[] opt3List;
    public boolean isOptList3Advanced() { return true; }

    protected JComboBox opt4Box = new JComboBox();
    protected JLabel opt4BoxLabel = new JLabel();
    protected String[] opt4List;
    public boolean isOptList4Advanced() { return true; }

    protected JCheckBox showAdvanced = new JCheckBox("Additional Connection Settings");

    protected JLabel systemPrefixLabel = new JLabel("Connection Prefix");
    protected JLabel connectionNameLabel = new JLabel("Connection Name");
    protected JTextField systemPrefixField = new JTextField();
    protected JTextField connectionNameField = new JTextField();
    protected String systemPrefix;
    protected String connectionName;
    
    protected JPanel _details;
    //protected jmri.jmrix.PortAdapter adapter = null;
    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    abstract public String getInfo();

    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
	abstract public void loadDetails(final JPanel details) ;
    
    abstract void showAdvancedItems();
    
    abstract void addStandardDetails(boolean incAdvanced);
        
    abstract public String getManufacturer();
    abstract public void setManufacturer(String manufacturer);
    
    abstract public String getConnectionName();

    abstract public boolean getDisabled();
    abstract public void setDisabled(boolean disable);

     static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractConnectionConfig.class.getName());

}

