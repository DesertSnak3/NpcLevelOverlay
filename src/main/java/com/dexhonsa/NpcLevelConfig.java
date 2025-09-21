package com.dexhonsa;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * Configuration for the NPC Level Overlay plugin.
 */
@ConfigGroup("npclevel")
public interface NpcLevelConfig extends Config
{
    enum IconSize
    {
        XS("Extra Small", 0.5),
        S("Small", 0.75),
        M("Medium", 1.0),
        LG("Large", 1.25);

        private final String name;
        private final double scale;

        IconSize(String name, double scale)
        {
            this.name = name;
            this.scale = scale;
        }

        public double getScale()
        {
            return scale;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
    @ConfigItem(
            keyName = "showLevel",
            name = "Show level",
            description = "Display the NPC's combat level."
    )
    default boolean showLevel()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showName",
            name = "Show NPC name",
            description = "Draw the NPC's name next to its level text."
    )
    default boolean showName()
    {
        return false;
    }

    @ConfigItem(
            keyName = "textColorGreen",
            name = "Custom Green",
            description = "Override the default green colour for lower‑level NPCs.",
            hidden = true
    )
    default Color textColorGreen() { return new Color(0x00FF00); }

    @ConfigItem(
            keyName = "textColorYellow",
            name = "Custom Yellow",
            description = "Override the default yellow colour for equal‑level NPCs.",
            hidden = true
    )
    default Color textColorYellow() { return new Color(0xFFFF00); }

    @ConfigItem(
            keyName = "textColorOrange",
            name = "Custom Orange",
            description = "Override the default orange colour for slightly‑higher NPCs.",
            hidden = true
    )
    default Color textColorOrange() { return new Color(0xFF981F); }

    @ConfigItem(
            keyName = "textColorRed",
            name = "Custom Red",
            description = "Override the default red colour for much‑higher NPCs.",
            hidden = true
    )
    default Color textColorRed() { return new Color(0xFF0000); }

    @ConfigItem(
            keyName = "showHits",
            name = "Show Max Hit",
            description = "Display the NPC's max possible hit alongside its level."
    )
    default boolean showHits()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showWeaknessIcon",
            name = "Show Weakness Icon",
            description = "Display an icon indicating what the NPC is weak to."
    )
    default boolean showWeaknessIcon()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showAggressionIcon",
            name = "Show Aggression Icon",
            description = "Display an icon indicating if the NPC is aggressive."
    )
    default boolean showAggressionIcon()
    {
        return true;
    }

    @ConfigItem(
            keyName = "iconSize",
            name = "Icon Size",
            description = "Set the size of weakness and aggression icons."
    )
    default IconSize iconSize()
    {
        return IconSize.M;
    }

    @ConfigItem(
            keyName = "minLevel",
            name = "Minimum Level",
            description = "Only show overlays for NPCs at or above this combat level (0 = show all)."
    )
    default int minLevel()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "onlyShowInCombat",
            name = "Only show in combat",
            description = "Only display overlays for NPCs you are currently in combat with."
    )
    default boolean onlyShowInCombat()
    {
        return false;
    }
}
