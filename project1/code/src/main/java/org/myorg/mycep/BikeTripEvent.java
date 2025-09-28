package org.myorg.mycep;

public class BikeTripEvent {
    public String rideId;
    public String bikeType;
    public float startStation;
    public float endStation;
    public long startTime;
    public long endTime;

    public BikeTripEvent(String rideId, float startStation, float endStation,
                    long startTime, long endTime) {
        this.rideId = rideId;
        this.startStation = startStation;
        this.endStation = endStation;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("BikeTripEvent, where ID is %s, start station is %s, and end station %s", rideId, startStation, endStation);
    }
}
