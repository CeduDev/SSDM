import csv
import json
from datetime import datetime

def csv_to_stream(filepath, in_stream):
    """Stream CSV rows directly to input stream"""
    with open(filepath, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            # Convert to JSON string and add to stream
            if row["ride_id"] and row["start_station_id"] and row["end_station_id"] and row["started_at"]:
                in_stream.add_item(json.dumps({
                    "bike_id": row["ride_id"],
                    "start_station": row["start_station_id"],
                    "end_station": row["end_station_id"],
                    "started_at": row["started_at"]
                }))
