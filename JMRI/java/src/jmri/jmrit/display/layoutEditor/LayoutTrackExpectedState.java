package jmri.jmrit.display.layoutEditor;

import jmri.ExpectedState;
import jmri.beans.Bean;

/**
 * Retain a LayoutTrack and its expected state.
 *
 * @author Randall Wood Copyright 2017
 * @param <T> the supported type of LayoutTrack
 */
public class LayoutTrackExpectedState<T extends LayoutTrack> extends Bean implements ExpectedState<T, Integer> {

    private final T layoutTrack;
    private Integer state;

    public LayoutTrackExpectedState(T layoutTrack, Integer state) {
        this.layoutTrack = layoutTrack;
        LayoutTrackExpectedState.this.setExpectedState(state);
    }

    @Override
    public Integer getExpectedState() {
        return state;
    }

    @Override
    public void setExpectedState(Integer state) throws UnsupportedOperationException {
        Integer old = this.state;
        this.state = state;
        this.propertyChangeSupport.firePropertyChange(EXPECTED_STATE, old, state);
    }

    @Override
    public T getObject() {
        return this.layoutTrack;
    }

}
