package jmri.jmrit.logix;


/**
 * A TrainOrder has information about a required speed change of
 * the warrant. This class is returned when a BlockOrder attempts
 * to set the warrant's path through its block.
 *
 * @author Pete Cressman Copyright (C) 2022
 */
 class TrainOrder {

    public enum Cause {
        NONE("None"),
        WARRANT("Warrant"),
        OCCUPY("Occupancy"),
        SIGNAL("Signal"),
        ERROR("Error");

        String _bundleKey;

        Cause(String bundleName) {
            _bundleKey = bundleName;
        }

        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }

    protected String _speedType;    // speedTyps name of speed change ahead.
    protected Cause _cause;         // case of speed change
    protected int _idxContrlBlock;  // index of BlockOrder whose condition is cause of speed change
    protected int _idxEnterBlock;   // index of BlockOrder where entry requires speed change
    protected String _message;      // message why speed change is needed

    protected TrainOrder(String speedType, Cause cause, int idxControl, int idxEnter, String msg) {
        _speedType = speedType;
        _cause = cause;
        _idxContrlBlock = idxControl;
        _idxEnterBlock = idxEnter;
        _message = msg;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TrainOrder: speedType \"");
        sb.append(_speedType);
        sb.append("\", cause \"");
        sb.append(_cause.toString());
        sb.append("\", idxContrlBlock= ");
        sb.append(_idxContrlBlock);
        sb.append(", idxEnterBlock= ");
        sb.append(_idxEnterBlock);
        sb.append(", _message ");
        sb.append(_message);
        return sb.toString();
    }
}
