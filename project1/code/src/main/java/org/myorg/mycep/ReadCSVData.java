package org.myorg.mycep;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
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
        return lines
            .filter(line -> !line.startsWith("ride_id")) // skip header
            .map(line -> {
                String[] parts = line.split(",");
                final DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

                long start_time = LocalDateTime.parse(parts[2], formatter)
                        .toEpochSecond(ZoneOffset.UTC);
                long end_time = LocalDateTime.parse(parts[3], formatter)
                        .toEpochSecond(ZoneOffset.UTC);

                return new BikeTripEvent(parts[0], Float.parseFloat(parts[5]), Float.parseFloat(parts[7]), start_time, end_time);
            })
            .assignTimestampsAndWatermarks(
                WatermarkStrategy.<BikeTripEvent>forMonotonousTimestamps()
                    .withTimestampAssigner((event, ts) -> event.startTime * 1000)
            );
    }
}
