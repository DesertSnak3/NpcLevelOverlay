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
    private static final Map<Integer, String> npcElementalWeaknessData = new HashMap<>();
    
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
                    if (fields.length < 29) { // Need at least 29 columns for all data
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
                        
                        // Extract elemental weakness (column 28)
                        String elementalWeakness = null;
                        if (fields.length > 28) {
                            String elemField = fields[28];
                            if (elemField != null && !elemField.trim().isEmpty()) {
                                elementalWeakness = elemField.trim().toLowerCase();
                                npcElementalWeaknessData.put(npcId, elementalWeakness);
                            }
                        }
                        
                        // Extract weakness data from attack_type and defense stats
                        String attackType = fields[6]; // Column 6: attack_type
                        
                        // Get defense stats (columns 23-27)
                        int defStab = parseDefense(fields[23]);
                        int defSlash = parseDefense(fields[24]);
                        int defCrush = parseDefense(fields[25]);
                        int defMagic = parseDefense(fields[26]);
                        int defRangedLight = parseDefense(fields[27]);
                        int defRangedStandard = parseDefense(fields[28]);
                        int defRangedHeavy = parseDefense(fields[29]);
                        
                        String weakness = determineWeakness(attackType, defStab, defSlash, defCrush, defMagic, defRangedLight, defRangedStandard, defRangedHeavy, elementalWeakness);
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
    private static String determineWeakness(String attackType, int defStab, int defSlash, int defCrush, int defMagic, int defRangedLight, int defRangedStandard, int defRangedHeavy, String elementalWeakness) {
        // First check if there's a specific elemental weakness
        if (elementalWeakness != null && !elementalWeakness.isEmpty()) {
            // Valid elemental weaknesses
            // 1 = Air, 2 = Water, 3 = Earth, 4 = Fire
            if (elementalWeakness.equals("4") || elementalWeakness.equals("2") ||
                elementalWeakness.equals("3") || elementalWeakness.equals("1")) {

                // change elemental weakness to corresponding type name
                switch (elementalWeakness){
                    case "1":
                        elementalWeakness = "Air";
                        break;
                    case "2":
                        elementalWeakness = "Water";
                        break;
                    case "3":
                        elementalWeakness = "Earth";
                        break;
                    case "4":
                        elementalWeakness = "Fire";
                }

                return elementalWeakness;
            }
        }
        
        // Find the lowest defense stat to determine weakness
        int minDef = Math.min(Math.min(Math.min(Math.min(defStab, defSlash), defCrush),defMagic), Math.min(defRangedLight,Math.min(defRangedStandard,defRangedHeavy)));
        
        // If magic defense is the lowest, check if it's significantly lower
        if (defMagic == minDef && defMagic < defStab - 10 && defMagic < defSlash - 10 && defMagic < defCrush - 10) {
            // Return general magic if no specific elemental weakness was provided
            return "magic";
        }
        
        // Check if the monster uses magic attacks but no specific elemental weakness
        if (attackType != null && attackType.toLowerCase().contains("magic") && (elementalWeakness == null || elementalWeakness.isEmpty())) {
            // Default to general magic for magic users without specific elemental weakness
            return "magic";
        }
        
        // If ranged defense is the lowest
        if(defRangedStandard < minDef - 5 || defRangedLight < minDef -5 || defRangedHeavy < minDef - 5) {
            // what range is lowest
            if (defRangedStandard <= defRangedLight && defRangedStandard <= defRangedHeavy) {
                return "ranged";
            }
            else if (defRangedLight <= defRangedHeavy) {
                return "lightranged";
            }
            else {
                return "heavyranged";
            }
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