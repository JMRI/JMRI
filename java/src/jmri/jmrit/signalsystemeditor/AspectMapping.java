package jmri.jmrit.signalsystemeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * An aspect mapping
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class AspectMapping {

    private String _advancedAspect;
    private final List<String> _ourAspects = new ArrayList<>();

    public AspectMapping(String advancedAspect) {
        this._advancedAspect = advancedAspect;
    }

    public void setAdvancedAspect(String advancedAspect) {
        this._advancedAspect = advancedAspect;
    }

    public String getAdvancedAspect() {
        return this._advancedAspect;
    }

    public List<String> getOurAspects() {
        return this._ourAspects;
    }

}
