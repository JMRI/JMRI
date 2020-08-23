package jmri.jmrix.can.cbus.swing;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.EnumSet;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jmri.jmrix.can.cbus.CbusFilter;
import jmri.jmrix.can.cbus.CbusFilterType;
import jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Frame to control an instance of CBUS filter to filter events.
 * Currently used in CBUS Console + Event capture tool
 *
 * @author Steve Young Copyright (C) 2018, 2020
 */
public class CbusFilterFrame extends JmriJFrame {
    
    private final CbusConsolePane _console;
    private final ConfigToolPane _evCap;
    private final CbusFilter _filter;
    private final HashMap<Integer, CbusFilterPanel> hash_map;
    
    private final static Dimension minimumSize = new Dimension(400, 200);

    /**
     * Create a new instance of CbusFilterFrame.
     * @param console CbusConsolePane Instance to Filter
     * @param evCap Event Capture Tool Instance to Filter
     */
    public CbusFilterFrame(CbusConsolePane console, ConfigToolPane evCap) {
        super();
        _console = console;
        _evCap = evCap;
        _filter = new CbusFilter(this);
        hash_map = new HashMap<>(CbusFilter.getHMapSize(CbusFilter.CFMAXCATS + CbusFilter.CFMAX_NODES));
        super.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }
    
    /**
     * Pass text to a CbusConsole instance.
     * @param text to include in the Console Log.2
     */
    protected void updateListeners(String text){
        if ( _console != null) {
            _console.nextLine( text + " \n", text + " \n", -1);
        }
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
        
        getContentPane().setLayout(new GridLayout(1,0));
        setTitle(title());
       
        JPanel fPane = new JPanel();
        fPane.setLayout(new BoxLayout(fPane, BoxLayout.Y_AXIS));
        
        EnumSet.range(CbusFilterType.CFIN, CbusFilterType.CFNODEMAX).forEach((singleFilter) -> {
            addPaneToMap(fPane, new CbusFilterPanel(this,singleFilter));
        });
        
        for ( int j=0 ; ( j < CbusFilter.CFMAX_NODES ) ; j++ ){
            addPaneToMap(fPane, new CbusFilterPanel(this,(CbusFilter.CFMAXCATS + j)));
        }
        
        EnumSet.range(CbusFilterType.CFDATA, CbusFilterType.CFUNKNOWN).forEach((singleFilter) -> {
            addPaneToMap(fPane, new CbusFilterPanel(this,singleFilter));
        });
        
        JScrollPane fPaneScroll = new JScrollPane();
        fPaneScroll.setPreferredSize(minimumSize); 
        fPaneScroll.getViewport().add(fPane);
        
        fPaneScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        fPaneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        fPaneScroll.setVisible(true);
        getContentPane().add(fPaneScroll);
        // prevent button areas from expanding
        pack();
        updateListeners(Bundle.getMessage("FilterWindowActive"));
    }
    
    /**
     * Add Pane.
     * @param fPane main Pane to add to
     * @param panel CbusFilterPanel to add
     */
    public void addPaneToMap(JPanel fPane, CbusFilterPanel panel) {
        fPane.add(panel);
        hash_map.put(panel.getIndex(),panel);
    }

    /**
     * Add Node to empty node Panel.
     * 
     * @param nodenum Node Number
     * @param position Position in main Filter list
     */
    public void addNode(int nodenum, int position) {
        hash_map.get(position).setNode(nodenum,
            hash_map.get(CbusFilterType.CFNODE.ordinal()).getButton(),
            hash_map.get(CbusFilterType.CFNODEMIN.ordinal()).getVisible()
        );
    }

    /**
     * Change which categories are filtered.
     * 
     * @param id Filter ID, includes the Node filters.
     * @param newselected If the category is now visible
     * @param changedFilter ENUM value of Category
     */
    protected void checkBoxChanged ( int id, boolean newselected, @Nonnull CbusFilterType changedFilter){
        _filter.setFilter( id, newselected); // update main filter boolean
        if (changedFilter.alwaysDisplay()) {
            setCategoryChanged(id,newselected,changedFilter);
        }
        else {
            if (changedFilter.getCategory()==null) {
                return;
            }
            boolean hasTrue = hash_map.values().stream().filter((value) -> ( (
                value.getFilterType().getCategory() == changedFilter.getCategory() )
                && ( value.getAvailable() ) 
                &&  (value.getButton()))).count()>0;
            
            boolean hasFalse = hash_map.values().stream().filter((value) -> ( (
                value.getFilterType().getCategory() == changedFilter.getCategory() )
                && ( value.getAvailable() ) 
                &&  (!value.getButton()))).count()>0;

                setFilterMaybeMixed(changedFilter.getCategory(),hasTrue,hasFalse);

        }
    }
    
    /**
     * Set Filter Category Pass / Filter / Mixed status
     * 
     * @param changedFilter Filter Category
     * @param hasTrue has Passes in children
     * @param hasFalse has Filters in children
     */
    private void setFilterMaybeMixed( CbusFilterType changedFilter, boolean hasTrue, boolean hasFalse) {
        if ( hasTrue && hasFalse ) {
            hash_map.get(changedFilter.ordinal()).setMixed();
            _filter.setFilter( changedFilter.ordinal(), false);
        }
        else if ( hasTrue && !hasFalse ) {
            hash_map.get(changedFilter.ordinal()).setPass(true);
            _filter.setFilter( changedFilter.ordinal(), true);
        } 
        else if ( !hasTrue && hasFalse ) {
            hash_map.get(changedFilter.ordinal()).setPass(false);
            _filter.setFilter( changedFilter.ordinal(), false);
        }
    }
    
    /**
     * Category master button, change status of all children.
     * 
     * @param id Filter ID, includes the Node filters.
     * @param newselected If the category is now visible
     * @param changedFilter ENUM value of Category
     */
    private void setCategoryChanged( int id, boolean newselected, @Nonnull CbusFilterType changedFilter){
        hash_map.values().stream().filter((value) -> ( (
            value.getFilterType().getCategory() == changedFilter )
                && ( value.getAvailable() ) )).forEach((value) -> {
                _filter.setFilter((id), newselected); // set main boolean filter
                value.setPass(newselected);
        });
    }
    
    /**
     * Change which categories are displayed following button click.
     * 
     * @param id Filter ID, includes the Node filters.
     * @param newselected If the category is now visible
     * @param categoryType ENUM value of Category
     */
    protected void showFiltersChanged(int id, boolean newselected, @Nonnull CbusFilterType categoryType){
        hash_map.values().stream().filter((value) -> ( (
            value.getFilterType().getCategory() == categoryType )
                && ( value.getAvailable() ) )).forEach((value) -> {
                value.visibleFilter(newselected);
        });
    }

    /**
     * Increment a filter panel pass value
     * @param id Panel ID
     */
    public void passIncrement(int id){
        ThreadingUtil.runOnGUIEventually( ()->{   
            hash_map.get(id).incrementPass();
        });
    }
    
    /**
     * Set the event or node min and max values.
     * 
     * @param filter CFEVENTMIN, CFEVENTMAX, CFNODEMIN or CFNODEMAX
     * @param value min or max value
     */
    protected void setMinMax(@Nonnull CbusFilterType filter, int value){
        _filter.setMinMax(filter,value);
        switch (filter) {
            case CFEVENTMIN:
                updateListeners(Bundle.getMessage("MinEventSet",value));
                break;
            case CFEVENTMAX:
                updateListeners(Bundle.getMessage("MaxEventSet",value));
                break;
            case CFNODEMIN:
                updateListeners(Bundle.getMessage("MinNodeSet",value));
                break;
            case CFNODEMAX:
                updateListeners(Bundle.getMessage("MaxNodeSet",value));
                break;
            default:
                break;
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
       int result = _filter.filter(m);
       if ( result > -1 ) {
            ThreadingUtil.runOnGUIEventually( ()->{
                hash_map.get(result).incrementFilter();
            });
            return true;
       }
       return false;
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusFilterFrame.class);
}
