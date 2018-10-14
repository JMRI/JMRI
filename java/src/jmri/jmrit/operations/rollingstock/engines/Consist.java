package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.operations.rollingstock.RollingStockGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consist is a group of engines that is managed as one engine.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 */
public class Consist extends RollingStockGroup<Engine> {

    protected int _consistNumber = 0;

    public Consist(String name) {
        super(name);
        log.debug("New Consist ({})", name);
    }

    public List<Engine> getEngines() {
        return new ArrayList<>(getGroup());
    }

    public int getConsistNumber() {
        return _consistNumber;
    }

    /**
     *
     * @param number DCC address for this consist
     */
    public void setConsistNumber(int number) {
        _consistNumber = number;
    }

    @Override
    public void dispose() {
        while (getGroup().size() > 0) {
            Engine engine = getGroup().get(0);
            if (engine != null) {
                engine.setConsist(null);
            }
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(Consist.class);
}
