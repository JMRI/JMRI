// RosterGroupSelector.java
package jmri.jmrit.roster.rostergroup;

/**
 * The getter method for a roster group selection.
 * <p>
 * Objects that implement this interface will be able to provide a source for
 * getting a roster group to other objects that manipulate roster groups.
 * 
 * @author rhwood
 */
public interface RosterGroupSelector {
    
    public String getSelectedRosterGroup();
    
}
