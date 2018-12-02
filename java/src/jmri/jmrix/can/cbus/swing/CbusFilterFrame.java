package jmri.jmrix.can.cbus.swing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusFilter;
import jmri.jmrix.can.cbus.swing.CbusFilterPanel;
import jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to control an instance of CBUS filter to filter events.
 * Currently used in CBUS Console + Event capture tool
 *
 * @author Steve Young Copyright (C) 2018
 */
public class CbusFilterFrame extends JmriJFrame {
    
    private CbusConsolePane _console;
    private ConfigToolPane _evCap;
    private ArrayList<CbusFilterPanel> listFilters;
    private JPanel fPane;
    private JScrollPane fPaneScroll;
    private CbusFilter _filter;

    /**
     * Create a new instance of CbusFilterFrame.
     */
    public CbusFilterFrame(CbusConsolePane console, ConfigToolPane evCap) {
        super();
        _console = console;
        _evCap = evCap;
        log.debug("CbusFilterFrame ctor called");
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    protected CbusFilterFrame() {
        super();
        _console = null;
        _evCap = null;
    }
    
    protected void updateListeners(String text){
        if ( _console != null) {
            _console.filterChanged(text);
        }
    }

    protected String title() {
        if ( _console != null) {
            return _console.getTitle() + " " + Bundle.getMessage("EventFilterTitleX","");
        } else 
        if ( _evCap != null) {
            return _evCap.getTitle() + " " + Bundle.getMessage("EventFilterTitleX","");
        }
        return Bundle.getMessage("EventFilterTitleX","");
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
        getContentPane().setLayout(new GridLayout(1,0));
        setTitle(title());
        listFilters = new ArrayList<CbusFilterPanel>();
        _filter = new CbusFilter(this);
        
        // add items to GUI
       
        fPane = new JPanel();
        fPane.setLayout(new BoxLayout(fPane, BoxLayout.Y_AXIS));
        fPaneScroll = new JScrollPane();
        
        CbusFilterPanel filterpanel;

        listFilters.add(new CbusFilterPanel(this,_filter.CFIN,Bundle.getMessage("Incoming"),false,0));
        listFilters.add(new CbusFilterPanel(this,_filter.CFOUT,Bundle.getMessage("Outgoing"),false,0));
        
        listFilters.add(new CbusFilterPanel(this,_filter.CFEVENT,Bundle.getMessage("CbusEvents"),true,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFON,Bundle.getMessage("CbusOnEvents"),false,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFOF,Bundle.getMessage("CbusOffEvents"),false,1));          
        listFilters.add(new CbusFilterPanel(this,_filter.CFSHORT,Bundle.getMessage("ShortEvents"),false,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFLONG,Bundle.getMessage("LongEvents"),false,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFSTD,Bundle.getMessage("StandardEvents"),false,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFREQUEST,Bundle.getMessage("RequestEvents"),false,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFRESPONSE,Bundle.getMessage("ResponseEvents"),false,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFED0,Bundle.getMessage("EVD0"),false,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFED1,Bundle.getMessage("EVD1"),false,1));
        listFilters.add(new CbusFilterPanel(this,_filter.CFED2,Bundle.getMessage("EVD2"),false,1));        
        listFilters.add(new CbusFilterPanel(this,_filter.CFED3,Bundle.getMessage("EVD3"),false,1));        
        
        listFilters.add(new CbusFilterPanel(this,_filter.CFDATA,Bundle.getMessage("OPC_DA"),true,2));
        listFilters.add(new CbusFilterPanel(this,_filter.CFACDAT,"ACDAT",false,2)); // NOI18N
        listFilters.add(new CbusFilterPanel(this,_filter.CFDDES,"DDES",false,2)); // NOI18N
        listFilters.add(new CbusFilterPanel(this,_filter.CFRQDAT,"RQDAT",false,2)); // NOI18N
        listFilters.add(new CbusFilterPanel(this,_filter.CFARDAT,"ARDAT",false,2)); // NOI18N
        listFilters.add(new CbusFilterPanel(this,_filter.CFDDRS,"DDRS",false,2)); // NOI18N
        listFilters.add(new CbusFilterPanel(this,_filter.CFRQDDS,"RQDDS",false,2)); // NOI18N
        listFilters.add(new CbusFilterPanel(this,_filter.CFCABDAT,"Cabdata (experimental)",false,2));
        
        listFilters.add(new CbusFilterPanel(this,_filter.CFCS,Bundle.getMessage("CommandStation"),true,3));
        listFilters.add(new CbusFilterPanel(this,_filter.CFCSAQRL,Bundle.getMessage("LocoCommands"),false,3));
        listFilters.add(new CbusFilterPanel(this,_filter.CFCSKA,Bundle.getMessage("KeepAlive"),false,3));       
        listFilters.add(new CbusFilterPanel(this,_filter.CFCSDSPD,Bundle.getMessage("SpeedDirection"),false,3));
        listFilters.add(new CbusFilterPanel(this,_filter.CFCSFUNC,Bundle.getMessage("Functions"),false,3));
        listFilters.add(new CbusFilterPanel(this,_filter.CFCSPROG,Bundle.getMessage("Programming"),false,3));
        listFilters.add(new CbusFilterPanel(this,_filter.CFCSLC,Bundle.getMessage("LayoutCommands"),false,3));        
        listFilters.add(new CbusFilterPanel(this,_filter.CFCSC,Bundle.getMessage("CommandStationControl"),false,3));
        
        listFilters.add(new CbusFilterPanel(this,_filter.CFNDCONFIG,Bundle.getMessage("NodeConfiguration"),true,4));
        listFilters.add(new CbusFilterPanel(this,_filter.CFNDSETUP,Bundle.getMessage("GeneralNodeSetup"),false,4));
        listFilters.add(new CbusFilterPanel(this,_filter.CFNDVAR,Bundle.getMessage("NodeVariables"),false,4));
        listFilters.add(new CbusFilterPanel(this,_filter.CFNDEV,Bundle.getMessage("NodeEvents"),false,4));
        listFilters.add(new CbusFilterPanel(this,_filter.CFNDNUM,Bundle.getMessage("NodeNumbers"),false,4));
        
        listFilters.add(new CbusFilterPanel(this,_filter.CFMISC,Bundle.getMessage("Misc"),true,5));
        listFilters.add(new CbusFilterPanel(this,_filter.CFNETWK,Bundle.getMessage("NetworkCommands"),false,5));
        listFilters.add(new CbusFilterPanel(this,_filter.CFCLOCK,Bundle.getMessage("CBUS_FCLK"),false,5));
        listFilters.add(new CbusFilterPanel(this,_filter.CFOTHER,Bundle.getMessage("Others"),false,5));
        listFilters.add(new CbusFilterPanel(this,_filter.CFUNKNOWN,Bundle.getMessage("Unknown"),false,5));
        
        // Nodes
        // Node List
        
        for ( int i=0 ; (i < listFilters.size()) ; i++){
            fPane.add(listFilters.get(i));
            listFilters.get(i).setToolTip(_filter.getTtip(listFilters.get(i).getIndex()));
        }
        
        Dimension minimumSize = new Dimension(150, 80);

        fPaneScroll.setMinimumSize(minimumSize); 
        fPaneScroll.getViewport().add(fPane);
        
        fPaneScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        fPaneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        fPaneScroll.setVisible(true);
        getContentPane().add(fPaneScroll);
        // prevent button areas from expanding
        pack();
        log.debug("window created");
        updateListeners(Bundle.getMessage("FilterWindowActive"));
    }

    protected void checkBoxChanged(int id, Boolean newselected, int category, Boolean catHead){
        _filter.setFilter( id, newselected);
        if (catHead) {
            for ( int i=0 ; (i < listFilters.size()) ; i++){
                if ( ( listFilters.get(i).getCategory() == category) && ( !listFilters.get(i).iscatHead() ) ) {
                    _filter.setFilter( i, newselected);
                    listFilters.get(i).setPass(newselected);
                }
            }
        }
        else if ( category > 0 ) {
            int filterId=0;
            Boolean hasTrue = false;
            Boolean hasFalse = false;
            for ( int i=0 ; (i < listFilters.size()) ; i++){
                if ( listFilters.get(i).getCategory() == category) {
                    if (listFilters.get(i).iscatHead()){
                        filterId = i;
                    } else {
                        if (listFilters.get(i).getButton()){
                            hasTrue = true;
                        } else {
                            hasFalse = true;
                        }
                    }
                }
            }
            if ( hasTrue && hasFalse ) {
                listFilters.get(filterId).setMixed();
                _filter.setFilter( filterId, false);
            }
            
            if ( hasTrue && !hasFalse ) {
                listFilters.get(filterId).setPass(true);
                _filter.setFilter( filterId, true);
            }
            if ( !hasTrue && hasFalse ) {
                listFilters.get(filterId).setPass(false);
                _filter.setFilter( filterId, false);
            }
        }
    }

    protected void showFiltersChanged(int id, Boolean newselected, int category){
        // log.debug("showFiltersChanged id {} newselected {} category {} ",id,newselected, category);
        for ( int i=0 ; (i < listFilters.size()) ; i++){
            if ( listFilters.get(i).getCategory() == category ) {
                if ( !listFilters.get(i).iscatHead()) {
                    listFilters.get(i).visibleFilter(newselected);
                }
            }
        }
    }

    public void passIncrement(int id){
        ThreadingUtil.runOnGUIEventually( ()->{   
            listFilters.get(id).incrementPass();
        });
    }

    /*
     * return true when to apply filter
     * return false to not filter and allow message
     *
     */
    public Boolean filter(CanMessage m) {
       int result = _filter.filter(m);
       if ( result > -1 ) {
            ThreadingUtil.runOnGUIEventually( ()->{   
                listFilters.get(result).incrementFilter();
            });
           return true;
       }
       return false;
    }
    
    /*
     * return true when to apply filter
     * return false to not filter and allow message
     *
     */
    public Boolean filter(CanReply r) {
       int result = _filter.filter(r);
       if ( result > -1 ) {
            ThreadingUtil.runOnGUIEventually( ()->{   
                listFilters.get(result).incrementFilter();
            });
            return true;
       }
       return false;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusFilterFrame.class);
}
