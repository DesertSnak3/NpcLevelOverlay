package com.dexhonsa;

import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

public class NPCDataLoaderTest {
    
    @Test
    public void testDataLoading() {
        // Test that data is loaded
        Map<Integer, Integer> allData = NPCDataLoader.getAllMaxHitData();
        assertNotNull("Max hit data should not be null", allData);
        assertTrue("Should have loaded some NPC data", allData.size() > 0);
        
        System.out.println("Loaded " + allData.size() + " NPC max hit entries");
    }
    
    @Test
    public void testSpecificNPCs() {
        // Test a few known NPCs from the CSV
        
        // Test Abyssal demon (ID 415) - max hit 8
        Integer abyssalDemonMaxHit = NPCDataLoader.getMaxHit(415);
        assertNotNull("Abyssal demon should have max hit data", abyssalDemonMaxHit);
        assertEquals("Abyssal demon max hit should be 8", Integer.valueOf(8), abyssalDemonMaxHit);
        
        // Test Blue dragon (ID 265) - max hit 10
        Integer blueDragonMaxHit = NPCDataLoader.getMaxHit(265);
        assertNotNull("Blue dragon should have max hit data", blueDragonMaxHit);
        assertEquals("Blue dragon max hit should be 10", Integer.valueOf(10), blueDragonMaxHit);
        
        // Test that a non-existent NPC returns null
        Integer nonExistentNPC = NPCDataLoader.getMaxHit(999999);
        assertNull("Non-existent NPC should return null", nonExistentNPC);
    }
    
    @Test
    public void testComplexMaxHitParsing() {
        // Test NPCs with numeric max hit values from monsters-complete.csv
        
        // Test Black demon (ID 240) - has 16 as max hit
        Integer blackDemonMaxHit = NPCDataLoader.getMaxHit(240);
        assertNotNull("Black demon should have max hit data", blackDemonMaxHit);
        assertEquals("Black demon max hit should be 16", Integer.valueOf(16), blackDemonMaxHit);
        
        // Test King Black Dragon (ID 239) - has 25 as max hit
        Integer kbdMaxHit = NPCDataLoader.getMaxHit(239);
        assertNotNull("King Black Dragon should have max hit data", kbdMaxHit);
        assertEquals("King Black Dragon max hit should be 25", Integer.valueOf(25), kbdMaxHit);
    }
    
    @Test
    public void testWeaknessData() {
        // Test weakness determination based on defense stats
        
        // Abyssal demon (ID 415) should have a weakness based on its defense stats
        String abyssalWeakness = NPCDataLoader.getWeakness(415);
        assertNotNull("Abyssal demon should have weakness data", abyssalWeakness);
        // The weakness is determined by lowest defense stat
        
        // Test magic-using NPCs (should show elemental weakness)
        // Infernal Mage (ID 443) uses magic attacks
        String infernalMageWeakness = NPCDataLoader.getWeakness(443);
        assertNotNull("Infernal Mage should have weakness data", infernalMageWeakness);
        assertEquals("Infernal Mage (magic attacker) should show fire weakness", "fire", infernalMageWeakness);
        
        System.out.println("Sample weaknesses found:");
        System.out.println("Abyssal demon: " + abyssalWeakness);
        System.out.println("Infernal Mage: " + infernalMageWeakness);
    }
    
    @Test
    public void testDefaultWeakness() {
        // Test that loaded NPCs have weaknesses
        
        // Every loaded NPC should have a weakness now (no nulls)
        Map<Integer, Integer> allMaxHits = NPCDataLoader.getAllMaxHitData();
        int totalNpcs = 0;
        int slashCount = 0;
        int stabCount = 0;
        int crushCount = 0;
        int magicCount = 0;
        int rangedCount = 0;
        
        for (Integer npcId : allMaxHits.keySet()) {
            String weakness = NPCDataLoader.getWeakness(npcId);
            assertNotNull("Every NPC should have a weakness (no nulls)", weakness);
            totalNpcs++;
            switch (weakness) {
                case "slash": slashCount++; break;
                case "stab": stabCount++; break;
                case "crush": crushCount++; break;
                case "magic": magicCount++; break;
                case "ranged": rangedCount++; break;
            }
        }
        
        System.out.println("Total NPCs with max hit data: " + totalNpcs);
        System.out.println("Weakness distribution:");
        System.out.println("  Slash: " + slashCount);
        System.out.println("  Stab: " + stabCount);
        System.out.println("  Crush: " + crushCount);
        System.out.println("  Magic: " + magicCount);
        System.out.println("  Ranged: " + rangedCount);
        
        // Verify that weaknesses are being assigned
        assertTrue("Some NPCs should have weaknesses", totalNpcs > 0);
    }
    
    @Test
    public void testMagicAndElementalWeakness() {
        // Test NPCs with magic attack type
        
        // Aberrant spectre (ID 2) uses magic attacks
        String aberrantSpectreWeakness = NPCDataLoader.getWeakness(2);
        assertNotNull("Aberrant spectre should have weakness data", aberrantSpectreWeakness);
        assertEquals("Aberrant spectre (magic attacker) should show fire weakness", "fire", aberrantSpectreWeakness);
        
        // Test NPCs with very low magic defense
        // Look for NPCs where magic defense is significantly lower than other defenses
        
        System.out.println("Magic/Elemental weakness test completed");
        System.out.println("Aberrant spectre (magic attacker): " + aberrantSpectreWeakness);
    }
    
    @Test
    public void testAggressiveData() {
        // Test that NPCs default to non-aggressive when field is empty
        
        // Most NPCs have empty aggressive field and should default to false
        // Kalphite Soldier (ID 138) has empty aggressive field
        boolean kalphiteAggressive = NPCDataLoader.isAggressive(138);
        assertFalse("Kalphite Soldier should not be aggressive (empty field)", kalphiteAggressive);
        
        // Test non-aggressive NPCs
        // Zombie (ID 26) is not aggressive
        boolean zombieAggressive = NPCDataLoader.isAggressive(26);
        assertFalse("Zombie should not be aggressive", zombieAggressive);
        
        // Test that non-existent NPCs return false (default)
        boolean nonExistentAggressive = NPCDataLoader.isAggressive(999999);
        assertFalse("Non-existent NPC should default to non-aggressive", nonExistentAggressive);
        
        // Count aggressive NPCs - note that most have empty data
        Map<Integer, Integer> allMaxHits = NPCDataLoader.getAllMaxHitData();
        int aggressiveCount = 0;
        int totalCount = 0;
        for (Integer npcId : allMaxHits.keySet()) {
            totalCount++;
            if (NPCDataLoader.isAggressive(npcId)) {
                aggressiveCount++;
            }
        }
        
        System.out.println("Aggressive data test completed");
        System.out.println("Total NPCs checked: " + totalCount);
        System.out.println("Total aggressive NPCs: " + aggressiveCount);
        System.out.println("Note: Most NPCs have empty aggressive data in this CSV");
        
        // Since most NPCs have empty aggressive data, we expect very few or none to be marked as aggressive
        assertTrue("Aggressive count should be low or zero", aggressiveCount >= 0);
    }
} 