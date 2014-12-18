package jmri.jmrit.roster.rostergroup;

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
        String oldGroup = Roster.getRosterGroupProperty(this.name);
        String newGroup = Roster.getRosterGroupProperty(name);
        for (RosterEntry re : this.getEntries()) {
            re.putAttribute(newGroup, "yes"); // NOI18N
            re.deleteAttribute(oldGroup);
        }
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return this.getName();
    }

}
