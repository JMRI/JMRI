/*
 * A CTCFileModel object loads an xml file when the program is started.
 * <P>
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import apps.startup.AbstractStartupModel;
import jmri.JmriException;
import jmri.jmrit.ctcserialdata.CTCSerialData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTCFileModel extends AbstractStartupModel {

    private final static Logger log = LoggerFactory.getLogger(CTCFileModel.class);
    private final static CTCMain _mCTCMain = new CTCMain();

    public String getFileName() { return this.getName(); }
    public void setFileName(String n) { this.setName(n); }

    @Override
    public void performAction() throws JmriException {
        CTCJythonAccessInstanceManager._mCTCMain = _mCTCMain;   // Give Jython access to our internal object
        String filename = this.getFileName();
        log.info("CTC " + CTCSerialData.CTCVersion + " Loading file {} ", filename);
        _mCTCMain.readDataFromXMLFile(filename);
    }
}
