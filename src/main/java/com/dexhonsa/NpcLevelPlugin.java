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
    
    // Aggression icon
    private static BufferedImage aggressionIcon;
    
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
    
    private static void loadAggressionIcon() {
        try {
            aggressionIcon = javax.imageio.ImageIO.read(NpcLevelOverlay.class.getResourceAsStream("/aggression_icon.png"));
        } catch (Exception e) {
            log.warn("Failed to load aggression icon", e);
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
                // Calculate total width needed for icons
                int totalIconWidth = 0;
                boolean showWeakness = config.showWeaknessIcon();
                boolean showAggression = config.showAggressionIcon() && NPCDataLoader.isAggressive(npc.getId());
                
                String weakness = NPCDataLoader.getWeakness(npc.getId());
                BufferedImage weaknessIcon = weakness != null && showWeakness ? weaknessIcons.get(weakness) : null;
                
                if (weaknessIcon != null) {
                    totalIconWidth += (int)(weaknessIcon.getWidth() * 0.75) + 4; // Icon width + padding
                }
                
                if (showAggression && aggressionIcon != null) {
                    totalIconWidth += (int)(aggressionIcon.getWidth() * 0.75) + 4; // Icon width + padding
                }
                
                // Draw weakness icon on the far left
                int currentX = loc.getX() - totalIconWidth;
                if (weaknessIcon != null) {
                    int iconWidth = (int)(weaknessIcon.getWidth() * 0.75);
                    int iconHeight = (int)(weaknessIcon.getHeight() * 0.75);
                    int iconY = loc.getY() - iconHeight / 2 - 4;
                    
                    graphics.drawImage(weaknessIcon, currentX, iconY, iconWidth, iconHeight, null);
                    currentX += iconWidth + 4; // Move right for next icon
                }
                
                // Draw aggression icon next (to the left of the text)
                if (showAggression && aggressionIcon != null) {
                    int iconWidth = (int)(aggressionIcon.getWidth() * 0.75);
                    int iconHeight = (int)(aggressionIcon.getHeight() * 0.75);
                    int iconY = loc.getY() - iconHeight / 2 - 4;
                    
                    graphics.drawImage(aggressionIcon, currentX, iconY, iconWidth, iconHeight, null);
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
