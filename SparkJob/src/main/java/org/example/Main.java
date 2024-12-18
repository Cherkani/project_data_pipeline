package org.example;

import java.time.Instant;
import java.time.ZoneId;

public class Main {
    public static void main(String[] args) {
        // Example timestamp in epoch milliseconds
        long exampleEpochMilli = 1710442800000L; // Example: "2024-03-14T12:00:00Z"
        String zab = java.time.Instant.ofEpochMilli(exampleEpochMilli)
                .atZone(java.time.ZoneId.of("UTC"))
                .toString();
        // Print the formatted date
        System.out.println("Formatted Timestamp: " + zab);
    }
}