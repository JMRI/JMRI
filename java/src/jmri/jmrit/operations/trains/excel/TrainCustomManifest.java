package jmri.jmrit.operations.trains.excel;

import jmri.InstanceManagerAutoDefault;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = MAINTAINED)
public class TrainCustomManifest extends TrainCustomCommon implements InstanceManagerAutoDefault {

    public TrainCustomManifest() {
        super("csvManifests", Xml.MANIFEST_CREATOR); // NOI18N
    }
}
