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
    private ArrayList<Integer> listMapped;
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
        listMapped = new ArrayList<Integer>();
        for ( int k=0 ; (k < CbusFilter.CFMAXCATS + CbusFilter.CFMAX_NODES) ; k++){
            listMapped.add(-1);
        }
        
        getContentPane().setLayout(new GridLayout(1,0));
        setTitle(title());
        listFilters = new ArrayList<CbusFilterPanel>();
        _filter = new CbusFilter(this);
        
        // add items to GUI
       
        fPane = new JPanel();
        fPane.setLayout(new BoxLayout(fPane, BoxLayout.Y_AXIS));
        fPaneScroll = new JScrollPane();

        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFIN,Bundle.getMessage("Incoming"),false,0));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFOUT,Bundle.getMessage("Outgoing"),false,0));
        
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFEVENT,Bundle.getMessage("CbusEvents"),true,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFEVENTMIN,Bundle.getMessage("MinEvent"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFEVENTMAX,Bundle.getMessage("MaxEvent"),false,CbusFilter.CFEVENT)); 
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFON,Bundle.getMessage("CbusOnEvents"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFOF,Bundle.getMessage("CbusOffEvents"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFSHORT,Bundle.getMessage("ShortEvents"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFLONG,Bundle.getMessage("LongEvents"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFSTD,Bundle.getMessage("StandardEvents"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFREQUEST,Bundle.getMessage("RequestEvents"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFRESPONSE,Bundle.getMessage("ResponseEvents"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFED0,Bundle.getMessage("EVD0"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFED1,Bundle.getMessage("EVD1"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFED2,Bundle.getMessage("EVD2"),false,CbusFilter.CFEVENT));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFED3,Bundle.getMessage("EVD3"),false,CbusFilter.CFEVENT));
        
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNODE,Bundle.getMessage("CbusNodes"),true,CbusFilter.CFNODE));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNODEMIN,Bundle.getMessage("MinNode"),false,CbusFilter.CFNODE));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNODEMAX,Bundle.getMessage("MaxNode"),false,CbusFilter.CFNODE));
        
        for ( int j=0 ; ( j < CbusFilter.CFMAX_NODES ) ; j++ ){
            listFilters.add(new CbusFilterPanel(false,this,(CbusFilter.CFMAXCATS + j),Bundle.getMessage("CbusNodes"),false,CbusFilter.CFNODE));
        }
        
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFDATA,Bundle.getMessage("OPC_DA"),true,CbusFilter.CFDATA));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFACDAT,"ACDAT",false,CbusFilter.CFDATA)); // NOI18N
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFDDES,"DDES",false,CbusFilter.CFDATA)); // NOI18N
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFRQDAT,"RQDAT",false,CbusFilter.CFDATA)); // NOI18N
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFARDAT,"ARDAT",false,CbusFilter.CFDATA)); // NOI18N
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFDDRS,"DDRS",false,CbusFilter.CFDATA)); // NOI18N
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFRQDDS,"RQDDS",false,CbusFilter.CFDATA)); // NOI18N
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCABDAT,"Cabdata (experimental)",false,CbusFilter.CFDATA));
        
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCS,Bundle.getMessage("CommandStation"),true,CbusFilter.CFCS));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCSAQRL,Bundle.getMessage("LocoCommands"),false,CbusFilter.CFCS));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCSKA,Bundle.getMessage("KeepAlive"),false,CbusFilter.CFCS));       
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCSDSPD,Bundle.getMessage("SpeedDirection"),false,CbusFilter.CFCS));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCSFUNC,Bundle.getMessage("Functions"),false,CbusFilter.CFCS));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCSPROG,Bundle.getMessage("Programming"),false,CbusFilter.CFCS));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCSLC,Bundle.getMessage("LayoutCommands"),false,CbusFilter.CFCS));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCSC,Bundle.getMessage("CommandStationControl"),false,CbusFilter.CFCS));
        
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNDCONFIG,Bundle.getMessage("NodeConfiguration"),true,CbusFilter.CFNDCONFIG));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNDSETUP,Bundle.getMessage("GeneralNodeSetup"),false,CbusFilter.CFNDCONFIG));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNDVAR,Bundle.getMessage("NodeVariables"),false,CbusFilter.CFNDCONFIG));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNDEV,Bundle.getMessage("NodeEvents"),false,CbusFilter.CFNDCONFIG));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNDNUM,Bundle.getMessage("NodeNumbers"),false,CbusFilter.CFNDCONFIG));
        
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFMISC,Bundle.getMessage("Misc"),true,CbusFilter.CFMISC));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFNETWK,Bundle.getMessage("NetworkCommands"),false,CbusFilter.CFMISC));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFCLOCK,Bundle.getMessage("CBUS_FCLK"),false,CbusFilter.CFMISC));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFOTHER,Bundle.getMessage("Others"),false,CbusFilter.CFMISC));
        listFilters.add(new CbusFilterPanel(true,this,CbusFilter.CFUNKNOWN,Bundle.getMessage("Unknown"),false,CbusFilter.CFMISC));
        
        // Nodes
        // Node List
        
        for ( int i=0 ; (i < listFilters.size()) ; i++){
            fPane.add(listFilters.get(i));
            listMapped.set(listFilters.get(i).getIndex(),i);
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

    public void addNode(int nodenum, int position) {
        log.debug("New node {} notification to position {} ",nodenum,position);
        listFilters.get(listMapped.get(position)).setNode(nodenum,
            listFilters.get(listMapped.get(CbusFilter.CFNODE)).getButton(),
            listFilters.get(listMapped.get(CbusFilter.CFNODEMIN)).getVisible()
        );
    }

    protected void checkBoxChanged ( int id, Boolean newselected, int category, Boolean catHead ){
        _filter.setFilter( id, newselected);
        if (catHead) {
            for ( int i=0 ; (i < listFilters.size()) ; i++ ) {
                if ( ( listFilters.get(i).getCategory() == category ) &&
                    ( !listFilters.get(i).iscatHead() ) &&
                    ( listFilters.get(i).getAvailable() ) ) {
                    _filter.setFilter((id), newselected);
                    listFilters.get(i).setPass(newselected);
                }
            }
        }
        else if ( category > 0 ) {
            if ( category == CbusFilter.CFNODE ) {
                return;
            }
            int filterId=0;
            int listID=0;
            Boolean hasTrue = false;
            Boolean hasFalse = false;
            for ( int i=0 ; (i < listFilters.size()) ; i++){
                if ( ( listFilters.get(i).getCategory() == category ) && ( listFilters.get(i).getAvailable() ) ) {
                    if (listFilters.get(i).iscatHead()){
                        filterId = listFilters.get(i).getIndex();
                        listID = i;
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
                listFilters.get(listID).setMixed();
                _filter.setFilter( filterId, false);
            }
            
            if ( hasTrue && !hasFalse ) {
                listFilters.get(listID).setPass(true);
                _filter.setFilter( filterId, true);
            }
            if ( !hasTrue && hasFalse ) {
                listFilters.get(listID).setPass(false);
                _filter.setFilter( filterId, false);
            }
        }
    }

    protected void showFiltersChanged(int id, Boolean newselected, int category){
        // log.debug("showFiltersChanged id {} newselected {} category {} ",id,newselected, category);
        for ( int i=0 ; (i < listFilters.size()) ; i++){
            if ( ( listFilters.get(i).getCategory() == category ) && 
                ( !listFilters.get(i).iscatHead() ) && 
                ( listFilters.get(i).getAvailable() ) ) {
                listFilters.get(i).visibleFilter(newselected);
            }
        }
    }

    public void passIncrement(int id){
        ThreadingUtil.runOnGUIEventually( ()->{   
            listFilters.get(listMapped.get(id)).incrementPass();
        });
    }

    protected void minEvChanged(int value){
        _filter.setEvMin(value);
    }

    protected void maxEvChanged(int value){
        _filter.setEvMax(value);
    }
    
    protected void minNdChanged(int value){
        _filter.setNdMin(value);
    }

    protected void maxNdChanged(int value){
        _filter.setNdMax(value);
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
                listFilters.get(listMapped.get(result)).incrementFilter();
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
                listFilters.get(listMapped.get(result)).incrementFilter();
            });
            return true;
       }
       return false;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusFilterFrame.class);
}
