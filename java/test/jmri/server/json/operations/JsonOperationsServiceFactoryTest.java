package jmri.server.json.operations;

import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.operations.JsonOperations.CAR;
import static jmri.server.json.operations.JsonOperations.CARS;
import static jmri.server.json.operations.JsonOperations.CAR_TYPE;
import static jmri.server.json.operations.JsonOperations.ENGINE;
import static jmri.server.json.operations.JsonOperations.KERNEL;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.operations.JsonOperations.LOCATIONS;
import static jmri.server.json.operations.JsonOperations.ROLLING_STOCK;
import static jmri.server.json.operations.JsonOperations.TRACK;
import static jmri.server.json.operations.JsonOperations.TRAIN;
import static jmri.server.json.operations.JsonOperations.TRAINS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.server.json.JSON;
import jmri.server.json.JsonServiceFactoryTestBase;

/**
 * 
 * @author Randall Wood Copyright 2020
 */
public class JsonOperationsServiceFactoryTest extends JsonServiceFactoryTestBase<JsonOperationsHttpService, JsonOperationsSocketService> {

    @BeforeEach
    public void setup() throws Exception {
        super.setUp();
        factory = new JsonOperationsServiceFactory();
    }

    @Override
    @Test
    public void testGetTypesV5() {
        assertThat(factory.getTypes(JSON.V5))
                .containsExactly(CAR, CARS, CAR_TYPE, ENGINE, ENGINES, KERNEL, LOCATION, LOCATIONS, ROLLING_STOCK,
                        TRACK, TRAIN, TRAINS);
    }
}
