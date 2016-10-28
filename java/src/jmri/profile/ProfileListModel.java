package jmri.profile;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.AbstractListModel;

/**
 * A list of {@link jmri.profile.Profile}s, suitable for use in Swing components
 * that display a list of items.
 *
 * @author Randall Wood
 */
public class ProfileListModel extends AbstractListModel<Profile> {

    public ProfileListModel() {
        ProfileManager.getDefault().addPropertyChangeListener(ProfileManager.PROFILES, (PropertyChangeEvent evt) -> {
            if (evt instanceof IndexedPropertyChangeEvent
                    && evt.getSource().equals(ProfileManager.getDefault())) {
                if (evt.getOldValue() == null) {
                    this.fireIntervalAdded(this, ((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
                } else if (evt.getNewValue() == null) {
                    this.fireIntervalRemoved(this, ((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
                }
                this.fireContentsChanged(this, ((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
            }
        });
    }

    @Override
    public int getSize() {
        return ProfileManager.getDefault().getProfiles().length;
    }

    @Override
    public Profile getElementAt(int index) {
        return ProfileManager.getDefault().getProfiles(index);
    }
}
