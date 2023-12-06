# Event Aggregator App

Event Aggregator App is a command-line utility written in Java that reads a list of user events from a JSON file, aggregates the events, and writes daily summary reports for every user back to another JSON file.

## Usage

To use the Event Aggregator App, follow these steps:

1. **Compile the Code:**

    $javac -cp .:json-20210307.jar EventAggregatorApp.java

2. **Run the App:**

    2.1  for a fresh input file:

         $java -cp .:json-20210307.jar EventAggregatorApp -i input.json -o output.json
   
   2.2 if same input file is updated with new events:
   
        
       $java -cp .:json-20210307.jar EventAggregatorApp -i input.json -o output.json --update
       

    Replace `input.json` with the path to your input file and `output.json` with the desired output file path.

4. **Options:**

    - `-i` or `--input`: Path to the input JSON file.
    - `-o` or `--output`: Path to the output JSON file.
    - `--update`: Use this option to support real-time aggregation. It updates the corresponding daily summary report without reprocessing all previous events.

## Example

If your input.json file contains:

```json
[
  {"userId": 1, "eventType": "post", "timestamp": 1672444800},
  {"userId": 1, "eventType": "likeReceived", "timestamp": 1672444801},
  // ...
]

