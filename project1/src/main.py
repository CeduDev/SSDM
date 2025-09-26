import os
import json
from datetime import datetime

dataset_dir = os.path.join(os.path.dirname(__file__), "..", "datasets", "2025-05-data")

from CEP import CEP
from stream.Stream import Stream
from pattern import bike_hot_path_pattern
from file_reader import csv_to_stream
from base.DataFormatter import DataFormatter, EventTypeClassifier


class BikeTripEventTypeClassifier(EventTypeClassifier):
    def get_event_type(self, event_payload: dict):
        return "BikeTrip"


class BikeTripDataFormatter(DataFormatter):
    def __init__(self):
        super().__init__(event_type_classifier=BikeTripEventTypeClassifier())

    def parse_event(self, raw_data: str):
        return json.loads(raw_data)

    # Use the "started_at"-column as the event timestamp
    def get_event_timestamp(self, payload: dict):
        return datetime.fromisoformat(payload["started_at"])

    def get_probability(self, payload: dict):
        return None


# filename_endStations_rowsToTake = ["test_data.csv", [7.0, 8.0, 9.0], None]
filename_endStations_rowsToTake = ["202505-citibike-tripdata_1.csv", [5569.06], 40]
print(
    "Using filename "
    + filename_endStations_rowsToTake[0]
    + " with end stations: "
    + ", ".join(map(str, filename_endStations_rowsToTake[1]))
)

pattern = bike_hot_path_pattern(filename_endStations_rowsToTake[1])

in_stream = Stream()
out_stream = Stream()

file_path = os.path.join(dataset_dir, filename_endStations_rowsToTake[0])

print("Reading input file: " + filename_endStations_rowsToTake[0])
csv_to_stream(file_path, in_stream, filename_endStations_rowsToTake[2])
in_stream.close()
print("File read, closing stream")

formatter = BikeTripDataFormatter()

print("Start CEP engine and start the run")
cep = CEP(patterns=[pattern])
total_time = cep.run(in_stream, out_stream, formatter)
print("Engine done running, ran for " + str(total_time) + " seconds")

for match in out_stream:
    print(match)
