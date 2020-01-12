package jmri.jmrit.symbolicprog;

import java.util.List;

/**
 * Force a set of Qualifiers to work in an AND relationship.
 * <p>
 * On transition, the qualifiers are evaluated in order, stopping when the
 * outcome is known.
 *
 * @author Bob Jacobsen Copyright (C) 2011
 *
 */
public class QualifierCombiner implements Qualifier, java.beans.PropertyChangeListener {

    public QualifierCombiner(List<Qualifier> qualifiers) {
        this.qualifiers = qualifiers;

        setWatchedAvailable(currentDesiredState());

    }

    List<Qualifier> qualifiers;

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // this was a change, may want to change it back
        setWatchedAvailable(currentDesiredState());  // relies on non-propogation of null changes
    }

    @Override
    public void setWatchedAvailable(boolean enable) {
        qualifiers.get(0).setWatchedAvailable(enable);
    }

    @Override
    public boolean currentDesiredState() {
        for (Qualifier q : qualifiers) {
            if (!q.currentDesiredState()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void update() {
        setWatchedAvailable(currentDesiredState());
    }
}
