package jmri.jmrix.can.cbus.swing;

import javax.annotation.Nonnull;
import javax.swing.WindowConstants;

import jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.util.JmriJFrame;

/**
 * Frame to control an instance of CBUS filter to filter events.
 * Currently used in CBUS Console + Event capture tool
 *
 * @author Steve Young Copyright (C) 2018, 2020
 */
public class CbusFilterFrame extends JmriJFrame {
    
    private final CbusConsolePane _console;
    private final ConfigToolPane _evCap;

    final CbusFilterTreePane ftp;

    /**
     * Create a new instance of CbusFilterFrame.
     * @param console CbusConsolePane Instance to Filter
     * @param evCap Event Capture Tool Instance to Filter
     */
    public CbusFilterFrame(CbusConsolePane console, ConfigToolPane evCap) {
        super();
        _console = console;
        _evCap = evCap;

        var memo = console != null ? console.getMemo() : null;
        if ( memo == null ) {
            memo = evCap != null ? evCap.getMemo() : null;
        }
        ftp = new CbusFilterTreePane( memo);
        super.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    /**
     * Get Filter Title.
     * @return Title incorporating CbusConsole or Event Capture Instance.
     */
    @Nonnull
    protected String title() {
        if (_console != null) {
            return _console.getTitle() + " " + Bundle.getMessage("EventFilterTitleX", "");
        } else if (_evCap != null) {
            return _evCap.getTitle() + " " + Bundle.getMessage("EventFilterTitleX", "");
        }
        return Bundle.getMessage("EventFilterTitleX", "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        setTitle(title());
        this.getContentPane().add(ftp);
        pack();
        if ( _console != null) {
            _console.nextLine( Bundle.getMessage("FilterWindowActive") + " \n",
                Bundle.getMessage("FilterWindowActive") + " \n", -1);
        }
    }

    /**
     * Filter a CanReply or CanMessage.
     * 
     * @param m CanMessage or CanReply
     * @return true when to apply filter, false to not filter.
     *
     */
    public boolean filter(@Nonnull jmri.jmrix.AbstractMessage m) {
       return ftp.filter(m);
    }

    @Override
    public void dispose() {
        super.dispose();
        ftp.dispose();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusFilterFrame.class);

}
