package jmri.jmrit.operations.trains.excel;

import jmri.InstanceManagerAutoDefault;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = MAINTAINED)
public class TrainCustomSwitchList extends TrainCustomCommon implements InstanceManagerAutoDefault {

    public TrainCustomSwitchList() {
        super("csvSwitchLists", Xml.SWITCHLIST_CREATOR); // NOI18N
    }
}
