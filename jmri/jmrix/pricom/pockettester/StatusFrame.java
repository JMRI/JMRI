// pricom.pockettester.StatusFrame.java

package jmri.jmrix.pricom.pockettester;

import javax.swing.*;

/**
 * Simple to display DCC status from Pocket Tester
 * <P>
 * For more info on the product, see http://www.pricom.com
 *
 * @author			Bob Jacobsen   Copyright (C) 2005
 * @version			$Revision: 1.1 $
 */
public class StatusFrame extends javax.swing.JFrame implements DataListener {

    static java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");

    public StatusFrame() { super(); }
    
    /**
     * Add GUI elements
     */
    public void initComponents() {
        JTabbedPane tabPane = new JTabbedPane();
        getContentPane().add(tabPane);
        
        // sequence through, adding panes
        // first, do timing manually
        {
            JPanel p = new JPanel();
            p.add(new JLabel("place-saver"));
            
            tabPane.addTab(rb.getString("CSMainPaneName"), p);
        }
        
        // next, loop over the auto definitions from the properties file.
        // get pane count
        int numAutoPane = Integer.parseInt(rb.getString("CSNumAutoPanes"));
        for (int i = 0; i<numAutoPane; i++) {
            // create and install tabbed pane
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            tabPane.addTab(rb.getString("CS"+i+"Title"), p);
            
            // install variables
            int numVars = Integer.parseInt(rb.getString("CS"+i+"NumVars"));
            for (int j = 0; j<numVars; j++) {
                JPanel line = new JPanel();
                
                line.add(new JLabel(rb.getString("CS"+i+"Var"+j+"Name")));
                line.add(new JLabel("00"));
                p.add(line);
            }
        }
        
        
        
        // and get ready to display
        pack();
    }
    
    protected String title() {
        String title = filter;
        if (filter == null) title = "";
        return java.text.MessageFormat.format(rb.getString("TitleStatus"),
                                                              new String[] {title});
    }

    public void dispose() {
        // and clean up parent
        super.dispose();
    }

    public void asciiFormattedMessage(String m) { 
        //if ( (filter==null) || m.startsWith(filter) )
        //    nextLine(m, "");
    }
    String filter = null;
    
    /**
     * Start filtering input to include only lines that
     * start with a specific string.
     * A null input passes all.
     */
    public void setFilter(String s) {
        filter = s;
        setTitle(title());
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StatusFrame.class.getName());


}
