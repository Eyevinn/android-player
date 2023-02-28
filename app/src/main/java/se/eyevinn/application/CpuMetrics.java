package se.eyevinn.application;

public class CpuMetrics {
    long lastCurrTime;
    float cpuTimeSec;
    int utime;
    int stime;
    int cutime;
    int cstime;

    public CpuMetrics() {
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

    public void setCpuTimeSec(float cpuTimeSec) {
        this.cpuTimeSec = cpuTimeSec;
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
