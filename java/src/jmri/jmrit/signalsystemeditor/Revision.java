package jmri.jmrit.signalsystemeditor;

/**
 * A revision
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class Revision {

    private String _revNumber;
    private String _date;
    private String _authorInitials;
    private String _remark;

    public Revision(String revNumber, String date, String authorInitials, String remark) {
        this._revNumber = revNumber;
        this._date = date;
        this._authorInitials = authorInitials;
        this._remark = remark;
    }

    public void setRevNumber(String revNumber) {
        this._revNumber = revNumber;
    }

    public String getRevNumber() {
        return this._revNumber;
    }

    public void setDate(String date) {
        this._date = date;
    }

    public String getDate() {
        return this._date;
    }

    public void setAuthorInitials(String authorInitials) {
        this._authorInitials = authorInitials;
    }

    public String getAuthorInitials() {
        return this._authorInitials;
    }

    public void setRemark(String remark) {
        this._remark = remark;
    }

    public String getRemark() {
        return this._remark;
    }

}
