package com.relicscape;

import com.relicscape.gpu.RelicScapeGpuPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RelicScapePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RelicScapePlugin.class, RelicScapeGpuPlugin.class);
		RuneLite.main(args);
	}
}