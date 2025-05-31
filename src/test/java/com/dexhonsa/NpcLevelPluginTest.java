package com.dexhonsa;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;


public class NpcLevelPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NpcLevelPlugin.class);
		RuneLite.main(args);
	}
}