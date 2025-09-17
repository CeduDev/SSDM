import sys
import os
import json
from datetime import datetime

# sys.path.append(os.path.join(os.path.dirname(__file__), "../.."))
# sys.path.append(os.path.join(os.path.dirname(__file__), "../..", "OpenCEP"))
dataset_dir = os.path.join(os.path.dirname(__file__), "..", "datasets", "2025-05-data")

from CEP import CEP
from base.Event import Event
from stream.Stream import Stream
from pattern import bike_hot_path_pattern, simple_bike_trip_pattern
from file_reader import csv_to_stream
from base.DataFormatter import DataFormatter, EventTypeClassifier


class MyEventTypeClassifier(EventTypeClassifier):
    def get_event_type(self, event_payload: dict):
        return "BikeTrip"


class MyDataFormatter(DataFormatter):
    def __init__(self):
        super().__init__(event_type_classifier=MyEventTypeClassifier())

    def parse_event(self, raw_data: str):
        return json.loads(raw_data)

    def get_event_timestamp(self, payload: dict):
        return datetime.fromisoformat(payload["started_at"])

    def get_probability(self, payload: dict):
        return None


pattern = bike_hot_path_pattern()

# Create streams
in_stream = Stream()
out_stream = Stream()


file_path = os.path.join(dataset_dir, "test_data.csv")
csv_to_stream(file_path, in_stream)
in_stream.close()

formatter = MyDataFormatter()

cep = CEP(patterns=[pattern])
cep.run(in_stream, out_stream, formatter)

print(out_stream.count())
print(out_stream.first())
print(out_stream.last())
