// CarRoads.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;


/**
 * Represents the road names that cars can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class CarRoads implements java.beans.PropertyChangeListener {
	
	private static final String ROADS = "AA%%ACL%%ADCX%%ADMX%%AESX%%ALTON%%ATM%%ATR%%ATSF"
		+ "%%ATW%%B&O%%BAR%%BCK%%BM%%BN%%BR%%BR&S%%BREX%%BWCX"
		+ "%%C&EI%%C&IM%%C&O%%CACX%%CB&Q%%CCBX%%CDLX%%CG%%CG&W%%CM%%CMSF%%CMWX%%CN%%CNJ%%CNW%%CONX%%COPR%%CP%%CRR%%CTSE%%CTT%%CTTX%%CV"
		+ "%%D&H%%D&M%%D&RGW%%EJ&E%%ERIE%%FCX%%FDD&S%%FEC%%FW&D"
		+ "%%G&F%%GAT%%GATX%%GCR%%GM&O%%GN%%GPEX%%GRC%%GRCX%%GTW"
		+ "%%IC%%IGN%%IN%%KCS%%KOT%%KOTX%%LN%%LNE%%LS&I%%LV"
		+ "%%MEC%%MILW%%MKT%%MNS%%MP%%MPA%%MRL%%MSL"
		+ "%%N&W%%NC&SL%%NH%%NKP%%NP%%NYC%%OTT%%OTTX"
		+ "%%PC%%PFE%%PM%%PRR%%PS&N%%RDG%%RE%%REX%%RF&P%%RI%%RP%%RPX%%RTC%%RTCX%%RUT"
		+ "%%SAL%%SDW%%SDWX%%SHP%%SHPX%%SLSF%%SOO%%SOU%%SP%%SSW%%SUNX"
		+ "%%T&P%%TC%%TP&W%%UOCX%%UP%%UTL%%UTLX" 
		+ "%%VGN%%VN%%VTR%%W&LE%%W&LG%%WA%%WAB%%WDL%%WDLX%%WFEX%%WM%%WP%%WRNX";
	public static final String CARROADS = "CarRoads";
	private static final String LENGTH = "Length";
	

    public CarRoads() {
    }
    
	/** record the single instance **/
	private static CarRoads _instance = null;

	public static synchronized CarRoads instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarRoads creating instance");
			// create and load
			_instance = new CarRoads();
		}
		if (log.isDebugEnabled()) log.debug("CarRoads returns instance "+_instance);
		return _instance;
	}

    public void dispose() {
    	list.clear();
    }
    
    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    }

    List list = new ArrayList();
    
    public String[] getNames(){
     	if (list.size() == 0){
     		String[] roads = ROADS.split("%%");
     		for (int i=0; i<roads.length; i++)
     			list.add(roads[i]);
    	}
     	String[] roads = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		roads[i] = (String)list.get(i);
   		return roads;
    }
    
    public void setNames(String[] roads){
    	if (roads.length == 0) return;
    	jmri.util.StringUtil.sort(roads);
 		for (int i=0; i<roads.length; i++)
 			list.add(roads[i]);
    }
    
    public void addName(String road){
    	// insert at start of list, sort later
    	if (list.contains(road))
    		return;
    	list.add(0,road);
    	firePropertyChange (CARROADS, null, LENGTH);
    }
    
    public void deleteName(String road){
    	list.remove(road);
    	firePropertyChange (CARROADS, null, LENGTH);
     }
    
    public boolean containsName(String road){
    	return list.contains(road);
     }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
		String[] roads = getNames();
		for (int i = 0; i < roads.length; i++)
			box.addItem(roads[i]);
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		String[] roads = getNames();
		for (int i = 0; i < roads.length; i++)
			box.addItem(roads[i]);
    }
        
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CarRoads.class.getName());
}
