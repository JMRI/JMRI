package jmri.jmrit.signalsystemeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * A copyright
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class Copyright {

    private String _holder;
    private final List<String> _dates = new ArrayList<>();

    public void setHolder(String holder) {
        this._holder = holder;
    }

    public String getHolder() {
        return this._holder;
    }

    public List<String> getDates() {
        return this._dates;
    }

}
