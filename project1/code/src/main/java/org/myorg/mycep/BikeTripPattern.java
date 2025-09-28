package org.myorg.mycep;

import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;

import java.time.Duration;
import java.util.List;

public class BikeTripPattern {

    public static Pattern<BikeTripEvent, ?> hotPathPattern(List<Float> endStations) {
        return Pattern.<BikeTripEvent>begin("a")
            .where(new IterativeCondition<BikeTripEvent>() {
                @Override
                public boolean filter(BikeTripEvent current, Context<BikeTripEvent> ctx) throws Exception {
                    // First event always passes
                    if (!ctx.getEventsForPattern("a").iterator().hasNext()) {
                        return true;
                    }
                    // Ensure continuity: same bike, previous end = current start
                    for (BikeTripEvent prev : ctx.getEventsForPattern("a")) {
                        if (prev.rideId.equals(current.rideId) &&
                        prev.endStation == current.startStation) {
                            return true;
                        }
                    }
                    return false;
                }
            })
            .oneOrMore()   // Kleene +
            .next("b")
            .where(new IterativeCondition<BikeTripEvent>() {
                @Override
                public boolean filter(BikeTripEvent b, Context<BikeTripEvent> ctx) throws Exception {
                    // Ensure same bike as last "a" event
                    BikeTripEvent lastA = null;
                    for (BikeTripEvent a : ctx.getEventsForPattern("a")) {
                        lastA = a;
                    }
                    if (lastA == null) return false;

                    boolean sameBike = lastA.rideId.equals(b.rideId);
                    boolean endsInHotStation = b.endStation == 7.0 ||
                                              b.endStation == 8.0 ||
                                              b.endStation == 9.0;
                    return sameBike && endsInHotStation;
                }
            })
            .within(Duration.ofMinutes(60));
    }
}
