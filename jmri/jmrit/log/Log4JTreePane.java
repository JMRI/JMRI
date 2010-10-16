// Log4JTreePane.java

package jmri.jmrit.log;

import java.util.*;

import javax.swing.*;

import org.apache.log4j.Logger;

/**
 * Show the current Log4J Logger tree; not dynamic.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.3 $
 */

public class Log4JTreePane extends jmri.util.swing.JmriPanel {

    /**
     * Provide a help target string which an enclosing
     * frame can provide as a help reference.
     */
    //public String getHelpTarget() { return "Acknowledgements.shtml"; }

    /**
     * Provide a recommended title for an enclosing frame.
     */
    public String getTitle() { 
        return ResourceBundle.getBundle("jmri.jmrit.JmritDebugBundle").getString("MenuItemLogTreeAction"); 
    }
    
    /**
     * Provide menu items
     */
    //public List<JMenu> getMenus() { return null; }
    
    public Log4JTreePane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }
    
    /**
     * 2nd stage of initialization, invoked after
     * the constructor is complete.
     */
    @SuppressWarnings("unchecked")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
	public void initComponents() throws Exception {
        org.apache.log4j.spi.LoggerRepository repo 
            = Logger.getRootLogger().getLoggerRepository();
            
        List<String> list = new ArrayList<String>();
        for (java.util.Enumeration<Logger> e = repo.getCurrentLoggers() ; e.hasMoreElements() ;) {
            list.add((e.nextElement()).getName());
        }
        java.util.Collections.sort(list);
        String result = "";
        for (String s : list) {
            result = result+s+"\n";
        }
        
        JTextArea text = new JTextArea();
        text.setText(result);
        JScrollPane scroll = new JScrollPane(text);
        add(scroll);
    }
    
    /**
     * 3rd stage of initialization, invoked after
     * Swing components exist.
     */
    public void initContext(Object context) throws Exception {}
    
    public void dispose() {}
}