package jmri.jmrix.can.cbus.swing;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.WindowConstants;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusEventHighlighter;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane;
import jmri.util.JmriJFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to control an instance of CBUS highlighter to highlight events.
 *
 * @author Andrew Crosland Copyright (C) 2008
 */
public class CbusEventHighlightFrame extends JmriJFrame {

    protected static final int HIGHLIGHTERS = 4;
    public static final Color[] highlightColors = {
        new Color(110, 235, 131), // green ish as will have black text on top
        new Color(68, 235, 255), // cyan ish
        new Color(228, 255, 26), // yellow ish
        new Color(255, 132, 84) // orange ish
        };
    protected CbusEventHighlightPanel[] highlightPanes = new CbusEventHighlightPanel[HIGHLIGHTERS];

    // member to hold reference to my HIGHLIGHTERS
    protected CbusEventHighlighter[] _highlight = new CbusEventHighlighter[HIGHLIGHTERS];
    protected boolean[] _highlightActive = new boolean[HIGHLIGHTERS];
    private CbusConsolePane _console = null;
    private ConfigToolPane _evCap = null;

    /**
     * Create a new instance of CbusFilterFrame.
     */
    public CbusEventHighlightFrame(CbusConsolePane console, ConfigToolPane evCap) {
        super();
        log.debug("CbusEventhighlightFrame(CbusEventFilter) ctor called");
        for (int i = 0; i < HIGHLIGHTERS; i++) {
            _highlight[i] = new CbusEventHighlighter();
            _highlightActive[i] = false;
        }
        _console = console;
        _evCap = evCap;
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        log.debug("CbusEventFilterFrame(CbusEventFilter) ctor done");
    }

    protected CbusEventHighlightFrame() {
        super();
    }

    protected String title() {
        if ( _console != null) {
            return _console.getTitle() + " " + Bundle.getMessage("EventHighlightTitle");
        } else 
        if ( _evCap != null) {
            return _evCap.getTitle() + " " + Bundle.getMessage("EventHighlightTitle");
        }
        return(Bundle.getMessage("EventHighlightTitle"));
    }

    protected void init() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        setTitle(title());
        // Panels will be added downwards
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add items to GUI
        for (int i = 0; i < HIGHLIGHTERS; i++) {
            // Pane to hold a highlighter
            highlightPanes[i] = new CbusEventHighlightPanel(this, i);
            highlightPanes[i].setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(highlightColors[i],4), Bundle.getMessage("EventHighlightTitleX", (i + 1))));
            highlightPanes[i].initComponents(i);
            getContentPane().add(highlightPanes[i]);
        }
        // prevent button areas from expanding
        pack();
    }

    public void enable(int index, int nn, boolean nnEn, int ev, boolean evEn, int ty, int dr) {
        if ( _console != null) { 
            _console.highlightOn(index, nn, nnEn, ev, evEn, ty, dr);
        }
        _highlight[index].setNn(nn);
        _highlight[index].setNnEnable(nnEn);
        _highlight[index].setEv(ev);
        _highlight[index].setEvEnable(evEn);
        _highlight[index].setType(ty);
        _highlight[index].setDir(dr);
        _highlightActive[index] = true;
    }

    public void disable(int index) {
        _highlightActive[index] = false;
        if ( _console != null) { 
            _console.highlightOff(index);
        }
    }

    public int highlight(CanMessage m) {
        int i;
        for (i = 0; i < HIGHLIGHTERS; i++) {
            if (_highlightActive[i] && _highlight[i].highlight(m)) {
                return i;
            }
        }
        return -1;
    }

    public int highlight(CanReply r) {
        int i;
        for (i = 0; i < HIGHLIGHTERS; i++) {
            if (_highlightActive[i] && _highlight[i].highlight(r)) {
                return i;
            }
        }
        return -1;
    }

    public Color getColor(int i) {
        return highlightColors[i];
    }

    private final static Logger log = LoggerFactory.getLogger(CbusEventHighlightFrame.class);
}
