package org.myorg.mycep;

public class BikeTripEvent {
    public int bikeId;
    public float startStation;
    public float endStation;
    public long startTime;
    public long endTime;

    public BikeTripEvent(int bikeId, float startStation, float endStation,
                    long startTime, long endTime) {
        this.bikeId = bikeId;
        this.startStation = startStation;
        this.endStation = endStation;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("BikeTripEvent [bikeId=%d, startStation=%f, endStation=%f, startTime=%.1f, endTime=%.1f]",
                bikeId, startStation, endStation, startTime, endTime);
    }
}
