/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.profile;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractListModel;

/**
 *
 * @author rhwood
 */
public class SearchPathsListModel extends AbstractListModel {

    public SearchPathsListModel() {
        ProfileManager.defaultManager().addPropertyChangeListener(ProfileManager.SEARCH_PATHS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt instanceof IndexedPropertyChangeEvent
                        && evt.getSource().equals(ProfileManager.defaultManager())) {
                    if (evt.getOldValue() == null) {
                        fireIntervalAdded(((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
                    } else if (evt.getNewValue() == null) {
                        fireIntervalRemoved(((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
                    }
                    fireContentsChanged(((IndexedPropertyChangeEvent) evt).getIndex(), ((IndexedPropertyChangeEvent) evt).getIndex());
                }
            }
        });
    }

    @Override
    public int getSize() {
        return ProfileManager.defaultManager().getSearchPaths().length;
    }

    @Override
    public Object getElementAt(int index) {
        return ProfileManager.defaultManager().getSearchPaths(index);
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
