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
        
        // Test that a non-existent NPC returns null
        Integer nonExistentNPC = NPCDataLoader.getMaxHit(999999);
        assertNull("Non-existent NPC should return null", nonExistentNPC);
    }
    
    @Test
    public void testComplexMaxHitParsing() {
        // Test NPCs with complex max hit values
        
        // Cerberus (ID 5862) - has "23" as max hit
        Integer cerberusMaxHit = NPCDataLoader.getMaxHit(5862);
        assertNotNull("Cerberus should have max hit data", cerberusMaxHit);
        assertEquals("Cerberus max hit should be 23", Integer.valueOf(23), cerberusMaxHit);
        
        // Test an NPC with multiple damage types
        // Commander Zilyana (ID 2205) - "27 (melee),20 (magic)"
        Integer zilyanaMaxHit = NPCDataLoader.getMaxHit(2205);
        assertNotNull("Commander Zilyana should have max hit data", zilyanaMaxHit);
        assertEquals("Commander Zilyana max hit should be highest value (27)", Integer.valueOf(27), zilyanaMaxHit);
    }
    
    @Test
    public void testWeaknessData() {
        // Test elemental weaknesses
        
        // Ahrim the Blighted (ID 1672) - weak to Air
        String ahrimWeakness = NPCDataLoader.getWeakness(1672);
        assertNotNull("Ahrim should have weakness data", ahrimWeakness);
        assertEquals("Ahrim should be weak to air", "air", ahrimWeakness);
        
        // Test combat weaknesses
        
        // Abyssal demon (ID 415) - weak to Stab
        String abyssalWeakness = NPCDataLoader.getWeakness(415);
        assertNotNull("Abyssal demon should have weakness data", abyssalWeakness);
        assertEquals("Abyssal demon should be weak to stab", "stab", abyssalWeakness);
        
        // Test NPC with no weakness
        String noWeaknessNPC = NPCDataLoader.getWeakness(2834); // Some generic NPC
        // May or may not have weakness, just testing it doesn't crash
        
        System.out.println("Sample weaknesses found:");
        System.out.println("Ahrim: " + ahrimWeakness);
        System.out.println("Abyssal demon: " + abyssalWeakness);
    }
    
    @Test
    public void testDefaultWeakness() {
        // Test that loaded NPCs have weaknesses
        
        // Test some NPCs that should have specific weaknesses
        String ahrimWeakness = NPCDataLoader.getWeakness(1672); // Should be air
        assertEquals("air", ahrimWeakness);
        
        // Test chicken which has stab weakness
        String chickenWeakness = NPCDataLoader.getWeakness(1173);
        assertNotNull("Chicken should have a weakness", chickenWeakness);
        assertEquals("Chicken should be weak to stab", "stab", chickenWeakness);
        
        // Every loaded NPC should have a weakness now (no nulls)
        Map<Integer, Integer> allMaxHits = NPCDataLoader.getAllMaxHitData();
        int totalNpcs = 0;
        int slashCount = 0;
        
        for (Integer npcId : allMaxHits.keySet()) {
            String weakness = NPCDataLoader.getWeakness(npcId);
            assertNotNull("Every NPC should have a weakness (no nulls)", weakness);
            totalNpcs++;
            if ("slash".equals(weakness)) {
                slashCount++;
            }
        }
        
        System.out.println("Total NPCs with max hit data: " + totalNpcs);
        System.out.println("NPCs with slash weakness: " + slashCount);
        System.out.println("Percentage with slash: " + (slashCount * 100.0 / totalNpcs) + "%");
        
        // Verify that slash is indeed being used as default for many NPCs
        assertTrue("Some NPCs should have slash weakness", slashCount > 0);
    }
} 