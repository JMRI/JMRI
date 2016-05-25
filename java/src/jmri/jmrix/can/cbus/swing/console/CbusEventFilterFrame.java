/*
 * CbusEventFilterFrame.java
 *
 */
package jmri.jmrix.can.cbus.swing.console;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.WindowConstants;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusEventFilter;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to control an instance of CBUS filter to filter events
 *
 * @author	Andrew Crosland Copyright (C) 2008
 * @version	$Revision: 17977 $
 */
public class CbusEventFilterFrame extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -618391050120364272L;
    protected static final int FILTERS = 4;
    static final Color[] filterColors = {Color.RED, Color.GREEN, Color.CYAN, Color.YELLOW};
    protected CbusEventFilterPanel[] filterPanes = new CbusEventFilterPanel[FILTERS];

    // member to hold reference to my filters
    protected CbusEventFilter[] _filter = new CbusEventFilter[FILTERS];
    protected boolean[] _filterActive = new boolean[FILTERS];
    private CbusConsolePane _console = null;

    /**
     * Creates a new instance of CbusFilterFrame
     */
    public CbusEventFilterFrame(CbusConsolePane console) {
        super();
        log.debug("CbusEventFilterFrame(CbusEventFilter) ctor called");
        for (int i = 0; i < FILTERS; i++) {
            _filter[i] = new CbusEventFilter();
            _filterActive[i] = false;
        }
        _console = console;
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        log.debug("CbusEventFilterFrame(CbusEventFilter) ctor done");
    }

    protected CbusEventFilterFrame() {
        super();
    }

    protected String title() {
        return "CBUS EventFilter";
    }

    protected void init() {
    }

    public void dispose() {
        super.dispose();
//        _console.filterFrameClosed();
    }

    public void initComponents() throws Exception {
        setTitle(title());
        // Panels will be added downwards
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add items to GUI
        for (int i = 0; i < FILTERS; i++) {
            // Pane to hold a filter
            filterPanes[i] = new CbusEventFilterPanel(this, i);
            filterPanes[i].setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Filter " + (i + 1)));
            filterPanes[i].initComponents(i);
            getContentPane().add(filterPanes[i]);
        }

        // add help menu to window
        addHelpMenu();

        // prevent button areas from expanding
        pack();
    }

    public void enable(int index, int nn, boolean nnEn, int ev, boolean evEn, int ty) {
        _console.filterOn(index, nn, nnEn, ev, evEn, ty);
        _filter[index].setNn(nn);
        _filter[index].setNnEnable(nnEn);
        _filter[index].setEv(ev);
        _filter[index].setEvEnable(evEn);
        _filter[index].setType(ty);
        _filterActive[index] = true;
    }

    public void disable(int index) {
        _filterActive[index] = false;
        _console.filterOff(index);
    }

    public int filter(CanMessage m) {
        int i;
        for (i = 0; i < FILTERS; i++) {
            if (_filterActive[i] && _filter[i].filter(m)) {
                return i;
            }
        }
        return -1;
    }

    public int filter(CanReply r) {
        int i;
        for (i = 0; i < FILTERS; i++) {
            if (_filterActive[i] && _filter[i].filter(r)) {
                return i;
            }
        }
        return -1;
    }

    public Color getColor(int i) {
        return filterColors[i];
    }

    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page that covers general features.
     * Specific implementations can override this to show their own help page if
     * desired.
     */
    protected void addHelpMenu() {

        // *** TO DO
//    	addHelpMenu("package.jmri.jmrix.can.cbus.CbusEventFilterFrame", true);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventFilterFrame.class.getName());
}
