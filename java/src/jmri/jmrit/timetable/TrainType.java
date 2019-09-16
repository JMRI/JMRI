package jmri.jmrit.timetable;

import java.awt.Color;
import jmri.util.swing.JmriColorChooser;
/**
 * Define the content of a Train Type record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class TrainType {

    /**
     * Create a new train type with default values.
     * @param layoutId The parent layout id.
     * @throws IllegalArgumentException TYPE_ADD_FAIL
     */
    public TrainType(int layoutId) throws IllegalArgumentException {
        if (_dm.getLayout(layoutId) == null) {
            throw new IllegalArgumentException(_dm.TYPE_ADD_FAIL);
        }
        _typeId = _dm.getNextId("TrainType");  // NOI18N
        _layoutId = layoutId;
        _dm.addTrainType(_typeId, this);
    }

    public TrainType(int typeId, int layoutId, String typeName, String typeColor) {
        _typeId = typeId;
        _layoutId = layoutId;
        setTypeName(typeName);
        setTypeColor(typeColor);
    }

    TimeTableDataManager _dm = TimeTableDataManager.getDataManager();

    private final int _typeId;
    private final int _layoutId;
    private String _typeName = "New Type";  // NOI18N
    private String _typeColor = "#000000";  // NOI18N

    public int getTypeId() {
        return _typeId;
    }

    public int getLayoutId() {
        return _layoutId;
    }

    public String getTypeName() {
        return _typeName;
    }

    public void setTypeName(String newName) {
        _typeName = newName;
    }

    public String getTypeColor() {
        return _typeColor;
    }

    public void setTypeColor(String newColor) {
        _typeColor = newColor;
        JmriColorChooser.addRecentColor(Color.decode(newColor));
    }

    @Override
    public String toString() {
        return _typeName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainType.class);
}
