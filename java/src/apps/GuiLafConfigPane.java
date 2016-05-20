// GuiLafConfigPane.java

package apps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Locale;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * Provide GUI to configure Swing GUI LAF defaults
 * <P>
 * Provides GUI configuration for SWING LAF by
 * displaying radio buttons for each LAF implementation available.
 * This information is then persisted separately
 * (e.g. by {@link jmri.configurexml.GuiLafConfigPaneXml})
 * <P>
 * Locale default language and country is also considered a
 * GUI (and perhaps LAF) configuration item.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2010
 * @version	$Revision$
 * @since 2.9.5  (Previously in jmri package)
 */
public class GuiLafConfigPane extends JPanel {
	
	 private static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

    java.util.Hashtable<String, String> installedLAFs;
    ButtonGroup LAFGroup;
    String selectedLAF;

    public GuiLafConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p;
        doLAF(p = new JPanel());
        add(p);
        doFontSize(p = new JPanel());
        add(p);
        doClickSelection(p = new JPanel());
        add(p);
    }

    void doClickSelection(JPanel panel) {
        panel.setLayout(new FlowLayout());
        mouseEvent = new JCheckBox("Use non-standard release event for mouse click?");
        mouseEvent.setSelected(jmri.util.swing.SwingSettings.getNonStandardMouseEvent());
        panel.add(mouseEvent);
    }

    public JCheckBox mouseEvent;

    void doLAF(JPanel panel) {
        // find L&F definitions
        panel.setLayout(new FlowLayout());
        UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
        installedLAFs = new java.util.Hashtable<String, String>(plafs.length);
        for (int i = 0; i < plafs.length; i++){
            installedLAFs.put(plafs[i].getName(), plafs[i].getClassName());
        }
        // make the radio buttons
        LAFGroup = new ButtonGroup();
        Enumeration<String> LAFNames = installedLAFs.keys();
        while (LAFNames.hasMoreElements()) {
            String name = LAFNames.nextElement();
            JRadioButton jmi = new JRadioButton(name);
            panel.add(jmi);
            LAFGroup.add(jmi);
            jmi.setActionCommand(name);
            jmi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        selectedLAF = e.getActionCommand();
                    }
                });
            if (installedLAFs.get(name).equals(UIManager.getLookAndFeel().getClass().getName())) {
                jmi.setSelected(true);
                selectedLAF = name;
            }
        }       
    }

    /**
     * Create and return a JPanel for configuring default local.
     * <P>
     * Most of the action is handled in a separate thread, which
     * replaces the contents of a JComboBox when the list of
     * Locales is available.
     * @return the panel
     */
    public JPanel doLocale() {
        JPanel panel = new JPanel();
        // add JComboBoxen for language and country
        panel.setLayout(new FlowLayout());
        localeBox = new JComboBox(new String[]{
                        Locale.getDefault().getDisplayName(),
                        "(Please Wait)"});
        panel.add(localeBox);

        // create object to find locales in new Thread
        Runnable r  = new Runnable() {
            public void run() {
                Locale[] locales = java.util.Locale.getAvailableLocales();
                localeNames = new String[locales.length];
                locale = new HashMap<String, Locale>();
                for (int i = 0; i<locales.length; i++) {
                    locale.put(locales[i].getDisplayName(), locales[i]);
                    localeNames[i] = locales[i].getDisplayName();
                }
                java.util.Arrays.sort(localeNames);
                Runnable update = new Runnable() {
                    public void run() {
                        localeBox.setModel(new javax.swing.DefaultComboBoxModel(localeNames));
                        //localeBox.setModel(new javax.swing.DefaultComboBoxModel(locale.keySet().toArray()));
                        localeBox.setSelectedItem(Locale.getDefault().getDisplayName());
                    }
                };
                javax.swing.SwingUtilities.invokeLater(update);
            }
        };
        new Thread(r).start();
        return panel;
    }

    JComboBox localeBox;
    HashMap<String, Locale> locale;
    String[] localeNames;

    public void setLocale(String loc){
        localeBox.setSelectedItem(loc);
    }
    
    /**
     * Get the currently configured Locale
     * or Locale.getDefault if no configuration has been done.
     */
    public Locale getLocale() {
        if (localeBox==null || locale==null) return Locale.getDefault();
        String desired = (String)localeBox.getSelectedItem();
        return locale.get(desired);
    }
    
    static int fontSize = 0;
    
    public static void setFontSize(int size) {
        fontSize = size<9?9:size>18?18:size;
        //fontSizeComboBox.setSelectedItem(fontSize);
    }
    
    public static int getFontSize(){
    	return fontSize;
    }
    
    private int getDefaultFontSize(){
    	if (getFontSize() == 0){
    		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
    		while (keys.hasMoreElements()) {
    			Object key = keys.nextElement();
    			Object value = UIManager.get (key);

    			if (value instanceof javax.swing.plaf.FontUIResource && key.toString().equals("List.font")){
    				Font f = UIManager.getFont(key);
    				log.debug("Key:"+key.toString()+" Font: "+f.getName()+" size: "+f.getSize());
    				return f.getSize();
    			}
    		}
    		return 11;	// couldn't find the default return a reasonable font size
    	}
		return getFontSize();
    }
    
    private static final Integer fontSizes[] = {
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        16,
        17,
        18 };
    
    static JComboBox fontSizeComboBox = new JComboBox(fontSizes);
    static java.awt.event.ActionListener listener;
    
    public void doFontSize(JPanel panel){
    	
    	JLabel fontSizeLabel = new JLabel(rb.getString("ConsoleFontSize"));
    	fontSizeComboBox.removeActionListener(listener);
        fontSizeComboBox.setSelectedItem(getDefaultFontSize());
        JLabel fontSizeUoM = new JLabel(rb.getString("ConsoleFontSizeUoM"));
       	
        panel.add(fontSizeLabel);
    	panel.add(fontSizeComboBox);
       	panel.add(fontSizeUoM);
       	
       	fontSizeComboBox.addActionListener(listener = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setFontSize((Integer)fontSizeComboBox.getSelectedItem());
			}
		});
    }

    public String getClassName() {
        return LAFGroup.getSelection().getActionCommand();

    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(GuiLafConfigPane.class.getName());
}

