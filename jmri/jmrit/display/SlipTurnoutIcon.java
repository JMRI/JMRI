package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import jmri.util.NamedBeanHandle;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An icon to display a status of a Slip, either Single or Double.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <P>
 * A click on the icon will command a state change. Specifically, it
 * will set the CommandedState to the opposite (THROWN vs CLOSED) of
 * the current KnownState.
 *<P>
 * Note: lower west to lower east icon is used for storing the slip icon, in a single slip,
 * even if the slip is set for upper west to upper east.
 *  
 * With a 3-Way point we use the following translations
 *
 * lower west to upper east - to upper exit
 * upper west to lower east - to middle exit
 * lower west to lower east - to lower exit
 * west Turnout - First Turnout
 * east Turnout - Second Turnout
 * singleSlipRoute - translates to which exit the first turnout goes to
 * true if upper, or false if lower
 *<P>
 * Based upon the TurnoutIcon by Bob Jacobsen
 * @author Kevin Dickerson Copyright (c) 2010
 * @version $Revision: 1.2 $
 */

public class SlipTurnoutIcon extends PositionableLabel implements java.beans.PropertyChangeListener, java.io.Serializable {

    public SlipTurnoutIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/os-slip-lower-west-upper-east.gif",
                            "resources/icons/smallschematics/tracksegments/os-slip-lower-west-upper-east.gif"), editor);
        _control = true;
        displayState(turnoutState());
        debug = log.isDebugEnabled();
        setPopupUtility(null);
    }

    // the associated Turnout object
    private NamedBeanHandle<Turnout> namedTurnoutWest = null;
    private NamedBeanHandle<Turnout> namedTurnoutEast = null;
    private boolean debug = false; 
    /**
     * Attached a named turnout to this display item
     * @param pName Used as a system/user name to lookup the turnout object
     */
     public void setTurnout(String pName, boolean west) {
         if (InstanceManager.turnoutManagerInstance()!=null) {
            Turnout turnout = InstanceManager.turnoutManagerInstance().
                 provideTurnout(pName);
             if (turnout != null) {
                setTurnout(new NamedBeanHandle<Turnout>(pName, turnout), west);
             } else {
                 log.error("Turnout '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No TurnoutManager for this protocol, icon won't see changes");
         }
     }

    public void setTurnout(NamedBeanHandle<Turnout> to, boolean west) {
        if (west){
            if (namedTurnoutWest != null) {
                getTurnout(west).removePropertyChangeListener(this);
            }
            namedTurnoutWest = to;
            if (namedTurnoutWest != null) {
                displayState(turnoutState());
                getTurnout(west).addPropertyChangeListener(this);
            } 
        }else {
            if (namedTurnoutEast != null) {
                getTurnout(west).removePropertyChangeListener(this);
            }
            namedTurnoutEast = to;
            if (namedTurnoutEast != null) {
                displayState(turnoutState());
                getTurnout(west).addPropertyChangeListener(this);
            } 
        } 
    }
    
    //true for double slip, false for single.
    int turnoutType = 0x00;
    
    public static final int DOUBLESLIP = 0x00;
    public static final int SINGLESLIP = 0x02;
    public static final int THREEWAY = 0x04;
    
    public void setTurnoutType(int slip){
        turnoutType = slip;
    }
    
    public int getTurnoutType() { return turnoutType; }
    
    boolean singleSlipRoute = false;
    static boolean LOWERWESTtoLOWEREAST = false;
    static boolean UPPERWESTtoUPPEREAST = true;
    /**
    * Single Slip Route, determines if the slip route is from
    * upper west to upper east (true) or lower west to lower east (false)
    * This also doubles up for the three way and determines if the
    * first turnout routes to the upper (true) or lower (false) exit point.
    * returns 
    */
    
    public boolean getSingleSlipRoute() { return singleSlipRoute; }
    
    public void setSingleSlipRoute(boolean route) {
        singleSlipRoute = route;
    }
    
    public Turnout getTurnout(boolean west) { 
        if(west)
            return namedTurnoutWest.getBean(); 
        return namedTurnoutEast.getBean();
    }
    
    public NamedBeanHandle <Turnout> getNamedTurnout(boolean west) {
        if(west)
            return namedTurnoutWest;
        return namedTurnoutEast;
    }
    
    /*
        Note: lower west to lower east icon is used for storing the slip icon, in a single slip,
        even if the slip is set for upper west to upper east.
        
        With a 3-Way point we use the following translations
        
        lower west to upper east - to upper exit
        upper west to lower east - to middle exit
        lower west to lower east - to lower exit
    
    */

    // display icons
    String lowerWestToUpperEastLName = "resources/icons/smallschematics/tracksegments/os-slip-lower-west-upper-east.gif";
    NamedIcon lowerWestToUpperEast = new NamedIcon(lowerWestToUpperEastLName, lowerWestToUpperEastLName);
    String upperWestToLowerEastLName = "resources/icons/smallschematics/tracksegments/os-slip-upper-west-lower-east.gif";
    NamedIcon upperWestToLowerEast = new NamedIcon(upperWestToLowerEastLName, upperWestToLowerEastLName);
    String lowerWestToLowerEastLName = "resources/icons/smallschematics/tracksegments/os-slip-lower-west-lower-east.gif";
    NamedIcon lowerWestToLowerEast = new NamedIcon(lowerWestToLowerEastLName, lowerWestToLowerEastLName);
    String upperWestToUpperEastLName = "resources/icons/smallschematics/tracksegments/os-slip-upper-west-upper-east.gif";
    NamedIcon upperWestToUpperEast = new NamedIcon(upperWestToUpperEastLName, upperWestToUpperEastLName);
    String inconsistentLName = "resources/icons/smallschematics/tracksegments/os-slip-error-full.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentLName, inconsistentLName);
    String unknownLName = "resources/icons/smallschematics/tracksegments/os-slip-unknown-full.gif";
    NamedIcon unknown = new NamedIcon(unknownLName, unknownLName);

    public NamedIcon getLowerWestToUpperEastIcon() { return lowerWestToUpperEast; }
    public void setLowerWestToUpperEastIcon(NamedIcon i) {
        lowerWestToUpperEast = i;
        displayState(turnoutState());
    }

    public NamedIcon getUpperWestToLowerEastIcon() { return upperWestToLowerEast; }
    public void setUpperWestToLowerEastIcon(NamedIcon i) {
        upperWestToLowerEast = i;
        displayState(turnoutState());
    }
    
    public NamedIcon getLowerWestToLowerEastIcon() { return lowerWestToLowerEast; }
    public void setLowerWestToLowerEastIcon(NamedIcon i) {
        lowerWestToLowerEast = i;
        displayState(turnoutState());
        if(turnoutType==0x02)
            setUpperWestToUpperEastIcon(i);
        else if (turnoutType==0x04)
            setUpperWestToUpperEastIcon(i);
    } 

    public NamedIcon getUpperWestToUpperEastIcon() { return upperWestToUpperEast; }
    public void setUpperWestToUpperEastIcon(NamedIcon i) {
        upperWestToUpperEast = i;
        displayState(turnoutState());
    }     

    public NamedIcon getInconsistentIcon() { return inconsistent; }
    public void setInconsistentIcon(NamedIcon i) {
        inconsistent = i;
        displayState(turnoutState());
    }

    public NamedIcon getUnknownIcon() { return unknown; }
    public void setUnknownIcon(NamedIcon i) {
        unknown = i;
        displayState(turnoutState());
    }

    
    public int maxHeight() {
        return Math.max(
                Math.max( (lowerWestToUpperEast!=null) ? lowerWestToUpperEast.getIconHeight() : 0,
                        (upperWestToLowerEast!=null) ? upperWestToLowerEast.getIconHeight() : 0),
                Math.max(
                Math.max( (upperWestToUpperEast!=null) ? upperWestToUpperEast.getIconHeight() : 0,
                        (lowerWestToLowerEast!=null) ? lowerWestToLowerEast.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0))
            );
    }
    public int maxWidth() {
        return Math.max(
                Math.max( (lowerWestToUpperEast!=null) ? lowerWestToUpperEast.getIconWidth() : 0,
                        (upperWestToLowerEast!=null) ? upperWestToLowerEast.getIconWidth() : 0),
                Math.max(
                Math.max( (upperWestToUpperEast!=null) ? upperWestToUpperEast.getIconWidth() : 0,
                        (lowerWestToLowerEast!=null) ? lowerWestToLowerEast.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0))
            );
    }

    /**
     * Get current state of attached turnout
     * @return A state variable from a Turnout, e.g. Turnout.CLOSED
     */
    int turnoutState() {
        //Need to rework this!
        //might be as simple as adding the two states together.
        //if either turnout is not entered then the state to report
        //back will be unknown
        int state=0x00;
        if (namedTurnoutWest != null){
            if (getTurnout(true).getKnownState()==Turnout.UNKNOWN)
                return Turnout.UNKNOWN;
            if (getTurnout(true).getKnownState()==Turnout.INCONSISTENT)
                return Turnout.INCONSISTENT;
            state =+ getTurnout(true).getKnownState();
        }
        else return Turnout.UNKNOWN;
        //We add 1 to the value of the west turnout to help identify the states for both turnouts
        if (namedTurnoutEast != null) {
            if (getTurnout(false).getKnownState()==Turnout.UNKNOWN)
                return Turnout.UNKNOWN;
            if (getTurnout(false).getKnownState()==Turnout.INCONSISTENT)
                return Turnout.INCONSISTENT;
            if (getTurnout(false).getKnownState()==Turnout.CLOSED)
                state = state + (getTurnout(false).getKnownState() +1);
            if (getTurnout(false).getKnownState()==Turnout.THROWN)
                state = state + (getTurnout(false).getKnownState() +3);
        }
        else return Turnout.UNKNOWN;
        return state;
    }
    
    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " " + e.getPropertyName() + " is now "
					+ e.getNewValue());

		// when there's feedback, transition through inconsistent icon for better
		// animation
		if (getTristate()
				&& (getTurnout(true).getFeedbackMode() != Turnout.DIRECT)
				&& (e.getPropertyName().equals("CommandedState"))) {
			if ((getTurnout(true).getCommandedState() != getTurnout(true).getKnownState())
                || (getTurnout(true).getCommandedState() != getTurnout(true).getKnownState())){
				int now = Turnout.INCONSISTENT;
				displayState(now);
			}
			// this takes care of the quick double click
			if ((getTurnout(true).getCommandedState() == getTurnout(true).getKnownState())
                || (getTurnout(false).getCommandedState() == getTurnout(false).getKnownState())) {
                displayState(turnoutState());
			}
		}

		if (e.getPropertyName().equals("KnownState")) {
            displayState(turnoutState());
		}
	}

    public String getNameString() {
        String name;
        if (namedTurnoutWest == null) name = rb.getString("NotConnected");
        else name = namedTurnoutWest.getName();
        if (namedTurnoutEast != null)
            name = name + " " + namedTurnoutEast.getName();
        return name;
    }

    public void setTristate(boolean set) {
    	tristate = set;
    }    
    public boolean getTristate() { return tristate; }
    private boolean tristate = false;

    javax.swing.JCheckBoxMenuItem tristateItem = null;
    void addTristateEntry(JPopupMenu popup) {
    	tristateItem = new javax.swing.JCheckBoxMenuItem(rb.getString("Tristate"));
    	tristateItem.setSelected(getTristate());
        popup.add(tristateItem);
        tristateItem.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setTristate(tristateItem.isSelected());
            }
        });
    }

    /******** popup AbstractAction.actionPerformed method overrides *********/

    protected void rotateOrthogonal() {
        lowerWestToUpperEast.setRotation(lowerWestToUpperEast.getRotation() + 1, this);
        upperWestToLowerEast.setRotation(upperWestToLowerEast.getRotation() + 1, this);
        lowerWestToLowerEast.setRotation(lowerWestToLowerEast.getRotation() + 1, this);
        upperWestToUpperEast.setRotation(upperWestToUpperEast.getRotation() + 1, this);
        unknown.setRotation(unknown.getRotation() + 1, this);
        inconsistent.setRotation(inconsistent.getRotation() + 1,this);
        displayState(turnoutState());
        // bug fix, must repaint icons that have same width and height
        repaint();
    }

    public void setScale(double s) {
        lowerWestToUpperEast.scale(s, this);
        upperWestToLowerEast.scale(s, this);
        lowerWestToLowerEast.scale(s, this);
        upperWestToUpperEast.scale(s, this);
        unknown.scale(s, this);
        inconsistent.scale(s, this);
        displayState(turnoutState());
    }

    void rotate(int deg) {
        lowerWestToUpperEast.rotate(deg, this);
        upperWestToLowerEast.rotate(deg, this);
        lowerWestToLowerEast.rotate(deg, this);
        upperWestToUpperEast.rotate(deg, this);
        unknown.rotate(deg, this);
        inconsistent.rotate(deg, this);
        displayState(turnoutState());
    }

    /**
	 * Drive the current state of the display from the state of the turnout.
	 */
    void displayState(int state) {
    //This needs to be worked on
        log.debug(getNameString() +" displayState "+state);
        updateSize();
        // we have to make some adjustments if we are using a single slip or three way point
        // to make sure that we get the correct representation.
        switch(getTurnoutType()){
            case SINGLESLIP:
                if (singleSlipRoute && state ==9){
                    state = 0;
                } else if ((!singleSlipRoute) && state == 7) {
                    state = 0;
                }
                break;
            case THREEWAY:
                if ((state == 7) || (state == 11)) {
                    if (singleSlipRoute) {
                        state = 11;
                    } else {
                        state = 9;
                    }
                } else if (state==9) {
                    if (!singleSlipRoute) {
                        state = 11;
                    }
                }
                break;
        }
        switch (state) {
        case Turnout.UNKNOWN:
            if (isText()) super.setText(rb.getString("UnKnown"));
            if (isIcon()) super.setIcon(unknown);
            break;
        case 5: //first closed, second closed
            if (isText()) super.setText(upperWestToLowerEastText);
            if (isIcon()) super.setIcon(upperWestToLowerEast);
            break;
        case 9: // first Closed, second Thrown
            if (isText()) super.setText(lowerWestToLowerEastText);
            if (isIcon()) super.setIcon(lowerWestToLowerEast);
            break;
        case 7: //first Thrown, second Closed
            if (isText()) super.setText(upperWestToUpperEastText);
            if (isIcon()) super.setIcon(upperWestToUpperEast);
            break;
        case 11: //first Thrown second Thrown
            if (isText()) super.setText(lowerWestToUpperEastText);
            if (isIcon()) super.setIcon(lowerWestToUpperEast);
            break;
        default:
            if (isText()) super.setText(rb.getString("Inconsistent"));
            if (isIcon()) super.setIcon(inconsistent);
            break;
        }
        return;
    }
    
    String lowerWestToUpperEastText = rb.getString("LowerWestToUpperEast");
    String upperWestToLowerEastText = rb.getString("UpperWestToLowerEast");
    String lowerWestToLowerEastText = rb.getString("LowerWestToLowerEast");
    String upperWestToUpperEastText = rb.getString("UpperWestToUpperEast");
    
    public String getLWUEText(){ return lowerWestToUpperEastText; }
    public String getUWLEText(){ return upperWestToLowerEastText; }
    public String getLWLEText(){ return lowerWestToLowerEastText; }
    public String getUWUEText(){ return upperWestToUpperEastText; }
    
    public void setLWUEText(String txt){ lowerWestToUpperEastText=txt; }
    public void setUWLEText(String txt){ upperWestToLowerEastText=txt; }
    public void setLWLEText(String txt){ lowerWestToLowerEastText=txt; }
    public void setUWUEText(String txt){ upperWestToUpperEastText=txt; }
    

    SlipIconAdder _iconEditor;
    
    protected void edit() {
        if (showIconEditorFrame(this)) {
            return;
        }
        _iconEditor = new SlipIconAdder();
        _iconEditor.setTurnoutType(getTurnoutType());
        switch(getTurnoutType()){
            case DOUBLESLIP : 
                _iconEditor.setIcon(3, "LowerWestToUpperEast", getLowerWestToUpperEastIcon());
                _iconEditor.setIcon(2, "UpperWestToLowerEast", getUpperWestToLowerEastIcon());
                _iconEditor.setIcon(4, "LowerWestToLowerEast", getLowerWestToLowerEastIcon());
                _iconEditor.setIcon(5, "UpperWestToUpperEast", getUpperWestToUpperEastIcon());
                break;
            case SINGLESLIP:
                _iconEditor.setSingleSlipRoute(getSingleSlipRoute());
                _iconEditor.setIcon(3, "LowerWestToUpperEast", getLowerWestToUpperEastIcon());
                _iconEditor.setIcon(2, "UpperWestToLowerEast", getUpperWestToLowerEastIcon());
                _iconEditor.setIcon(4, "Slip", getLowerWestToLowerEastIcon());
                
                break;
            case THREEWAY:
                _iconEditor.setSingleSlipRoute(getSingleSlipRoute());
                _iconEditor.setIcon(3, "Upper", getLowerWestToUpperEastIcon());
                _iconEditor.setIcon(2, "Middle", getUpperWestToLowerEastIcon());
                _iconEditor.setIcon(4, "Lower", getLowerWestToLowerEastIcon());
                break;
        }
        _iconEditor.setIcon(0, "BeanStateInconsistent", getInconsistentIcon());
        _iconEditor.setIcon(1, "BeanStateUnknown", getUnknownIcon());
        _iconEditor.setTurnout("west", namedTurnoutWest);
        _iconEditor.setTurnout("east", namedTurnoutEast);

        _iconEditorFrame = makeAddIconFrame("EditSl", "addIconsToPanel", 
                                           "SelectTO", _iconEditor, this);
        _iconEditor.makeIconPanel();
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.turnoutPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateTurnout();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _iconEditor.addCatalog();
                    _iconEditorFrame.pack();
                }
        };
        _iconEditor.complete(addIconAction, changeIconAction, true, true);
    }
    void updateTurnout() {
        setTurnoutType(_iconEditor.getTurnoutType());
        switch(_iconEditor.getTurnoutType()){
            case DOUBLESLIP : 
                setLowerWestToUpperEastIcon(_iconEditor.getIcon("LowerWestToUpperEast"));
                setUpperWestToLowerEastIcon(_iconEditor.getIcon("UpperWestToLowerEast"));
                setLowerWestToLowerEastIcon(_iconEditor.getIcon("LowerWestToLowerEast"));
                setUpperWestToUpperEastIcon(_iconEditor.getIcon("UpperWestToUpperEast"));  
                break;
            case SINGLESLIP:
                setLowerWestToUpperEastIcon(_iconEditor.getIcon("LowerWestToUpperEast"));
                setUpperWestToLowerEastIcon(_iconEditor.getIcon("UpperWestToLowerEast"));
                setSingleSlipRoute(_iconEditor.getSingleSlipRoute());
                setLowerWestToLowerEastIcon(_iconEditor.getIcon("Slip"));
                break;
            case THREEWAY:
                setSingleSlipRoute(_iconEditor.getSingleSlipRoute());
                setLowerWestToUpperEastIcon(_iconEditor.getIcon("Upper"));
                setUpperWestToLowerEastIcon(_iconEditor.getIcon("Middle"));
                setLowerWestToLowerEastIcon(_iconEditor.getIcon("Lower"));
                break;
        }

        setInconsistentIcon(_iconEditor.getIcon("BeanStateInconsistent"));
        setUnknownIcon(_iconEditor.getIcon("BeanStateUnknown"));
        namedTurnoutWest = _iconEditor.getTurnout("west");
        namedTurnoutEast = _iconEditor.getTurnout("east");
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    /**
     * Throw the turnout when the icon is clicked
     * @param e
     */
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) return;
        if (e.isMetaDown() || e.isAltDown() ) return;
        if ((namedTurnoutWest==null) || (namedTurnoutEast==null)){
            log.error("No turnout connection, can't process click");
            return;
        }
        switch(turnoutType){
            case 0x00:  doDoubleSlipMouseClick();
                        break;
            case 0x02:  doSingleSlipMouseClick();
                        break;
            case 0x04:  do3WayMouseClick();
                        break;
        }

    }
    
    private void doDoubleSlipMouseClick(){
        switch(turnoutState()){
            case 5:  setUpperWestToUpperEast();
                     break;
            case 7:  setLowerWestToUpperEast();
                     break;
            case 9:  setUpperWestToLowerEast();
                     break;
            case 11: setLowerWestToLowerEast();
                     break;
            default : setUpperWestToLowerEast();
        }
    }
    
    private void doSingleSlipMouseClick(){
        switch(turnoutState()){
            case 5: if (singleSlipRoute)
                        setLowerWestToUpperEast();
                    else
                        setLowerWestToLowerEast();
                    break;
            case 7: if (singleSlipRoute)
                        setUpperWestToLowerEast();
                    else
                        setLowerWestToUpperEast();
                    break;
            case 9: if (singleSlipRoute)
                        setUpperWestToLowerEast();
                    else
                        setLowerWestToUpperEast();
                    break;
            case 11: if (singleSlipRoute)
                        setUpperWestToUpperEast();
                     else
                        setUpperWestToLowerEast();

                     break;
            default : setUpperWestToLowerEast();
        }
    }
    
    private void do3WayMouseClick(){
        switch(turnoutState()){
            case 5: if (singleSlipRoute)
                        setLowerWestToLowerEast();
                    else
                        setUpperWestToUpperEast();
                    break;
            case 7: if (singleSlipRoute)
                        setLowerWestToUpperEast();
                    else
                        setLowerWestToLowerEast();
                    break;
            case 9: if (singleSlipRoute)
                        setLowerWestToUpperEast();
                    else
                        setUpperWestToLowerEast();
                    break;
            case 11: if (singleSlipRoute)
                        setUpperWestToLowerEast();
                     else
                        setLowerWestToLowerEast();
                     break;
            default : setLowerWestToUpperEast();
        }
    }
    
    HashMap <Turnout, Integer> _turnoutSetting = new HashMap <Turnout, Integer>();
    
    protected HashMap<Turnout, Integer> getTurnoutSettings() { return _turnoutSetting; }
    
    protected void reset() {
        _turnoutSetting = new HashMap <Turnout, Integer>();
    }
    
    private void setUpperWestToLowerEast(){
        reset();
        _turnoutSetting.put(getTurnout(true), new Integer(jmri.Turnout.CLOSED));
        _turnoutSetting.put(getTurnout(false), new Integer(jmri.Turnout.CLOSED));
        
        setSlip();
    }
    
    private void setLowerWestToUpperEast(){
        reset();
        _turnoutSetting.put(getTurnout(false), new Integer(jmri.Turnout.THROWN));
        _turnoutSetting.put(getTurnout(true), new Integer(jmri.Turnout.THROWN));
        setSlip();
    }
    
    private void setUpperWestToUpperEast(){
        reset();
        _turnoutSetting.put(getTurnout(true), new Integer(jmri.Turnout.THROWN));
        _turnoutSetting.put(getTurnout(false), new Integer(jmri.Turnout.CLOSED));
        setSlip();
    }
    
    private void setLowerWestToLowerEast(){
        reset();
        _turnoutSetting.put(getTurnout(true), new Integer(jmri.Turnout.CLOSED));
        _turnoutSetting.put(getTurnout(false), new Integer(jmri.Turnout.THROWN));
        setSlip();
    }
    
    /**
    * Displays a popup menu to select a given state, rather than cycling
    * through each state
    * @param popup
    */
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            // add tristate option if turnout has feedback
            boolean returnstate=false;
            if (namedTurnoutWest != null && getTurnout(true).getFeedbackMode() != Turnout.DIRECT) {
                addTristateEntry(popup);
                returnstate = true;
            }
            if (namedTurnoutEast != null && getTurnout(false).getFeedbackMode() != Turnout.DIRECT) {
                addTristateEntry(popup);
                returnstate = true;
            }
            return returnstate;
        } else {
            JMenuItem LWUE = new JMenuItem(lowerWestToUpperEastText);
            if ((turnoutType==0x04) && (!singleSlipRoute)){
                LWUE.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { setLowerWestToLowerEast(); }
                });
                
            } else {
                LWUE.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { setLowerWestToUpperEast(); }
                });    
            }
            popup.add(LWUE); 
            JMenuItem UWLE = new JMenuItem(upperWestToLowerEastText);
            UWLE.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { setUpperWestToLowerEast(); }
            });
            popup.add(UWLE);
            if ((turnoutType==0x00)||((turnoutType==0x02)&&(!singleSlipRoute))){
                JMenuItem LWLE = new JMenuItem(lowerWestToLowerEastText);
                LWLE.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { setLowerWestToLowerEast(); }
                });
                popup.add(LWLE);
            }
            if ((turnoutType==0x00)||((turnoutType==0x02)&&(singleSlipRoute))){
                JMenuItem UWUE = new JMenuItem(upperWestToUpperEastText);
                UWUE.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { setUpperWestToUpperEast(); }
                });
                popup.add(UWUE);
            }
            if (turnoutType==0x04){
                JMenuItem LWLE = new JMenuItem(lowerWestToLowerEastText);
                if(!singleSlipRoute){
                    LWLE.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) { setLowerWestToUpperEast(); }
                    });
                } else {
                    LWLE.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) { setLowerWestToLowerEast(); }
                    });
                }
                popup.add(LWLE);
            }
        }
        return true;
    }
    
    // overide
    public boolean setTextEditMenu(JPopupMenu popup) {
        String popuptext = rb.getString("SetSlipText");
        if (turnoutType==0x04)
            popuptext = rb.getString("Set3WayText");
        popup.add(new AbstractAction(popuptext) {
            public void actionPerformed(ActionEvent e) {
                String name = getNameString();
                SlipTurnoutTextEdit(name);
            }
        });
        return true;
    }
    
    public void SlipTurnoutTextEdit(String name) {
        if (debug) log.debug("make text edit menu");

        SlipTurnoutTextEdit f = new SlipTurnoutTextEdit();
        f.addHelpMenu("package.jmri.jmrit.display.SlipTurnoutTextEdit", true);
        try {
            f.initComponents(this, name);
            }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
            }
        f.setVisible(true);
    }

    public void dispose() {
        if (namedTurnoutWest != null) {
            getTurnout(true).removePropertyChangeListener(this);
        }
        namedTurnoutWest = null;
        if (namedTurnoutEast != null) {
            getTurnout(false).removePropertyChangeListener(this);
        }
        namedTurnoutEast = null;
        lowerWestToUpperEast = null;
        upperWestToLowerEast = null;
        lowerWestToLowerEast = null;
        upperWestToUpperEast = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    boolean busy = false;
    
    /**
     * Method to set Slip busy when commands are being issued to 
     *   Slip turnouts
	 */
    protected void setSlipBusy() {
		busy = true;
	}

    /**
     * Method to set Slip not busy when all commands have been
     *   issued to Slip turnouts
	 */
    protected void setSlipNotBusy() {
		busy = false;
	}

    /**
     * Method to query if Slip is busy (returns true if commands are
     *   being issued to Slips turnouts)
	 */
    protected boolean isSlipBusy() {
		return (busy);
	}
    
     /**
     * Method to set the Slip
     * Sets the slips Turnouts to the state required
	 * This call is ignored if the slip is 'busy', i.e., if there is a 
	 *    thread currently sending commands to this Slips's turnouts.
     */
    private void setSlip() {
			if (!busy) {
				setSlipBusy();
				SetSlipThread thread = new SetSlipThread(this);
				thread.start();
			}
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SlipTurnoutIcon.class.getName());
}

class SetSlipThread extends Thread {
	/**
	 * Constructs the thread
	 */
    public SetSlipThread(SlipTurnoutIcon aSlip) {
		s = aSlip;
	}    
    //This is used to set the two turnouts, with a delay of 250ms between each one.
    public void run() {
		
        HashMap <Turnout, Integer> _turnoutSetting = s.getTurnoutSettings();
        
        Iterator itr = _turnoutSetting.keySet().iterator();
        while(itr.hasNext()) {
            Turnout t = (Turnout) itr.next();
            int state = _turnoutSetting.get(t);
            t.setCommandedState(state);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
        }
        
        //set Slip not busy
		s.setSlipNotBusy();   
	}
    
    private SlipTurnoutIcon s;
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SetSlipThread.class.getName());

    
}
