// pricom.pockettester.MonitorFrame.java
package jmri.jmrix.pricom.pockettester;


/**
 * Simple GUI for access to PRICOM Pocket Monitor.
 * <P>
 * For more info on the product, see http://www.pricom.com
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @version	$Revision$
 */
public class MonitorFrame extends jmri.jmrix.AbstractMonFrame implements DataListener {

    /**
     *
     */
    private static final long serialVersionUID = 7060223518481154741L;
    static java.util.ResourceBundle rb
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");

    public MonitorFrame() {
        super();
    }

    public void init() {
    }

    protected String title() {
        String title = filter;
        if (filter == null) {
            title = "";
        }
        return java.text.MessageFormat.format(rb.getString("TitleMonitor"),
                (Object[]) new String[]{title});
    }

    public void dispose() {
        // and clean up parent
        super.dispose();
    }

    public void asciiFormattedMessage(String m) {
        if ((filter == null) || m.startsWith(filter)) {
            nextLine(m, "");
        }
    }
    String filter = null;

    /**
     * Start filtering input to include only lines that start with a specific
     * string. A null input passes all.
     */
    public void setFilter(String s) {
        filter = s;
        setTitle(title());
    }

}
