package jmri;

public interface ConfiguringSystemConnectionMemo extends SystemConnectionMemo {

    /**
     * Configure the common managers associated with the System Connection memo.
     */
    void configureManagers();

}
