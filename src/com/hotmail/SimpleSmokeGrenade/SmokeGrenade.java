package com.hotmail.SimpleSmokeGrenade;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

public class SmokeGrenade
{
	private final Item g;
	private int des = 1;
	//
	public SmokeGrenade(final Item g) 
	{
		this.g = g;
	}
	
	public void explode() 
	{
		if (g != null && g.getLocation() != null && g.getLocation().getWorld() != null) 
		{
			final long explodeInstant = System.currentTimeMillis();
			final Location loc = g.getLocation();
			//
			new BukkitRunnable()
			{
				@Override
				public void run() 
				{
					if (loc != null)
					{
						// Play Sound
						if (!VersionUtils.isNewSpigotVersion())
							loc.getWorld().playSound(loc, Sound.valueOf("FIZZ"), 2.0F, 2.0F);
						else
							loc.getWorld().playSound(loc, Sound.ENTITY_TNT_PRIMED, 2.0F, 2.0F);
						
						// Variables
						int seconds = (int)((System.currentTimeMillis() - explodeInstant) / 1000);
						float f = (float) ((((double)seconds) / 10.0D) + 1.5D);
						
						// Display Smoke Effect
						ParticleEffect.CLOUD.display(f, 0.10F, f, ((float)(f / 12)), des, loc.clone().add(0.0D, 0.5D, 0.0D), 10000);
						
						// More Smoke
						if (des < 850)
							des += 10;

						// Cancel
						if (seconds >= Main.getInstance().smokeDuration)
							cancel();
					}
				}
			}.runTaskTimer(Main.getInstance(), 2L, 2L);
		}
	}
}
