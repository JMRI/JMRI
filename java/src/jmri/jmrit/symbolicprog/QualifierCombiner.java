package jmri.jmrit.symbolicprog;

import java.util.ArrayList;
import java.util.List;

/**
 * Force a set of Qualifiers to work in an AND relationship.
 * <p>
 * On transition, the qualifiers are evaluated in order, stopping when the
 * outcome is known.
 *
 * @author Bob Jacobsen Copyright (C) 2011
 */
public class QualifierCombiner implements Qualifier, java.beans.PropertyChangeListener {

    public QualifierCombiner(List<Qualifier> qualifiers) {
        this.qualifiers = qualifiers;

        // handle the change events here so
        // add a listener for each VariableValue used by the component qualifiers
        // and remove the listeners for the component qualifiers
        ArrayList<VariableValue> lv = new ArrayList<VariableValue>();
        for (Qualifier q : qualifiers) {
            AbstractQualifier aq = (AbstractQualifier)q;
            VariableValue v = aq.getWatchedVariable();
            if (v != null) {
                // only add one listener for any given VariableValue
                if (!lv.contains(v)) {
                    lv.add(v);
                    v.addPropertyChangeListener(this);
                }
                // remove listener from component qualifier
                v.removePropertyChangeListener(aq);
            }
        }

        setWatchedAvailable(currentDesiredState());
    }

    List<Qualifier> qualifiers;

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("Value")) {
            setWatchedAvailable(currentDesiredState());  // relies on non-propagation of null changes
        }
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
