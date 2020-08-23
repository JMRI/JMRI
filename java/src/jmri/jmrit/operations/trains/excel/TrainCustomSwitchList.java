package jmri.jmrit.operations.trains.excel;

import jmri.InstanceManagerAutoDefault;

public class TrainCustomSwitchList extends TrainCustomCommon implements InstanceManagerAutoDefault {

    public TrainCustomSwitchList() {
        super("csvSwitchLists", Xml.SWITCHLIST_CREATOR); // NOI18N
    }
}
