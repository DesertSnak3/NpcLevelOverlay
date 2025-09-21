/*
 * NPC Level Overlay Plugin for RuneLite
 * -------------------------------------
 * Shows NPC combat levels (and optionally names) above their heads, with
 * vanilla attack‑option colours. Holding **Shift** can temporarily promote
 * "Attack" to the default left‑click when enabled in settings.
 */

package com.dexhonsa;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.Point;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "NPC Level Overlay",
        description = "Shows NPC combat levels (and optionally names) above their heads.",
        tags = {"npc", "level", "combat", "overlay"}
)
public class NpcLevelPlugin extends Plugin
{
    // Injected services
    @Inject private OverlayManager overlayManager;
    @Inject private NpcLevelOverlay overlay;
    @Inject private Client client;
    @Inject private NpcLevelConfig config;

    /*---------------- CONFIG ----------------*/
    @Provides
    NpcLevelConfig provideConfig(ConfigManager cm)
    {
        return cm.getConfig(NpcLevelConfig.class);
    }

    /*---------------- LIFECYCLE ----------------*/
    @Override protected void startUp()
    {
        overlayManager.add(overlay);
        log.info("NPC Level Overlay started.");
    }

    @Override protected void shutDown()
    {
        overlayManager.remove(overlay);
        log.info("NPC Level Overlay stopped.");
    }
}

/*=====================================================================
 * OVERLAY CLASS
 *====================================================================*/
@Slf4j
class NpcLevelOverlay extends Overlay
{
    private static final int OFFSET_Z = 40;
    private static final Color GREEN  = new Color(0x00FF00);
    private static final Color YELLOW = new Color(0xFFFF00);
    private static final Color ORANGE = new Color(0xFF981F);
    private static final Color RED    = new Color(0xFF0000);
    
    // Icon scaling factor - will be dynamically set from config

    private final Client client;
    private final NpcLevelConfig config;
    
    // Weakness icon cache - stores original images (scaling done at render time)
    private static final Map<String, BufferedImage> weaknessIconsOriginal = new HashMap<>();

    // Aggression icon - original
    private static BufferedImage aggressionIconOriginal;

    static {
        loadWeaknessIcons();
        loadAggressionIcon();
    }
    
    private static void loadWeaknessIcons() {
        // Load all weakness icons
        loadIcon("fire", "/Fire_rune.png");
        loadIcon("water", "/Water_rune.png");
        loadIcon("earth", "/Earth_rune.png");
        loadIcon("air", "/Air_rune.png");
        loadIcon("stab", "/White_dagger.png");
        loadIcon("slash", "/White_scimitar.png");
        loadIcon("crush", "/White_warhammer.png");
        loadIcon("magic", "/Magic_icon.png");
        loadIcon("ranged", "/Steel_arrow_5.png");  // Default ranged icon
        loadIcon("arrow", "/Steel_arrow_5.png");
        loadIcon("bolt", "/Steel_bolts_5.png");
        loadIcon("dart", "/Steel_dart.png");
    }

    private static void loadIcon(String name, String fileName) {
        try {
            BufferedImage originalIcon = ImageUtil.loadImageResource(NpcLevelOverlay.class, fileName);
            if (originalIcon != null) {
                weaknessIconsOriginal.put(name, originalIcon);
            }
        } catch (Exception e) {
            log.warn("Failed to load weakness icon: " + fileName, e);
        }
    }
    
    private static void loadAggressionIcon() {
        try {
            BufferedImage originalIcon = ImageUtil.loadImageResource(NpcLevelOverlay.class, "/aggression_icon.png");
            if (originalIcon != null) {
                aggressionIconOriginal = originalIcon;
            }
        } catch (Exception e) {
            log.warn("Failed to load aggression icon", e);
        }
    }

    private BufferedImage scaleIcon(BufferedImage original, double scale) {
        if (original == null) return null;
        int scaledWidth = (int)(original.getWidth() * scale);
        int scaledHeight = (int)(original.getHeight() * scale);
        return ImageUtil.resizeImage(original, scaledWidth, scaledHeight);
    }

    @Inject
    NpcLevelOverlay(Client client, NpcLevelConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Player local = client.getLocalPlayer();
        if (local == null) return null;
        int playerLevel = local.getCombatLevel();

        for (NPC npc : client.getNpcs())
        {
            NPCComposition comp = npc.getTransformedComposition();
            if (comp == null) continue;
            int npcLevel = comp.getCombatLevel();
            if (npcLevel <= 0) continue;
            if (npcLevel < config.minLevel()) continue;

            // Check if we should only show NPCs in combat
            if (config.onlyShowInCombat())
            {
                Actor npcTarget = npc.getInteracting();
                Actor playerTarget = local.getInteracting();
                boolean inCombat = (npcTarget == local) || (playerTarget == npc);
                if (!inCombat) continue;
            }

            Color colour = levelColour(npcLevel - playerLevel);

            String textBody = "";
            if (config.showName() && config.showLevel())
            {
                textBody = comp.getName() + " (" + npcLevel + ")";
            }
            else if (config.showName())
            {
                textBody = comp.getName();
            }
            else if (config.showLevel())
            {
                textBody = Integer.toString(npcLevel);
            }

            if (config.showHits())
            {
                // Use the new NPCDataLoader to get max hit
                Integer maxHit = NPCDataLoader.getMaxHit(npc.getId());
                
                if (maxHit != null && maxHit > 0)
                {
                    textBody += " [" + maxHit + "]";
                }
            }

            LocalPoint lp = npc.getLocalLocation();
            Point loc = Perspective.getCanvasTextLocation(client, graphics, lp, textBody, npc.getLogicalHeight() + OFFSET_Z);
            if (loc != null)
            {
                // Get icon scale from config
                double iconScale = config.iconSize().getScale();

                // Calculate total width needed for icons
                int totalIconWidth = 0;
                boolean showWeakness = config.showWeaknessIcon();

                // Check if NPC will actually be aggressive to the player
                boolean npcIsAggressive = NPCDataLoader.isAggressive(npc.getId());
                boolean willAttackPlayer = false;

                if (npcIsAggressive) {
                    // OSRS aggression formula: if playerLevel > (2 * npcLevel + 1), NPC won't auto-attack
                    // Example: level 28 hobgoblin won't attack level 57+ players
                    if (playerLevel <= (2 * npcLevel + 1)) {
                        willAttackPlayer = true;
                    }
                    // TODO: Add support for "always aggressive" NPCs that ignore the level formula
                }

                boolean showAggression = config.showAggressionIcon() && willAttackPlayer;

                String weakness = NPCDataLoader.getWeakness(npc.getId());
                BufferedImage weaknessIconOriginal = weakness != null && showWeakness ? weaknessIconsOriginal.get(weakness) : null;
                BufferedImage weaknessIcon = scaleIcon(weaknessIconOriginal, iconScale);
                BufferedImage aggressionIcon = showAggression ? scaleIcon(aggressionIconOriginal, iconScale) : null;

                if (weaknessIcon != null) {
                    totalIconWidth += weaknessIcon.getWidth() + 4; // Icon width + padding
                }

                if (aggressionIcon != null) {
                    totalIconWidth += aggressionIcon.getWidth() + 4; // Icon width + padding
                }

                // Draw weakness icon on the far left
                int currentX = loc.getX() - totalIconWidth;
                if (weaknessIcon != null) {
                    int iconY = loc.getY() - weaknessIcon.getHeight() / 2 - 4;

                    graphics.drawImage(weaknessIcon, currentX, iconY, null);
                    currentX += weaknessIcon.getWidth() + 4; // Move right for next icon
                }

                // Draw aggression icon next (to the left of the text)
                if (aggressionIcon != null) {
                    int iconY = loc.getY() - aggressionIcon.getHeight() / 2 - 4;

                    graphics.drawImage(aggressionIcon, currentX, iconY, null);
                }
                
                // Draw the text
                OverlayUtil.renderTextLocation(graphics, loc, textBody, colour);
            }
        }
        return null;
    }

    private static Color levelColour(int diff)
    {
        if (diff < 0)  return GREEN;
        if (diff == 0) return YELLOW;
        if (diff <= 3) return ORANGE;
        return RED;
    }

    private static boolean isAttackable(NPCComposition comp)
    {
        String[] actions = comp.getActions();
        if (actions == null) return false;
        for (String a : actions)
        {
            if ("Attack".equalsIgnoreCase(a))
            {
                return true;
            }
        }
        return false;
    }
}
