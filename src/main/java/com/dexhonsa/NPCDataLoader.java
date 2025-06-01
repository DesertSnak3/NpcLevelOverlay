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
    
    private static final String CSV_FILE = "/monsters-complete.csv";
    private static final Map<Integer, Integer> npcMaxHitData = new HashMap<>();
    private static final Map<Integer, String> npcWeaknessData = new HashMap<>();
    private static final Map<Integer, Boolean> npcAggressiveData = new HashMap<>();
    
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
                boolean firstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    // Skip header line
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    
                    String[] fields = parseCSVLine(line);
                    if (fields.length < 27) { // Need at least 27 columns for all data
                        continue;
                    }
                    
                    try {
                        // Extract NPC ID (column 0)
                        int npcId = Integer.parseInt(fields[0].trim());
                        
                        // Extract max hit (column 5)
                        String maxHitField = fields[5];
                        if (maxHitField != null && !maxHitField.isEmpty() && !maxHitField.equals("?")) {
                            try {
                                int maxHit = Integer.parseInt(maxHitField.trim());
                                if (maxHit > 0) {
                                    npcMaxHitData.put(npcId, maxHit);
                                }
                            } catch (NumberFormatException e) {
                                // Skip invalid max hit values
                            }
                        }
                        
                        // Extract aggressive boolean (column 7)
                        if (fields.length > 7) {
                            String aggressiveField = fields[7];
                            // Only set aggressive to true if the field explicitly contains "true"
                            // Empty fields or any other value defaults to false
                            if (aggressiveField != null && aggressiveField.trim().equalsIgnoreCase("true")) {
                                npcAggressiveData.put(npcId, true);
                            } else {
                                npcAggressiveData.put(npcId, false);
                            }
                        }
                        
                        // Extract weakness data from attack_type and defense stats
                        String attackType = fields[6]; // Column 6: attack_type
                        
                        // Get defense stats (columns 23-27)
                        int defStab = parseDefense(fields[23]);
                        int defSlash = parseDefense(fields[24]);
                        int defCrush = parseDefense(fields[25]);
                        int defMagic = parseDefense(fields[26]);
                        int defRanged = parseDefense(fields[27]);
                        
                        String weakness = determineWeakness(attackType, defStab, defSlash, defCrush, defMagic, defRanged);
                        npcWeaknessData.put(npcId, weakness);
                        
                        lineCount++;
                    } catch (NumberFormatException e) {
                        // Skip rows with invalid IDs
                    }
                }
                log.info("Loaded {} NPC entries with max-hit, weakness, and aggressive data from CSV", lineCount);
            }
        } catch (IOException e) {
            log.error("Failed to read monster data", e);
        }
    }
    
    /**
     * Parse defense value, handling empty or invalid values
     */
    private static int parseDefense(String defStr) {
        if (defStr == null || defStr.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(defStr.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Determine the primary weakness based on defense stats and attack type
     */
    private static String determineWeakness(String attackType, int defStab, int defSlash, int defCrush, int defMagic, int defRanged) {
        // First check if the monster uses magic attacks - these often have elemental weaknesses
        if (attackType != null && attackType.toLowerCase().contains("magic")) {
            // Check if it has particularly low defense against a specific element
            // For now, default to fire rune for magic users
            // In the future, this could be enhanced with more specific elemental data
            return "fire";
        }
        
        // Find the lowest defense stat to determine weakness
        int minDef = Math.min(Math.min(Math.min(defStab, defSlash), defCrush), Math.min(defMagic, defRanged));
        
        // If magic defense is the lowest, check if it's significantly lower
        if (defMagic == minDef && defMagic < defStab - 10 && defMagic < defSlash - 10 && defMagic < defCrush - 10) {
            return "magic";
        }
        
        // If ranged defense is the lowest
        if (defRanged == minDef && defRanged < defStab - 5 && defRanged < defSlash - 5 && defRanged < defCrush - 5) {
            return "ranged";
        }
        
        // Otherwise, determine melee weakness
        if (defStab == minDef) {
            return "stab";
        } else if (defSlash == minDef) {
            return "slash";
        } else if (defCrush == minDef) {
            return "crush";
        }
        
        // Default to slash if no clear weakness
        return "slash";
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
    
    /**
     * Check if a specific NPC is aggressive
     */
    public static boolean isAggressive(int npcId) {
        return npcAggressiveData.getOrDefault(npcId, false);
    }
} 