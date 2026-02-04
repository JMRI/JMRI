package jmri.jmrit.logix;

import javax.annotation.Nonnull;

/**
 * This class holds speed data for a block of a warrant's route. The data is
 * gathered when a warrant is about to be executed and uses speed information
 * of the locomotive running the warrant.
 *  
 * @author Pete Cressman Copyright (C) 2019
*/

class BlockSpeedInfo {

    private final String blockName;
    private final float entranceSpeed;
    private final float exitSpeed;
    private final long time;
    private final float pathDist;
    private final float calcDist;
    private final int firstIdx;
    private final int lastIdx;

    BlockSpeedInfo( @Nonnull String n, float ens, float exs, long t, float d, float c, int fi, int li) {
        blockName = n;
        entranceSpeed = ens;
        exitSpeed = exs;
        time = t;
        pathDist = d;
        calcDist = c;
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

    // Throttle setting at exit of block
    float getExitSpeed() {
        return exitSpeed;
    }

    long getTimeInBlock() {
        return time;
    }

    // Path length in block
    float getPathLen() {
        return pathDist;
    }

    // Calculated path length in block according to speedProfile
    float getCalcLen() {
        return calcDist;
    }
    int getFirstIndex() {
        return firstIdx;
    }

    int getLastIndex() {
        return lastIdx;
    }

    @Override
    @Nonnull
    public String toString() {
        StringBuilder sb = new StringBuilder(" \"");
        sb.append(blockName);
        sb.append("\" entranceSpeed ");
        sb.append(entranceSpeed);
        sb.append(", exitSpeed ");
        sb.append(exitSpeed);
        sb.append(", time ");
        sb.append(time);
        sb.append(", pathDist ");
        sb.append(pathDist);
        sb.append(", calcDist ");
        sb.append(calcDist);
        sb.append(", from ");
        sb.append(firstIdx);
        sb.append(" to ");
        sb.append(lastIdx);
        return sb.toString();
    }
}
