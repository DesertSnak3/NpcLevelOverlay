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
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.Point;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.*;
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

    private final Client client;
    private final NpcLevelConfig config;
    
    // Weakness icon cache
    private static final Map<String, BufferedImage> weaknessIcons = new HashMap<>();
    
    static {
        loadWeaknessIcons();
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
    
    private static void loadIcon(String name, String path) {
        try {
            BufferedImage icon = javax.imageio.ImageIO.read(NpcLevelOverlay.class.getResourceAsStream(path));
            if (icon != null) {
                weaknessIcons.put(name, icon);
            }
        } catch (Exception e) {
            log.warn("Failed to load weakness icon: " + path, e);
        }
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

            Color colour = levelColour(npcLevel - playerLevel);

            String textBody;
            if (config.showName())
            {
                textBody = comp.getName() + " (" + npcLevel + ")";
            }
            else
            {
                textBody = Integer.toString(npcLevel);
            }

            if (config.showHits())
            {
                // Use the new NPCDataLoader to get max hit
                Integer maxHit = NPCDataLoader.getMaxHit(npc.getId());
                
                if (maxHit != null && maxHit > 0)
                {
                    textBody += " [0-" + maxHit + "]";
                }
            }

            LocalPoint lp = npc.getLocalLocation();
            Point loc = Perspective.getCanvasTextLocation(client, graphics, lp, textBody, npc.getLogicalHeight() + OFFSET_Z);
            if (loc != null)
            {
                // Draw the text
                OverlayUtil.renderTextLocation(graphics, loc, textBody, colour);
                
                // Draw weakness icon if enabled and available
                if (config.showWeaknessIcon())
                {
                    String weakness = NPCDataLoader.getWeakness(npc.getId());
                    if (weakness != null && weaknessIcons.containsKey(weakness))
                    {
                        BufferedImage icon = weaknessIcons.get(weakness);
                        // Scale down the icon to 75% of original size
                        int iconWidth = (int)(icon.getWidth() * 0.75);
                        int iconHeight = (int)(icon.getHeight() * 0.75);
                        
                        // Calculate icon position (to the left of the text)
                        int textWidth = graphics.getFontMetrics().stringWidth(textBody);
                        int iconX = loc.getX() - iconWidth - 4; // 4px padding
                        int iconY = loc.getY() - iconHeight / 2 - 4; // Center vertically with text
                        
                        graphics.drawImage(icon, iconX, iconY, iconWidth, iconHeight, null);
                    }
                }
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
