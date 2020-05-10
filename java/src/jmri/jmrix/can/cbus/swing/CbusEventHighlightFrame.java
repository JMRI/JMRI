package jmri.jmrix.can.cbus.swing;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.WindowConstants;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusEventHighlighter;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane;
import jmri.util.JmriJFrame;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

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
    private CbusConsolePane _console;
    private ConfigToolPane _evCap;

    /**
     * Create a new instance of CbusFilterFrame.
     * @param console main Console Window, can be null
     * @param evCap main Event Capture Window, can be null
     */
    public CbusEventHighlightFrame(CbusConsolePane console, ConfigToolPane evCap) {
        super();
        for (int i = 0; i < HIGHLIGHTERS; i++) {
            _highlight[i] = new CbusEventHighlighter();
            _highlightActive[i] = false;
        }
        _console = console;
        _evCap = evCap;
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    protected CbusEventHighlightFrame() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if ( _console != null) {
            return _console.getTitle() + " " + Bundle.getMessage("EventHighlightTitle");
        }
        else if ( _evCap != null) {
            return _evCap.getTitle() + " " + Bundle.getMessage("EventHighlightTitle");
        }
        return(Bundle.getMessage("EventHighlightTitle"));
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
        setTitle(getTitle());
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

    /**
     * Enable Highlighter.
     * @param index highlighter index.
     * @param nn node number.
     * @param nnEn node number enabled.
     * @param ev event number.
     * @param evEn event number enabled.
     * @param ty event type.
     * @param dr event direction.
     */
    public void enable(int index, int nn, boolean nnEn, int ev, boolean evEn, int ty, int dr) {
        
        _highlight[index].setNn(nn);
        _highlight[index].setNnEnable(nnEn);
        _highlight[index].setEv(ev);
        _highlight[index].setEvEnable(evEn);
        _highlight[index].setType(ty);
        _highlight[index].setDir(dr);
        _highlightActive[index] = true;
        if ( _console != null) { 
            updateConsole(index);
        }
    }
    
    private void updateConsole(int index) {
    
        // log.debug("Cbus Console highlight applied");
        StringBuilder sb = new StringBuilder(80);
        if ( _highlight[index].getNnEnable() ) {
            sb.append(Bundle.getMessage("CbusNode")).append(_highlight[index].getNn()).append(" ");
        }
        if (_highlight[index].getEvEnable()) {
            sb.append(Bundle.getMessage("CbusEvent")).append(_highlight[index].getEv()).append(" ");
        }
        
        appendType(sb,index);
        appendDirection(sb,index);
        
        sb.append("\n");
        _console.nextLine(sb.toString(), sb.toString(), index);
    }
    
    private void appendType( StringBuilder sb, int index ){
        switch (_highlight[index].getType()) {
            case CbusConstants.EVENT_ON:
                sb.append(Bundle.getMessage("CbusEventOn"));
                break;
            case CbusConstants.EVENT_OFF:
                sb.append(Bundle.getMessage("CbusEventOff"));
                break;
            default:
                sb.append(Bundle.getMessage("CbusEventOnOrOff"));
                break;
        }
    }
    
    private void appendDirection( StringBuilder sb, int index ){
        switch (_highlight[index].getDir()) {
            case CbusConstants.EVENT_DIR_IN:
                sb.append(Bundle.getMessage("InEventsTooltip"));
                break;
            case CbusConstants.EVENT_DIR_OUT:
                sb.append(Bundle.getMessage("OutEventsTooltip"));
                break;        
            default:
                sb.append(Bundle.getMessage("InOrOutEventsToolTip"));
                break;
        }
    }

    /**
     * Disable a Highlighter by Index Number.
     * @param index Highlighter Index number
     */
    public void disable(int index) {
        _highlightActive[index] = false;
        if ( _console != null) { 
            _console.nextLine( Bundle.getMessage("HighlightDisabled") + " \n", Bundle.getMessage("HighlightDisabled") + " \n",  index);
        }
    }

    /**
     * Get whether to Highlight a particular CAN Frame.
     * @param m CanMessage or CanReply
     * @return -1 to NOT Highlight, else Highlighter Number.
     */
    public int highlight(AbstractMessage m) {
        for (int i = 0; i < HIGHLIGHTERS; i++) {
            if (_highlightActive[i] && _highlight[i].highlight(m)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get Colour for a particular highlighter.
     * @param i Highlight index
     * @return Highlight Colour
     */
    public Color getColor(int i) {
        return highlightColors[i];
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventHighlightFrame.class);
}
