package com.dexhonsa;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NPCDataLoader {
    
    private static final String CSV_FILE = "/monsterdata.csv";
    private static final Map<Integer, Integer> npcMaxHitData = new HashMap<>();
    private static final Map<Integer, String> npcWeaknessData = new HashMap<>();
    
    static {
        loadData();
    }
    
    private static void loadData() {
        try (InputStream inputStream = NPCDataLoader.class.getResourceAsStream(CSV_FILE)) {
            if (inputStream == null) {
                log.warn("Could not find file: {}", CSV_FILE);
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    String[] fields = parseCSVLine(line);
                    if (fields.length < 13) { // Need at least 13 columns for weakness data
                        continue;
                    }
                    
                    // Extract NPC IDs (column 10, 0-indexed)
                    String npcIdsField = fields[10];
                    if (npcIdsField == null || npcIdsField.isEmpty() || npcIdsField.equals("?")) {
                        continue;
                    }
                    
                    // Extract max hit (column 11, 0-indexed)
                    String maxHitField = fields[11];
                    Integer maxHit = null;
                    if (maxHitField != null && !maxHitField.isEmpty() && !maxHitField.equals("?")) {
                        maxHit = parseMaxHit(maxHitField);
                    }
                    
                    // Extract weakness data
                    String elementalWeakness = fields[1]; // Column 1: elemental weakness
                    String primaryWeakness = fields[12];  // Column 12: primary weakness
                    
                    String weakness = determineWeakness(elementalWeakness, primaryWeakness);
                    
                    // Parse NPC IDs and add to maps
                    String[] npcIds = npcIdsField.split(",");
                    for (String idStr : npcIds) {
                        try {
                            int id = Integer.parseInt(idStr.trim());
                            if (maxHit != null && maxHit > 0) {
                                npcMaxHitData.put(id, maxHit);
                            }
                            // Always add weakness since determineWeakness now always returns a value
                            npcWeaknessData.put(id, weakness);
                            lineCount++;
                        } catch (NumberFormatException e) {
                            // Skip invalid IDs
                        }
                    }
                }
                log.info("Loaded {} NPC entries with max-hit and weakness data from CSV", lineCount);
            }
        } catch (IOException e) {
            log.error("Failed to read monster data", e);
        }
    }
    
    /**
     * Determine the primary weakness from elemental and combat weaknesses
     */
    private static String determineWeakness(String elemental, String combat) {
        // Prioritize elemental weakness if present
        if (elemental != null && !elemental.trim().isEmpty()) {
            String elem = elemental.trim().toLowerCase();
            if (elem.equals("fire")) return "fire";
            if (elem.equals("water")) return "water";
            if (elem.equals("earth")) return "earth";
            if (elem.equals("air")) return "air";
        }
        
        // Otherwise use combat weakness
        if (combat != null && !combat.trim().isEmpty() && !combat.equals("?")) {
            // Extract the first weakness if multiple are listed
            String[] weaknesses = combat.split(",");
            if (weaknesses.length > 0) {
                String primary = weaknesses[0].trim().toLowerCase();
                // Remove any parenthetical content
                int parenIndex = primary.indexOf('(');
                if (parenIndex > 0) {
                    primary = primary.substring(0, parenIndex).trim();
                }
                
                // Map to our icon types
                if (primary.contains("stab")) return "stab";
                if (primary.contains("slash")) return "slash";
                if (primary.contains("crush")) return "crush";
                if (primary.contains("magic")) return "magic";
                if (primary.contains("ranged")) return "ranged";
            }
        }
        
        // Default to slash if no specific weakness found
        return "slash";
    }
    
    /**
     * Parse max hit from the CSV field. Handles various formats like:
     * - Simple numbers: "10"
     * - Multiple hits: "10 (melee),20 (magic)"
     * - Range expressions: "10x2"
     * @return the highest max hit value found, or null if parsing fails
     */
    private static Integer parseMaxHit(String maxHitStr) {
        if (maxHitStr == null || maxHitStr.isEmpty()) {
            return null;
        }
        
        // Remove HTML tags if present
        maxHitStr = maxHitStr.replaceAll("<[^>]+>", " ");
        
        int maxValue = -1;
        
        // Split by common separators
        String[] parts = maxHitStr.split("[,;]");
        
        for (String part : parts) {
            // Extract numbers from each part
            String cleanPart = part.trim();
            
            // Handle multiplication expressions like "20x3"
            if (cleanPart.contains("x")) {
                String[] multParts = cleanPart.split("x");
                try {
                    int base = extractFirstNumber(multParts[0]);
                    int multiplier = 1;
                    if (multParts.length > 1) {
                        multiplier = extractFirstNumber(multParts[1]);
                    }
                    if (base > 0 && multiplier > 0) {
                        maxValue = Math.max(maxValue, base * multiplier);
                    }
                } catch (Exception e) {
                    // Skip invalid expressions
                }
            } else {
                // Extract first number from the part
                int value = extractFirstNumber(cleanPart);
                if (value > 0) {
                    maxValue = Math.max(maxValue, value);
                }
            }
        }
        
        return maxValue > 0 ? maxValue : null;
    }
    
    /**
     * Extract the first number from a string
     */
    private static int extractFirstNumber(String str) {
        if (str == null) return -1;
        
        // Remove parentheses content and special characters
        str = str.replaceAll("\\([^)]*\\)", "").trim();
        
        // Extract digits
        StringBuilder number = new StringBuilder();
        boolean foundDigit = false;
        
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
                foundDigit = true;
            } else if (foundDigit) {
                // Stop at first non-digit after finding digits
                break;
            }
        }
        
        try {
            return number.length() > 0 ? Integer.parseInt(number.toString()) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Parse CSV line handling quoted fields
     */
    private static String[] parseCSVLine(String line) {
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        char[] chars = line.toCharArray();
        
        for (char ch : chars) {
            if (inQuotes) {
                startCollectChar = true;
                if (ch == '\"') {
                    inQuotes = false;
                } else {
                    sb.append(ch);
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true;
                    if (startCollectChar) {
                        sb.append('\"');
                    }
                } else if (ch == ',') {
                    sb.append('|'); // Use a different delimiter to split later
                } else {
                    sb.append(ch);
                }
            }
        }
        
        return sb.toString().split("\\|");
    }
    
    /**
     * Get max hit for a specific NPC ID
     */
    public static Integer getMaxHit(int npcId) {
        return npcMaxHitData.get(npcId);
    }
    
    /**
     * Get all max hit data
     */
    public static Map<Integer, Integer> getAllMaxHitData() {
        return new HashMap<>(npcMaxHitData);
    }
    
    /**
     * Get weakness for a specific NPC ID
     */
    public static String getWeakness(int npcId) {
        return npcWeaknessData.get(npcId);
    }
} 