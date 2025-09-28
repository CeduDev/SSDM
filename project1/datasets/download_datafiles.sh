ZIP_FILE="2025-05-data.zip"
EXTRACT_DIR="2025-05-data"
URL="https://s3.amazonaws.com/tripdata/202505-citibike-tripdata.zip"

extract_zip() {
  echo "Extracting $ZIP_FILE into $EXTRACT_DIR..."
  mkdir -p "$EXTRACT_DIR"
  unzip -o "$ZIP_FILE" -d "$EXTRACT_DIR"
}

if [ -f "$ZIP_FILE" ]; then
  echo "$ZIP_FILE already exists."
  if [ ! -d "$EXTRACT_DIR" ] || [ -z "$(ls -A "$EXTRACT_DIR")" ]; then
    extract_zip
  else
    echo "Files already extracted in $EXTRACT_DIR. Skipping extraction."
  fi
else
  echo "Downloading $ZIP_FILE..."
  curl -L "$URL" --output "$ZIP_FILE"
  extract_zip
fi