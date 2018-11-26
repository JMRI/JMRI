package jmri.jmrix.pricom.pockettester;


/**
 * Simple GUI for access to PRICOM Pocket Monitor.
 * <p>
 * For more info on the product, see http://www.pricom.com
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 */
public class MonitorFrame extends jmri.jmrix.AbstractMonFrame implements DataListener {

    public MonitorFrame() {
        super();
    }

    @Override
    public void init() {
    }

    @Override
    protected String title() {
        String title = filter;
        if (filter == null) {
            title = "";
        }
        return java.text.MessageFormat.format(Bundle.getMessage("TitleMonitor"),
                new Object[]{title});
    }

    @Override
    public void dispose() {
        // and clean up parent
        super.dispose();
    }

    @Override
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
