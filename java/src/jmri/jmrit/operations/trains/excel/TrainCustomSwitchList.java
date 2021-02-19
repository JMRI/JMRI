package jmri.jmrit.operations.trains.excel;

import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.trains.TrainManagerXml;

public class TrainCustomSwitchList extends TrainCustomCommon implements InstanceManagerAutoDefault {

    public TrainCustomSwitchList() {
        super(TrainManagerXml.CSV_SWITCH_LISTS, Xml.SWITCHLIST_CREATOR); // NOI18N
    }
}
