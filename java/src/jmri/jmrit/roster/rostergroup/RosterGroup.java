package jmri.jmrit.roster.rostergroup;

import java.util.List;
import jmri.beans.Bean;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterObject;

/**
 * A RosterGroup object contains information about groupings of entries within
 * the {@link jmri.jmrit.roster.Roster}.
 *
 * This object allows groups to be manipulated as Java beans.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class RosterGroup extends Bean implements RosterObject {

    private String name;

    /**
     * Create a roster group.
     *
     * This sets the name without calling {@link #setName(java.lang.String) }.
     *
     */
    public RosterGroup(String name) {
        this.name = name;
    }

    /**
     * Get the list of entries associated with this group.
     *
     * @return the list of entries or an empty list.
     */
    public List<RosterEntry> getEntries() {
        return Roster.getDefault().getEntriesInGroup(this.getName());
    }

    /**
     * Get the RosterGroup's name.
     *
     * Use {@link #getDisplayName() } to get the name to be displayed to a user.
     *
     * @return the name
     * @see #getDisplayName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the RosterGroup's name, changing it in every entry associated with
     * the roster.
     *
     * @param newName the new name
     */
    public void setName(String newName) {
        if (Roster.getDefault().getRosterGroups().containsKey(newName)) {
            return;
        }
        String oldName = this.name;
        String oldGroup = Roster.getRosterGroupProperty(oldName);
        String newGroup = Roster.getRosterGroupProperty(newName);
        Roster.getDefault().remapRosterGroup(this, newName);
        for (RosterEntry re : this.getEntries()) {
            re.putAttribute(newGroup, "yes"); // NOI18N
            re.deleteAttribute(oldGroup);
        }
        this.name = newName;
        Roster.getDefault().rosterGroupRenamed(oldName, newName);
    }

    @Override
    public String getDisplayName() {
        return this.getName();
    }

    /**
     * Flag indicating that this RosterGroup can be edited by the user.
     *
     * The default implementation always returns true.
     *
     * @return true if the group can be edited.
     */
    public boolean canEdit() {
        return true;
    }

    /**
     * Flag indicating that this RosterGroup can be deleted by the user.
     *
     * The default implementation always returns true.
     *
     * @return true if the group can be deleted.
     */
    public boolean canDelete() {
        return true;
    }

    /**
     * Flag indicating that this RosterGroup can be duplicated by the user.
     *
     * The default implementation always returns true.
     *
     * @return true if the group can be copied.
     */
    public boolean canCopy() {
        return true;
    }

    /**
     * Flag indicating that the contents of this RosterGroup can be changed by
     * the user.
     *
     * The default implementation always returns true.
     *
     * @return true if entries in this group can be changed.
     */
    public boolean canChangeContents() {
        return true;
    }
}
