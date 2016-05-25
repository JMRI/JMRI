// Kernel.java
package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Kernel is a group of cars that is managed as one car.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */
public class Kernel extends RollingStockGroup {

    public Kernel(String name) {
        super(name);
        log.debug("New Kernel ({})", name);
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "getGroup() only provides Car Objects")
    public List<Car> getCars() {
        List<Car> cars = new ArrayList<Car>();
        for (RollingStock rs : getGroup()) {
            cars.add((Car) rs);
        }
        return cars;
    }

    @Override
    public void dispose() {
        while (getGroup().size() > 0) {
            Car car = (Car) getGroup().get(0);
            if (car != null) {
                car.setKernel(null);
            }
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(Kernel.class.getName());
}
