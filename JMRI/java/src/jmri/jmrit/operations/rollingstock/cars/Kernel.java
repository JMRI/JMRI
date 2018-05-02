package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.operations.rollingstock.RollingStockGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Kernel is a group of cars that is managed as one car.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 */
public class Kernel extends RollingStockGroup<Car> {

    public Kernel(String name) {
        super(name);
        log.debug("New Kernel ({})", name);
    }

    public List<Car> getCars() {
        return new ArrayList<>(getGroup());
    }

    @Override
    public void dispose() {
        while (getGroup().size() > 0) {
            Car car = getGroup().get(0);
            if (car != null) {
                car.setKernel(null);
            }
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(Kernel.class);
}
