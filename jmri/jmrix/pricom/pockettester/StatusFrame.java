// pricom.pockettester.StatusFrame.java

package jmri.jmrix.pricom.pockettester;

import javax.swing.*;
import com.sun.java.util.collections.Hashtable;

/**
 * Simple to display DCC status from Pocket Tester
 * <P>
 * For more info on the product, see http://www.pricom.com
 *
 * @author			Bob Jacobsen   Copyright (C) 2005
 * @version			$Revision: 1.5 $
 */
public class StatusFrame extends javax.swing.JFrame implements DataListener {

    static java.util.ResourceBundle rb 
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");

    javax.swing.Timer timer = new javax.swing.Timer(500, new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            sendRequest();
        }
    });

    JTabbedPane tabPane = new JTabbedPane();
    Hashtable displayHash = new Hashtable();
    Hashtable formatHash = new Hashtable();
    DataSource source = null;
    
    public StatusFrame() { super(rb.getString("TitleStatus")); }
    
    /**
     * Add GUI elements
     */
    public void initComponents() {
        getContentPane().add(tabPane);
        
        // loop over the auto definitions from the properties file, adding panes
        // get pane count
        int numAutoPane = Integer.parseInt(rb.getString("CSNumAutoPanes"));
        
        for (int i = 0; i<numAutoPane; i++) {
            // create and install tabbed pane
            JPanel p = new JPanel();
            p.setLayout(new java.awt.GridLayout(0, 2));  // 0 rows is a dummy value
            tabPane.addTab(rb.getString("CS"+i+"Title"), p);
            
            // install variables
            int numVars = Integer.parseInt(rb.getString("CS"+i+"NumVars"));
            for (int j = 0; j<numVars; j++) {
                p.add(new JLabel(rb.getString("CS"+i+"Var"+j+"Name")));
                JLabel val = new JLabel("-----");
                // record the label and format for later
                displayHash.put(rb.getString("CS"+i+"Var"+j+"ID"),val);
                formatHash.put(rb.getString("CS"+i+"Var"+j+"ID"),rb.getString("CS"+i+"Var"+j+"Format"));
                p.add(val);
            }
        }
        
        // add a listener to hear about selections
        tabPane.addChangeListener(new javax.swing.event.ChangeListener(){
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    sendRequest();  // to get a fast update
                }
            }
        );
        
        // want to dispose this window when closed
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // get ready to display
        pack();

        // start the timer for updates
        timer.setRepeats(true);     // in case we run by
        timer.start();

    }
    
    protected String title() {
        return rb.getString("TitleStatus");
    }

    // note that the message coming from the unit has
    // an invisible character after the "="
    public void asciiFormattedMessage(String input) { 
        String m = input+" ";  // extra space to make stripping easier
        // check if interesting
        if (!m.substring(0,1).equals(" ")) return;
        if (!m.substring(3,4).equals("=")) return;
        // basically OK. Break into tokens, store if match, quit when done.
        while (m.length() >= 15) {
            String id = m.substring(1, 3);
            String value = m.substring(5, 15);
            System.out.println("set var "+id+":"+value);
            if (log.isDebugEnabled()) log.debug("set var "+id+":"+value);
            JLabel label = (JLabel)displayHash.get(id);
            String format = (String)formatHash.get(id);
            if (label != null) label.setText(convertValue(value, format));
            m = m.substring(15);
        }
    }
    
    protected String convertValue(String val, String format) {
        if (format.equals("address")) {
            // long or short address format
            int address = Integer.valueOf(val).intValue();
            if (address >= 0x8000)
                return ""+(address-0x8000)+" (long)";
            else 
                return ""+address+" (short)";
                
        } else if (format.equals("msec")) {
            // value is in 10's of nsec, add decimal point for msec
            return val.substring(0,5)+"."+val.substring(5)+" msec";
            
        } else if (format.equals("none")) {
            // no formatting, used for counts, etc
            return val; // don't change the content
            
        } else
        // didn't find matching format name, warn
        return val+" (bad format)";
    }
    
    /**
     * Time to send the next request
     */
    private void sendRequest() {
        int i = tabPane.getSelectedIndex();
        String prompt = rb.getString("CS"+i+"PromptChar");
        if (log.isDebugEnabled()) log.debug("send "+prompt+" for pane "+i);
        
        if (source == null) {
            log.error("DataSource should not be null in sendRequest");
            timer.stop();
            return;
        }
        
        source.sendBytes(prompt.getBytes());
        
    }
        
    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        // dispose();
        dispose();
    }

    public void setSource(DataSource s) {
        source = s;
    }
    
    public void dispose() {
        // stop timer
        timer.stop();
        // and clean up parent
        super.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StatusFrame.class.getName());

}
