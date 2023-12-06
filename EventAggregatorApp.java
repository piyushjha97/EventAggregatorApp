import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventAggregatorApp {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        // Parse command-line options
        String inputFile = null;
        String outputFile = null;
        boolean updateMode = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-i":
                    if (i + 1 < args.length) {
                        inputFile = args[i + 1];
                        i++;
                    }
                    break;
                case "-o":
                    if (i + 1 < args.length) {
                        outputFile = args[i + 1];
                        i++;
                    }
                    break;
                    case "--update":
                    updateMode = true;
                    break;
                default:
                    break;
            }
        }

        // Check if input and output files are present in the command line
        if (inputFile == null || outputFile == null) {
            System.out.println("Usage: java EventAggregatorApp -i <inputFile> -o <outputFile>");
            System.exit(1);
        }

        try {
            JSONArray events = readJsonArrayFromFile(inputFile);
            JSONArray aggregatedData = new JSONArray();

            if (updateMode) {
                // Update existing aggregatedData with new events
                updateAggregatedData(events, aggregatedData);
            } else {
                // Perform initial aggregation
                aggregateEvents(events, aggregatedData);
            }

            writeJsonArrayToFile(aggregatedData, outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONArray readJsonArrayFromFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return new JSONArray(content.toString());
    }

    private static void aggregateEvents(JSONArray events, JSONArray aggregatedData) {
        Map<Integer, Map<String, JSONObject>> userEventSummaries = new HashMap<>();

        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            int userId = event.getInt("userId");
            String date = DATE_FORMAT.format(new Date(event.getLong("timestamp") * 1000L));

            Map<String, JSONObject> dailyEventSummaries = userEventSummaries.computeIfAbsent(userId, k -> new HashMap<>());

            JSONObject dailySummary = dailyEventSummaries.computeIfAbsent(date, k -> {
                JSONObject newSummary = new JSONObject();
                newSummary.put("date", date);
                newSummary.put("userId", userId);
                return newSummary;
            });

            String eventType = event.getString("eventType");
            dailySummary.put(eventType, dailySummary.optInt(eventType, 0) + 1);
        }

        // Add each JSONObject individually to the aggregatedData array
        for (Map<String, JSONObject> dailyEventSummaries : userEventSummaries.values()) {
            aggregatedData.putAll(dailyEventSummaries.values());
        }
    }

    private static void updateAggregatedData(JSONArray newEvents, JSONArray aggregatedData) {
        Map<Integer, Map<String, JSONObject>> userSummaries = new HashMap<>();
    
        for (int i = 0; i < newEvents.length(); i++) {
            JSONObject event = newEvents.getJSONObject(i);
            int userId = event.getInt("userId");
            String date = DATE_FORMAT.format(new Date(event.getLong("timestamp") * 1000L));
    
            Map<String, JSONObject> dailySummaries = userSummaries.computeIfAbsent(userId, k -> new HashMap<>());
    
            JSONObject dailySummary = dailySummaries.computeIfAbsent(date, k -> {
                int existingIndex = findUserDailySummaryIndex(aggregatedData, userId, date);
                if (existingIndex != -1) {
                    return aggregatedData.getJSONObject(existingIndex);
                } else {
                    JSONObject newSummary = new JSONObject();
                    newSummary.put("date", date);
                    newSummary.put("userId", userId);
                    return newSummary;
                }
            });
    
            String eventType = event.getString("eventType");
            dailySummary.put(eventType, dailySummary.optInt(eventType, 0) + 1);
        }
    
        // Update aggregatedData with updated daily summaries
        for (Map<String, JSONObject> dailySummaries : userSummaries.values()) {
            aggregatedData.putAll(dailySummaries.values());
        }
    }
    
    private static int findUserDailySummaryIndex(JSONArray aggregatedData, int userId, String date) {
        for (int i = 0; i < aggregatedData.length(); i++) {
            JSONObject summary = aggregatedData.getJSONObject(i);
            if (userId == summary.getInt("userId") && date.equals(summary.getString("date"))) {
                return i;
            }
        }
        return -1;
    }

    private static int findDailySummaryIndex(JSONArray aggregatedData, String date) {
        for (int i = 0; i < aggregatedData.length(); i++) {
            if (date.equals(aggregatedData.getJSONObject(i).getString("date"))) {
                return i;
            }
        }
        return -1;
    }

    private static void writeJsonArrayToFile(JSONArray jsonArray, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(jsonArray.toString(2));
        }
    }
}
