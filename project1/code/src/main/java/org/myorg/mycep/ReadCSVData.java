package org.myorg.mycep;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ReadCSVData {

    public static DataStream<BikeTripEvent> fromCsv(
            StreamExecutionEnvironment env,
            String basePath,
            String fileName) {

        // Create file source
        final FileSource<String> source = FileSource
            .forRecordStreamFormat(new TextLineInputFormat(),
                Path.fromLocalFile(new File(basePath, fileName)))
            .build();

        // Read file into DataStream
        DataStream<String> lines = env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "bike-trip-file-source"
        );

        // Parse lines into BikeTripEvent objects
        DataStream<BikeTripEvent> events = lines
            .filter(line -> !line.startsWith("tripduration")) // skip header
            .map(line -> {
                String[] parts = line.split(",");
                final DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS");

                int bikeId = Integer.parseInt(parts[11]);
                long startTime = LocalDateTime.parse(parts[1], formatter)
                    .toInstant(ZoneOffset.UTC).toEpochMilli();
                long endTime = LocalDateTime.parse(parts[2], formatter)
                    .toInstant(ZoneOffset.UTC).toEpochMilli();
                float startStation = Float.parseFloat(parts[3]);
                float endStation = Float.parseFloat(parts[7]);

                return new BikeTripEvent(bikeId, startStation, endStation, startTime, endTime);
            })
            .assignTimestampsAndWatermarks(
                WatermarkStrategy.<BikeTripEvent>forMonotonousTimestamps()
                    .withTimestampAssigner((event, ts) -> event.startTime)
            );

        return events.keyBy(new KeySelector<BikeTripEvent, Integer>() {
            @Override
            public Integer getKey(BikeTripEvent value) throws Exception {
                return value.bikeId;
            }
        });
    }
}
