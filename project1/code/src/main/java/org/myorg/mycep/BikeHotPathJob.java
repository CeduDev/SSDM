package org.myorg.mycep;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BikeHotPathJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        String dataFilePath = "../datasets/";
        String fileName = "test_data.csv";
        List<Float> endStations = Arrays.asList(7.0f, 8.0f, 9.0f);

        // Create data from the fileName
        DataStream<BikeTripEvent> trips = ReadCSVData.fromCsv(env, dataFilePath, fileName);

        // Use the pattern
        Pattern<BikeTripEvent, ?> pattern = BikeTripPattern.hotPathPattern(endStations);

        // Apply CEP
        PatternStream<BikeTripEvent> patternStream = CEP.pattern(trips, pattern);

        DataStream<String> result = patternStream.process(
            new PatternProcessFunction<BikeTripEvent, String>() {
            // We override the processMatch function to return a custom formatted string whenever a pattern is found 
            @Override
            public void processMatch(
                    Map<String, List<BikeTripEvent>> pattern,
                    Context ctx,
                    Collector<String> out) throws Exception {
                List<BikeTripEvent> aTrips = pattern.get("a");
                BikeTripEvent b = pattern.get("b").get(0);
                String res = "Hot path detected! Stations in order are: ";
                for (BikeTripEvent a: aTrips) {
                    res += a.startStation + " -> " + a.endStation + ", ";
                }
                res += b.endStation;

                out.collect(res);
        }});

        // Print to stdout in Flink (not in your own terminal, see the README.md for more details)
        result.print();

        env.execute("Bike Hot Path CEP Job");
    }
}
