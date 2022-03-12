package jmri.jmrit.operations.trains.excel;

import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.trains.TrainManagerXml;

public class TrainCustomManifest extends TrainCustomCommon implements InstanceManagerAutoDefault {

    public TrainCustomManifest() {
        super(TrainManagerXml.CSV_MANIFESTS, Xml.MANIFEST_CREATOR); // NOI18N
    }
}
