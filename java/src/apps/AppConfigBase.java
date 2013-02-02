// AppConfigBase.java

package apps;

import org.apache.log4j.Logger;
import jmri.Application;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.JmrixConfigPane;
import jmri.util.swing.JmriPanel;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

/**
 * Basic configuration infrastructure, to be 
 * used by specific GUI implementations
 *
 * @author	Bob Jacobsen   Copyright (C) 2003, 2008, 2010
 * @author      Matthew Harris copyright (c) 2009
 * @author	Ken Cameron Copyright (C) 2011
 * @version	$Revision$
 */
public class AppConfigBase extends JmriPanel {

    protected static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

    /**
     * Remember items to persist
     */
    protected List<Component> items = new ArrayList<Component>();

    /**
     * Construct a configuration panel for inclusion in a preferences
     * or configuration dialog with default number of connections.
     */
    public AppConfigBase() {
    }
    
    public static String getManufacturerName(int index) {
        return JmrixConfigPane.instance(index).getCurrentManufacturerName();
    }
    
    public static String getConnection(int index) {
        return JmrixConfigPane.instance(index).getCurrentProtocolName();
    }
    
    public static String getPort(int index) {
        return JmrixConfigPane.instance(index).getCurrentProtocolInfo();
    }

    public static String getConnectionName(int index) {
        return JmrixConfigPane.instance(index).getConnectionName();
    }

    public static boolean getDisabled(int index) {
        return JmrixConfigPane.instance(index).getDisabled();
    }
    
    // initialize logging
    static Logger log = Logger.getLogger(AppConfigBase.class.getName());

    /**
     * Detect duplicate connection types
     * It depends on all connections have the first word be the same
     * if they share the same type. So LocoNet ... is a fine example.
     * <p>
     * This also was broken when the names for systems were updated before
     * JMRI 2.9.4, so it should be revisited.
     * 
     * @return true if OK, false if duplicates present.
     */
    private boolean checkDups() {
        Map<String, List<JmrixConfigPane>> ports = new HashMap<String, List<JmrixConfigPane>>();
        ArrayList<JmrixConfigPane> configPaneList = JmrixConfigPane.getListOfConfigPanes();
    	for (int i=0; i<configPaneList.size(); i++){
            JmrixConfigPane configPane = configPaneList.get(i);
            if (!configPane.getDisabled()) {
		        String port = configPane.getCurrentProtocolInfo();
		        /*We need to test to make sure that the connection port is not set to (none)
		        If it is set to none, then it is likely a simulator.*/
		        if(!port.equals(JmrixConfigPane.NONE)){
		            if (!ports.containsKey(port)){
		            	List<JmrixConfigPane> arg1 = new ArrayList<JmrixConfigPane>();
		            	arg1.add(configPane);
						ports.put(port, arg1);
		            } else {
		            	ports.get(port).add(configPane);
		            }
		        }
            }
        }
        boolean ret = true;
        /* one or more dups or NONE, lets see if it is dups */
        for (Map.Entry<String, List<JmrixConfigPane>> e : ports.entrySet()) {
        	if (e.getValue().size() > 1) {
        		/* dup port found */
        		ret = false;
        		StringBuilder nameB = new StringBuilder();
        		for (int n = 0; n < e.getValue().size(); n++) {
                    nameB.append(e.getValue().get(n).getCurrentManufacturerName());
        			nameB.append("|");
        		}
        		String instanceNames = new String(nameB);
        		instanceNames = instanceNames.substring(0, instanceNames.lastIndexOf("|"));
        		instanceNames = instanceNames.replaceAll("[|]", ", ");
        		log.error("Duplicate ports found on: " + instanceNames + " for port: " + e.getKey());
        	}
        }
        return ret;
    }

    /**
     * Checks to see if user selected a valid serial port
     * @return true if okay
     */
    private boolean checkPortNames() {
        ArrayList<JmrixConfigPane> configPane = JmrixConfigPane.getListOfConfigPanes();
    	for (int i=0; i<configPane.size(); i++){
            String port = configPane.get(i).getCurrentProtocolInfo();
    		if (port.equals(JmrixConfigPane.NONE_SELECTED) || port.equals(JmrixConfigPane.NO_PORTS_FOUND)) {
    	           if (JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("MessageSerialPortWarning"), new Object[]{port, configPane.get(i).getCurrentProtocolName()}), rb.getString("MessageSerialPortNotValid"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) != JOptionPane.YES_OPTION)
    			return false;
    		}
    	}
    	return true;
    }

    public void dispose() {
        items.clear();
    }

    public void saveContents() {
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI items on the persistance list
        for (int i = 0; i < items.size(); i++) {
            InstanceManager.configureManagerInstance().registerPref(items.get(i));
        }
        InstanceManager.configureManagerInstance().storePrefs();
    }

    /**
     * Handle the Save button:  Backup the file, write a new one, prompt for
     * what to do next.  To do that, the last step is to present a dialog
     * box prompting the user to end the program.
     */
     
    public void savePressed() {
        // true if port name OK
        if (!checkPortNames())           
                return;
        // true if there arn't any duplicates
        if (!checkDups())
        	if (!(JOptionPane.showConfirmDialog(null, rb.getString("MessageLongDupsWarning"), rb.getString("MessageShortDupsWarning"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION))
        		return;
        saveContents();
        final UserPreferencesManager p;
        p = InstanceManager.getDefault(UserPreferencesManager.class);
        p.resetChangeMade();
        if (p.getMultipleChoiceOption(getClassName(),"quitAfterSave") == 0) {
            JPanel message = new JPanel();
            JLabel question = new JLabel(MessageFormat.format(rb.getString("MessageLongQuitWarning"), Application.getApplicationName()));
            final JCheckBox remember = new JCheckBox(rb.getString("MessageRememberSetting"));
            remember.setFont(remember.getFont().deriveFont(10.0F));
            message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
            message.add(question);
            message.add(remember);
            Object[] options = {rb.getString("RestartNow"), rb.getString("RestartLater")};
            int retVal = JOptionPane.showOptionDialog(this,
                    message,
                    MessageFormat.format(rb.getString("MessageShortQuitWarning"), Application.getApplicationName()),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    null);
            switch (retVal) {
                case JOptionPane.YES_OPTION:
                    if (remember.isSelected()) {
                        p.setMultipleChoiceOption(getClassName(), "quitAfterSave", 0x02);
                        saveContents();
                    }
                    dispose();
                    Apps.handleRestart();
                    break;
                case JOptionPane.NO_OPTION:
                    if (remember.isSelected()) {
                        p.setMultipleChoiceOption(getClassName(), "quitAfterSave", 0x01);
                    }
                    break;
                default:
                    break;
            }
        } else if (p.getMultipleChoiceOption(getClassName(),"quitAfterSave") == 2) {
        	// restart the program
        	dispose();
        	// do orderly shutdown.  Note this
        	// invokes Apps.handleRestart, even if this
        	// panel hasn't been created by an Apps subclass.
        	Apps.handleRestart();
        }
        // don't restart the program, just close the window
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }

    public String getClassDescription() { return rb.getString("Application"); }

    public void setMessagePreferencesDetails(){
        HashMap<Integer,String> options = new HashMap<Integer,String>(3);
        options.put(0x00, rb.getString("QuitAsk"));
        options.put(0x01, rb.getString("QuitNever"));
        options.put(0x02, rb.getString("QuitAlways"));
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).messageItemDetails(getClassName(), "quitAfterSave", rb.getString("quitAfterSave"), options, 0x00);
    }

    public String getClassName() { return AppConfigBase.class.getName(); }
}
