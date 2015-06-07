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

    private static final long serialVersionUID = -2962313838094980115L;

    public ProfileListModel() {
        ProfileManager.defaultManager().addPropertyChangeListener(ProfileManager.PROFILES, (PropertyChangeEvent evt) -> {
            if (evt instanceof IndexedPropertyChangeEvent
                    && evt.getSource().equals(ProfileManager.defaultManager())) {
                if (evt.getOldValue() == null) {
                    fireIntervalAdded(((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
                } else if (evt.getNewValue() == null) {
                    fireIntervalRemoved(((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
                }
                fireContentsChanged(((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
            }
        });
    }

    @Override
    public int getSize() {
        return ProfileManager.defaultManager().getProfiles().length;
    }

    @Override
    public Profile getElementAt(int index) {
        return ProfileManager.defaultManager().getProfiles(index);
    }

    private void fireContentsChanged(int index0, int index1) {
        super.fireContentsChanged(this, index0, index1);
    }

    private void fireIntervalAdded(int index0, int index1) {
        super.fireIntervalAdded(this, index0, index1);
    }

    private void fireIntervalRemoved(int index0, int index1) {
        super.fireIntervalRemoved(this, index0, index1);
    }
}
