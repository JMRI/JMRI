package jmri.jmrit.logix;

/**
 * This class holds speed data for a block of a warrant's route. The data is
 * gathered when a warrant is about to be executed and uses speed information
 * of the locomotive running the warrant.
 *  
 * @author Pete Cressman Copyright (C) 2019
*/

class BlockSpeedInfo {
    String blockName;
    float entranceSpeed;
    float maxSpeed;
    float exitSpeed;
    long time;
    float blkDist;
    int firstIdx;
    int lastIdx;

    BlockSpeedInfo(String n, float ens, float ms, float exs, long t, float d, int fi, int li) {
        blockName = n;
        entranceSpeed = ens;
        maxSpeed = ms;
        exitSpeed = exs;
        time = t;
        blkDist = d;
        firstIdx = fi;
        lastIdx = li;
    }

    String getBlockDisplayName() {
        return blockName;
    }
    // Throttle setting at entrance of block
    float getEntranceSpeed() {
        return entranceSpeed;
    }

    // Maximum throttle setting in block
    float getMaxSpeed() {
        return maxSpeed;
    }

    // Throttle setting at exit of block
    float getExitSpeed() {
        return exitSpeed;
    }

    long getTimeInBlock() {
        return time;
    }

    float getDistance() {
        return blkDist;
    }
    int getFirstIndex() {
        return firstIdx;
    }

    int getLastIndex() {
        return lastIdx;
    }
}
