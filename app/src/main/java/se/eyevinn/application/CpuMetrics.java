package se.eyevinn.application;

import android.system.Os;
import android.system.OsConstants;

public class CpuMetrics {
    long numCores;
    long clockSpeedHz;
    long lastCurrTime;
    float cpuTimeSec;
    int utime;
    int stime;
    int cutime;
    int cstime;

    public CpuMetrics() {
        numCores = Os.sysconf(OsConstants._SC_NPROCESSORS_CONF);
        clockSpeedHz = Os.sysconf(OsConstants._SC_CLK_TCK);
        lastCurrTime = System.currentTimeMillis();
        cpuTimeSec = 0.00F;
        utime = 0;
        stime = 0;
        cutime = 0;
        cstime = 0;
    }

    public void updateCpuMetrics(long currTime, float cpuTime) {
        this.lastCurrTime = currTime;
        this.cpuTimeSec = cpuTime;
    }

    public void updateStatMetrics(String[] splitStatResult) {
        this.utime = Integer.parseInt(splitStatResult[14]);
        this.stime = Integer.parseInt(splitStatResult[15]);
        this.cutime = Integer.parseInt(splitStatResult[16]);
        this.cstime = Integer.parseInt(splitStatResult[17]);
    }

    public float calcCpuTime(String[] stat) {
        int dUTime = Integer.parseInt(stat[14]) - this.getUtime();
        int dSTime = Integer.parseInt(stat[15]) - this.getStime();
        int dCuTime = Integer.parseInt(stat[16]) - this.getCutime();
        int dCsTime = Integer.parseInt(stat[17]) - this.getCstime();

        return (float)(dUTime + dSTime + dCuTime + dCsTime) / this.clockSpeedHz;
    }

    public float calcCurrCpuUsage(long currTime) {
        return (float)((100 * (this.cpuTimeSec - this.getCpuTimeSec() / (currTime - this.getStartTime()))) / this.numCores);
    }

    // Getters and Setters

    public long getStartTime() {
        return lastCurrTime;
    }

    public void setStartTime(long startTime) {
        this.lastCurrTime = startTime;
    }

    public float getCpuTimeSec() {
        return cpuTimeSec;
    }

    public int getUtime() {
        return utime;
    }

    public int getStime() {
        return stime;
    }

    public int getCutime() {
        return cutime;
    }

    public int getCstime() {
        return cstime;
    }

}
