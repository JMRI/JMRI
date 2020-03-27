package jmri.jmrit.operations.trains.excel;

import jmri.InstanceManagerAutoDefault;

public class TrainCustomManifest extends TrainCustomCommon implements InstanceManagerAutoDefault {

    public TrainCustomManifest() {
        super("csvManifests", Xml.MANIFEST_CREATOR); // NOI18N
    }
}
