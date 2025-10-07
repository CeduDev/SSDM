package org.myorg.mycep;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BikeHotPathJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Enable latency tracking every 1 sec, working? Idk
        env.getConfig().setLatencyTrackingInterval(1000);

        // Small test data
        // String dataFilePath = "../datasets/";
        // List<Float> endStations = Arrays.asList(7.0f, 8.0f, 9.0f);
        // DataStream<BikeTripEvent> trips = ReadCSVData.fromCsv(env, dataFilePath, "test_data.csv");

        // January 1 data
        String dataFilePath = "../datasets/2019-data/2019-citibike-tripdata/1_January/";
        List<Float> endStations = Arrays.asList(3283.0f, 518.0f, 3154.0f);
        DataStream<BikeTripEvent> trips = ReadCSVData.fromCsv(env, dataFilePath, "201901-citibike-tripdata_1.csv");

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

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                String res = "================\nHot path detected! Stations and times in order are\n";
                
                for (BikeTripEvent a: aTrips) {
                    String startTimeStr = LocalDateTime.ofEpochSecond(a.startTime / 1000, 0, ZoneOffset.UTC).format(formatter);
                    String endTimeStr = LocalDateTime.ofEpochSecond(a.endTime / 1000, 0, ZoneOffset.UTC).format(formatter);

                    res += String.format("%s -> %s, times: %s -> %s\n",
                        a.startStation,
                        a.endStation,
                        startTimeStr,
                        endTimeStr
                    );
                }

                res += String.format("%s -> %s, times: %s -> %s\n", b.startStation, b.endStation, LocalDateTime.ofEpochSecond(b.startTime / 1000, 0, ZoneOffset.UTC).format(formatter), LocalDateTime.ofEpochSecond(b.endTime / 1000, 0, ZoneOffset.UTC).format(formatter));
                res += "================";

                // Override resulting string for easier counting of patterns
                res = "HOT PATH";

                out.collect(res);
        }});

        // Define the sink
        FileSink<String> sink = FileSink
            .forRowFormat(
                new Path("./project1/output/"),      // directory where files are written
                new SimpleStringEncoder<String>("UTF-8")
            )
            .withRollingPolicy(OnCheckpointRollingPolicy.build()) // not sure what this is honestly :D
            .build();
        
        // Print to the sink defined above
        result.sinkTo(sink);

        // Print to stdout in Flink (not in your own terminal, see the README.md for more details)
        // result.print();

        env.execute("Bike Hot Path CEP Job");
    }
}
