package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;
import jmri.util.NamedBeanHandle;

/**
 * An icon to display a status of a turnout.<P>
 * This responds to only KnownState, leaving CommandedState to some other
 * graphic representation later.
 * <P>
 * A click on the icon will command a state change. Specifically, it
 * will set the CommandedState to the opposite (THROWN vs CLOSED) of
 * the current KnownState.
 *<P>
 * The default icons are for a left-handed turnout, facing point
 * for east-bound traffic.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision: 1.56 $
 */

public class TurnoutIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public TurnoutIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                            "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif"), editor);
        _control = true;
        displayState(turnoutState());
        setPopupUtility(null);
    }

    public Positionable clone() {
        TurnoutIcon pos = new TurnoutIcon(_editor);
        pos.setTurnout(getNameString());
        pos.setClosedIcon(cloneIcon(getClosedIcon(), pos));
        pos.setThrownIcon(cloneIcon(getThrownIcon(), pos));
        pos.setInconsistentIcon(cloneIcon(getInconsistentIcon(), pos));
        pos.setUnknownIcon(cloneIcon(getUnknownIcon(), pos));
        pos.setTristate(getTristate());
        finishClone(pos);
        return pos;
    }

    // the associated Turnout object
    //Turnout turnout = null;
    private NamedBeanHandle<Turnout> namedTurnout = null;

    /**
     * Attached a named turnout to this display item
     * @param pName Used as a system/user name to lookup the turnout object
     */
     public void setTurnout(String pName) {
         if (InstanceManager.turnoutManagerInstance()!=null) {
            Turnout turnout = InstanceManager.turnoutManagerInstance().
                 provideTurnout(pName);
             if (turnout != null) {
                 setTurnout(new NamedBeanHandle<Turnout>(pName, turnout));
             } else {
                 log.error("Turnout '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No TurnoutManager for this protocol, icon won't see changes");
         }
     }

    public void setTurnout(NamedBeanHandle<Turnout> to) {
        if (namedTurnout != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedTurnout = to;
        if (namedTurnout != null) {
            displayState(turnoutState());
            getTurnout().addPropertyChangeListener(this);
        } 
    }

    public Turnout getTurnout() { return namedTurnout.getBean(); }
    
    public NamedBeanHandle <Turnout> getNamedTurnout() {
        return namedTurnout;
    }

    // display icons
    String closedLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif";
    NamedIcon closed = new NamedIcon(closedLName, closedLName);
    String thrownLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif";
    NamedIcon thrown = new NamedIcon(thrownLName, thrownLName);
    String inconsistentLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif";
    NamedIcon inconsistent = new NamedIcon(inconsistentLName, inconsistentLName);
    String unknownLName = "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif";
    NamedIcon unknown = new NamedIcon(unknownLName, unknownLName);

    public NamedIcon getClosedIcon() { return closed; }
    public void setClosedIcon(NamedIcon i) {
        closed = i;
        displayState(turnoutState());
    }

    public NamedIcon getThrownIcon() { return thrown; }
    public void setThrownIcon(NamedIcon i) {
        thrown = i;
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
                Math.max( (closed!=null) ? closed.getIconHeight() : 0,
                        (thrown!=null) ? thrown.getIconHeight() : 0),
                Math.max((unknown!=null) ? unknown.getIconHeight() : 0,
                        (inconsistent!=null) ? inconsistent.getIconHeight() : 0)
            );
    }
    public int maxWidth() {
        return Math.max(
                Math.max((closed!=null) ? closed.getIconWidth() : 0,
                        (thrown!=null) ? thrown.getIconWidth() : 0),
                Math.max((unknown!=null) ? unknown.getIconWidth() : 0,
                        (inconsistent!=null) ? inconsistent.getIconWidth() : 0)
            );
    }

    /**
     * Get current state of attached turnout
     * @return A state variable from a Turnout, e.g. Turnout.CLOSED
     */
    int turnoutState() {
        if (namedTurnout != null) return getTurnout().getKnownState();
        else return Turnout.UNKNOWN;
    }
    
    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled())
			log.debug("property change: " + getNameString() + " " + e.getPropertyName() + " is now "
					+ e.getNewValue());

		// when there's feedback, transition through inconsistent icon for better
		// animation
		if (getTristate()
				&& (getTurnout().getFeedbackMode() != Turnout.DIRECT)
				&& (e.getPropertyName().equals("CommandedState"))) {
			if (getTurnout().getCommandedState() != getTurnout().getKnownState()) {
				int now = Turnout.INCONSISTENT;
				displayState(now);
			}
			// this takes care of the quick double click
			if (getTurnout().getCommandedState() == getTurnout().getKnownState()) {
				int now = ((Integer) e.getNewValue()).intValue();
				displayState(now);
			}
		}

		if (e.getPropertyName().equals("KnownState")) {
			int now = ((Integer) e.getNewValue()).intValue();
			displayState(now);
		}
	}

    public String getNameString() {
        String name;
        if (namedTurnout == null) name = rb.getString("NotConnected");
        else name = namedTurnout.getName();
        /*else if (getTurnout().getUserName()!=null)
            name = getTurnout().getUserName()+" ("+getTurnout().getSystemName()+")";
        else
            name = getTurnout().getSystemName();*/
        return name;
    }

    public void setTristate(boolean set) {
    	tristate = set;
    }    
    public boolean getTristate() { return tristate; }
    private boolean tristate = false;

    /**
     * Pop-up displays unique attributes of turnouts
     */
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            // add tristate option if turnout has feedback
            if (namedTurnout != null && getTurnout().getFeedbackMode() != Turnout.DIRECT) {
                addTristateEntry(popup);
                return true;
            }
            return false;
        }
        return true;
	}

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
        closed.setRotation(closed.getRotation() + 1, this);
        thrown.setRotation(thrown.getRotation() + 1, this);
        unknown.setRotation(unknown.getRotation() + 1, this);
        inconsistent.setRotation(inconsistent.getRotation() + 1,this);
        displayState(turnoutState());
        // bug fix, must repaint icons that have same width and height
        repaint();
    }

    public void setScale(double s) {
        closed.scale(s, this);
        thrown.scale(s, this);
        unknown.scale(s, this);
        inconsistent.scale(s, this);
        displayState(turnoutState());
    }

    public void rotate(int deg) {
        closed.rotate(deg, this);
        thrown.rotate(deg, this);
        unknown.rotate(deg, this);
        inconsistent.rotate(deg, this);
        displayState(turnoutState());
    }

    /**
	 * Drive the current state of the display from the state of the turnout.
	 */
    void displayState(int state) {
        log.debug(getNameString() +" displayState "+state);
        updateSize();
        switch (state) {
        case Turnout.UNKNOWN:
            if (isText()) super.setText(rb.getString("UnKnown"));
            if (isIcon()) super.setIcon(unknown);
            break;
        case Turnout.CLOSED:
            if (isText()) super.setText(InstanceManager.turnoutManagerInstance().getClosedText());
            if (isIcon()) super.setIcon(closed);
            break;
        case Turnout.THROWN:
            if (isText()) super.setText(InstanceManager.turnoutManagerInstance().getThrownText());
            if (isIcon()) super.setIcon(thrown);
            break;
        default:
            if (isText()) super.setText(rb.getString("Inconsistent"));
            if (isIcon()) super.setIcon(inconsistent);
            break;
        }

        return;
    }

    protected void edit() {
        if (showIconEditorFrame(this)) {
            return;
        }
        _iconEditor = new IconAdder();
        _iconEditor.setIcon(3, "TurnoutStateClosed", getClosedIcon());
        _iconEditor.setIcon(2, "TurnoutStateThrown", getThrownIcon());
        _iconEditor.setIcon(0, "BeanStateInconsistent", getInconsistentIcon());
        _iconEditor.setIcon(1, "BeanStateUnknown", getUnknownIcon());
        _iconEditorFrame = makeAddIconFrame("EditTO", "addIconsToPanel", 
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
        _iconEditor.setSelection(getTurnout());
    }
    void updateTurnout() {
        setClosedIcon(_iconEditor.getIcon("TurnoutStateClosed"));
        setThrownIcon(_iconEditor.getIcon("TurnoutStateThrown"));
        setInconsistentIcon(_iconEditor.getIcon("BeanStateInconsistent"));
        setUnknownIcon(_iconEditor.getIcon("BeanStateUnknown"));
        setTurnout(_iconEditor.getTableSelection().getDisplayName());
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
        if (namedTurnout==null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        if (getTurnout().getKnownState()==jmri.Turnout.CLOSED)
            getTurnout().setCommandedState(jmri.Turnout.THROWN);
        else
            getTurnout().setCommandedState(jmri.Turnout.CLOSED);
    }

    public void dispose() {
        if (namedTurnout != null) {
            getTurnout().removePropertyChangeListener(this);
        }
        namedTurnout = null;

        closed = null;
        thrown = null;
        inconsistent = null;
        unknown = null;

        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutIcon.class.getName());
}
