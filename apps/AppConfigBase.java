// AppConfigBase.java

package apps;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.JmrixConfigPane;
import jmri.util.swing.JmriPanel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * @version	$Revision: 1.17 $
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppConfigBase.class.getName());

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
    
        Map<String, List<Integer>> ports = new HashMap<String, List<Integer>>();
        int maxSize = JmrixConfigPane.getNumberOfInstances();
        for (int count = 0; count < maxSize; count++) {
            if (!getDisabled(count)) {
		        String port = getPort(count);
		        /*We need to test to make sure that the connection port is not set to (none)
		        If it is set to none, then it is likely a simulator.*/
		        if(!port.equals(JmrixConfigPane.NONE)){
		            if (!ports.containsKey(port)){
		            	List<Integer> arg1 = new ArrayList<Integer>();
		            	arg1.add(count);
						ports.put(port, arg1);
		            } else {
		            	ports.get(port).add(count);
		            }
		        }
            }
        }
        boolean ret = true;
        /* one or more dups or NONE, lets see if it is dups */
        for (Map.Entry<String, List<Integer>> e : ports.entrySet()) {
        	if (e.getValue().size() > 1) {
        		/* dup port found */
        		ret = false;
        		StringBuilder nameB = new StringBuilder();
        		for (int n = 0; n < e.getValue().size(); n++) {
        			nameB.append(getManufacturerName(e.getValue().get(n)));
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
    	for (int i=0; i<items.size(); i++){
    		if (getPort(i).equals(JmrixConfigPane.NONE_SELECTED) || getPort(i).equals(JmrixConfigPane.NO_PORTS_FOUND)) {
    	           if (JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("MessageSerialPortWarning"), new Object[]{getPort(i), getConnection(i)}), rb.getString("MessageSerialPortNotValid"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) != JOptionPane.YES_OPTION)
    			return false;
    		}
    	}
    	return true;
    }

    public void dispose() {
        items.clear();
    }
    
    protected void saveContents() {
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI items on the persistance list
        for (int i = 0; i < items.size(); i++) {
            InstanceManager.configureManagerInstance().registerPref(items.get(i));
        }
        //Need to register the userpreferencesmanager, otherwise all the settings get lost.
        InstanceManager.configureManagerInstance().registerPref(jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class));
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
        	final JDialog dialog = new JDialog();
        	dialog.setTitle(rb.getString("MessageShortQuitWarning"));
        	dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	JPanel container = new JPanel();
        	container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        	container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        	Icon icon = UIManager.getIcon("OptionPane.questionIcon");
        	JLabel question = new JLabel(rb.getString("MessageLongQuitWarning"));
        	question.setAlignmentX(Component.CENTER_ALIGNMENT);
        	question.setIcon(icon);
        	container.add(question);
        	final JCheckBox remember = new JCheckBox(rb.getString("MessageRemenberSetting"));
        	remember.setFont(remember.getFont().deriveFont(10.0F));
        	remember.setAlignmentX(Component.CENTER_ALIGNMENT);
        	JButton yesButton = new JButton(rb.getString("Yes"));
        	JButton noButton = new JButton(rb.getString("No"));
        	JPanel button = new JPanel();
        	button.setAlignmentX(Component.CENTER_ALIGNMENT);
        	button.add(yesButton);
        	button.add(noButton);
        	container.add(button);
        	noButton.addActionListener(new ActionListener() {

        		public void actionPerformed(ActionEvent e) {
        			if (remember.isSelected()) {
                        p.setMultipleChoiceOption(getClassName(),"quitAfterSave", 0x01);
        			}
        			dialog.dispose();
        		}
        	});
        	yesButton.addActionListener(new ActionListener() {

        		public void actionPerformed(ActionEvent e) {
        			if (remember.isSelected()) {
        				p.setMultipleChoiceOption(getClassName(),"quitAfterSave", 0x02);
        				saveContents();
        			}
        			dialog.dispose();
        			dispose();
        			Apps.handleQuit();
        		}
        	});
        	container.add(remember);
        	container.setAlignmentX(Component.CENTER_ALIGNMENT);
        	container.setAlignmentY(Component.CENTER_ALIGNMENT);
        	dialog.getContentPane().add(container);
        	dialog.pack();
        	dialog.setModal(true);
        	int w = dialog.getSize().width;
        	int h = dialog.getSize().height;
        	int x = (p.getScreen().width - w) / 2;
        	int y = (p.getScreen().height - h) / 2;
        	dialog.setLocation(x, y);
        	dialog.setVisible(true);
        } else if (p.getMultipleChoiceOption(getClassName(),"quitAfterSave") == 2) {
        	// end the program
        	dispose();
        	// do orderly shutdown.  Note this
        	// invokes Apps.handleQuit, even if this
        	// panel hasn't been created by an Apps subclass.
        	Apps.handleQuit();
        }
        // don't end the program, just close the window
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
