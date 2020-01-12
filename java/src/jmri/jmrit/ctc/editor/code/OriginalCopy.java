package jmri.jmrit.ctc.editor.code;

import java.util.ArrayList;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.ctc.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctc.ctcserialdata.OtherData;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *
 * This object just maintains a deep copy of all of the originalReference data
 * read from the .xml file.  A more proper place to put this would have been in
 * the CTCSerialData object, however, since that object is shared by the runtime
 * system, I didn't want two copies of everything, plus other CPU overhead
 * in making the copies in the runtime system.
 * 
 * All objects tracked in here (OtherData, ArrayList CodeButtonHandlerData
 * MUST have a deepCopy routine available.
 *
 * Ergo I put it here, so it only exists within the GUI system.
 * 
 */

public class OriginalCopy {
//  Original copies after read in for modification detection:
    private OtherData _mOriginalCopyOtherData;
    private ArrayList <CodeButtonHandlerData> _mOriginalCopyCodeButtonHandlerDataArrayList;
    
    public OriginalCopy() {
        _mOriginalCopyOtherData = new OtherData();
        _mOriginalCopyCodeButtonHandlerDataArrayList = new ArrayList<>();
    }
    
    public void makeDeepCopy(CTCSerialData ctcSerialData) {
        _mOriginalCopyOtherData = ctcSerialData.getOtherData().deepCopy();
        _mOriginalCopyCodeButtonHandlerDataArrayList = new ArrayList<>();
        ArrayList <CodeButtonHandlerData> originalReference = ctcSerialData.getCodeButtonHandlerDataArrayList();
        for (CodeButtonHandlerData codeButtonHandlerData : originalReference) {
            _mOriginalCopyCodeButtonHandlerDataArrayList.add(codeButtonHandlerData.deepCopy());
        }
    }
    
    public boolean changed(CTCSerialData ctcSerialData) {
        if (!ClassCompareContents.objectsEqual(ctcSerialData.getOtherData(), _mOriginalCopyOtherData)) return true;
        ArrayList <CodeButtonHandlerData> originalReference = ctcSerialData.getCodeButtonHandlerDataArrayList();
        if (originalReference.size() != _mOriginalCopyCodeButtonHandlerDataArrayList.size()) return true;
        for (int index = 0; index < originalReference.size(); index++) {
            if (!ClassCompareContents.objectsEqual(originalReference.get(index), _mOriginalCopyCodeButtonHandlerDataArrayList.get(index))) return true;
        }
        return false;   // If nothing compares different, is the same.
    }
}
