package jmri.jmrix.pricom.pockettester;

import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple to display DCC status from Pocket Tester.
 * <p>
 * For more info on the product, see http://www.pricom.com
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 */
public class StatusFrame extends jmri.util.JmriJFrame implements DataListener {

    javax.swing.Timer timer = new javax.swing.Timer(500, new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            sendRequest();
        }
    });

    JTabbedPane tabPane = new JTabbedPane();
    Hashtable<String, JLabel> displayHash = new Hashtable<String, JLabel>();
    Hashtable<String, String> formatHash = new Hashtable<String, String>();
    DataSource source = null;

    public StatusFrame() {
        super(Bundle.getMessage("TitleStatus"));
    }

    /**
     * Add GUI elements
     */
    @Override
    public void initComponents() {
        getContentPane().add(tabPane);

        // loop over the auto definitions from the properties file, adding panes
        // get pane count
        int numAutoPane = Integer.parseInt(Bundle.getMessage("CSNumAutoPanes"));

        for (int i = 0; i < numAutoPane; i++) {
            // create and install tabbed pane
            JPanel p = new JPanel();
            p.setLayout(new java.awt.GridLayout(0, 2));  // 0 rows is a dummy value
            tabPane.addTab(Bundle.getMessage("CS" + i + "Title"), p);

            // install variables
            int numVars = Integer.parseInt(Bundle.getMessage("CS" + i + "NumVars"));
            for (int j = 0; j < numVars; j++) {
                p.add(new JLabel(Bundle.getMessage("CS" + i + "Var" + j + "Name")));
                JLabel val = new JLabel("-----");
                // record the label and format for later
                displayHash.put(Bundle.getMessage("CS" + i + "Var" + j + "ID"), val);
                formatHash.put(Bundle.getMessage("CS" + i + "Var" + j + "ID"), Bundle.getMessage("CS" + i + "Var" + j + "Format"));
                p.add(val);
            }
        }

        // add a listener to hear about selections
        tabPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                sendRequest();  // to get a fast update
            }
        }
        );

        // get ready to display
        pack();

    }

    protected String title() {
        return Bundle.getMessage("TitleStatus");
    }

    // note that the message coming from the unit has
    // an invisible null character after the "=" in version 1.5.1
    // but not in later versions
    @Override
    public void asciiFormattedMessage(String input) {
        String m = input + " ";  // extra space to make stripping easier
        // check if interesting
        if (!m.substring(0, 1).equals(" ")) {
            return;
        }
        if (!m.substring(3, 4).equals("=")) {
            return;
        }
        int addOne = 0;
        if (m.substring(4, 5).equals("\000")) {
            addOne = 1;
        }
        // basically OK. Break into tokens, store if match, quit when done.
        while (m.length() >= 14 + addOne) {
            String id = m.substring(1, 3);
            String value = m.substring(4 + addOne, 14 + addOne);
            if (log.isDebugEnabled()) {
                log.debug("set var " + id + ":" + value);
            }
            JLabel label = displayHash.get(id);
            String format = formatHash.get(id);
            if (label != null) {
                label.setText(convertValue(value, format));
            }
            m = m.substring(14 + addOne);
        }
    }

    protected String convertValue(String val, String format) {
        if (format.equals("address")) {
            // long or short address format
            int address = Integer.parseInt(val);
            if (address >= 0x8000) {
                return "" + (address - 0x8000) + " (long)";
            } else {
                return "" + address + " (short)";
            }

        } else if (format.equals("msec")) {
            // value is in 10's of nsec, add decimal point for msec
            return val.substring(0, 5) + "." + val.substring(5) + " msec";

        } else if (format.equals("none")) {
            // no formatting, used for counts, etc
            return val; // don't change the content

        } else // didn't find matching format name, warn
        {
            return val + " (bad format)";
        }
    }

    /**
     * Time to send the next request
     */
    private void sendRequest() {
        int i = tabPane.getSelectedIndex();
        String prompt = Bundle.getMessage("CS" + i + "PromptChar");
        log.debug("send {} for pane {}", prompt, i);

        if (source == null) {
            log.error("DataSource should not be null in sendRequest");
            timer.stop();
            return;
        }

        source.sendBytes(prompt.getBytes());

    }

    public void setSource(DataSource s) {
        source = s;
        // start the timer for updates
        timer.setRepeats(true);     // in case we run by
        timer.start();
    }

    @Override
    public void dispose() {
        // stop timer
        timer.stop();
        // and clean up parent
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(StatusFrame.class);

}
