package org.myorg.mycep;

import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;

import java.time.Duration;
import java.util.List;

public class BikeTripPattern {

    public static Pattern<BikeTripEvent, ?> hotPathPattern(List<Float> endStations) {
        AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.skipPastLastEvent();

        return Pattern.<BikeTripEvent>begin("a", skipStrategy)
            .where(new IterativeCondition<BikeTripEvent>() {
                @Override
                public boolean filter(BikeTripEvent current, Context<BikeTripEvent> ctx) throws Exception {
                    // First event always passes
                    if (!ctx.getEventsForPattern("a").iterator().hasNext()) {
                        return true;
                    }
                    // Ensure continuity: same bike, previous end = current start
                    for (BikeTripEvent prev : ctx.getEventsForPattern("a")) {
                        if (prev.bikeId == current.bikeId &&
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
                    BikeTripEvent firstA = ctx.getEventsForPattern("a").iterator().next();
                    BikeTripEvent lastA = null;
                    for (BikeTripEvent a : ctx.getEventsForPattern("a")) {
                        lastA = a;
                    }
                    if (lastA == null) return false;

                    boolean sameBike = lastA.bikeId == b.bikeId;
                    boolean endsInHotStation = endStations.contains(b.endStation);
                    long endToEndLatency = b.processingStart - firstA.processingStart;
                    return sameBike && endsInHotStation && endToEndLatency < 10;
                }
            })
            .within(Duration.ofMinutes(60));
    }
}
