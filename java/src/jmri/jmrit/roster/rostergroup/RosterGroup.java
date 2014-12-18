package jmri.jmrit.roster.rostergroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterObject;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class RosterGroup extends RosterObject {

    private String name;

    public RosterGroup(String aName) {
        this.name = aName;
        Roster.instance().addPropertyChangeListener(Roster.ROSTER_GROUP_RENAMED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getOldValue().equals(name)) {
                    name = evt.getNewValue().toString();
                }
            }
        });
    }

    public List<RosterEntry> getEntries() {
        return Roster.instance().getEntriesInGroup(this.getName());
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        Roster.instance().renameRosterGroupList(this.name, name);
        this.name = name;
    }

}
