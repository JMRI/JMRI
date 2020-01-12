package jmri.jmrit.logixng.digital.expressions;

import java.util.TimerTask;

/**
 *
 */
abstract class MyTimerTask extends TimerTask {

    private final Object _lock;
    private boolean _timerIsRunning = false;
    private boolean _stopTimer = false;
    
    public MyTimerTask() {
        _lock = new Object();
    }
    
    public abstract void execute();
    
    @Override
    public void run() {
        synchronized(_lock) {
            if (_stopTimer) return;
            _timerIsRunning = true;
        }
        
        execute();
        
        synchronized(_lock) {
            _timerIsRunning = false;
        }
    }
    
    /**
     * Stop the timer.
     * This method will not return until the timer task is cancelled and stopped.
     * I had some concurrency errors in about 1 of 20 times of running TimerTest.
     * The call _timerTask.cancel() return even if the task is still running,
     * so we are not guaranteed that after the call to _timerTask.cancel(),
     * the _timerTask is completed.
     * This code ensures that we don't return from this method until _timerTask
     * is cancelled and that it's not running any more. / Daniel Bergqvist
     */
    @SuppressWarnings(value = "SleepWhileInLoop")
    void stopTimer() {
        int count = 1;
        synchronized (_lock) {
            _stopTimer = true;
            if (cancel()) return;
            // If the timer task is not running, we don't have
            // to wait for it to finish.
            if (!_timerIsRunning) {
                return;
            }
        }
        // Try max 50 times
        while (count <= 50) {
            synchronized (_lock) {
                if (!_timerIsRunning) {
                    return;
                }
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            count++;
        }
        throw new RuntimeException("Cannot stop timer");
    }

}
