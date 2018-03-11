package com.hotmail.SimpleSmokeGrenade;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener
{
	private static Main instance;
	private static Integer task;
	private final Map<UUID, Object[]> Grenades = new HashMap<UUID, Object[]>();
	private final Map<UUID, Long> delay = new HashMap<UUID, Long>();
	private final Map<UUID, Long> cDelay = new HashMap<UUID, Long>();
	
	// Variables
	public int timeToExplosion = 5;
	public int smokeDuration = 15;
	
	public int useCommandDelay = 60;
	public String waitCommandMessage = "&cYou have to wait {SECONDS} seconds to use this command again";
	
	public int useDelay = 5;
	public String wait = "&cYou have to wait {SECONDS} seconds to use the grenade again";
	public String grenadeName = "&f&lSmokeGrenade";
	
	public boolean visibleTime = true;
	
	//
	@Override
	public void onEnable() 
	{
		// Creating instance;
		instance = this;
		
		// Register Give Grenade Command
		new GiveGrenadeCommand(this);
		
		// Save Configuration File
		saveDefaultConfig();
		
		// Get Configuration Values
		FileConfiguration f = this.getConfig();
		if (f != null) 
		{
			timeToExplosion = f.getInt("ExplodeTime", timeToExplosion);
			smokeDuration = f.getInt("SmokeDuration", smokeDuration);
			visibleTime = f.getBoolean("VisibleExplodeTime", visibleTime);
			useDelay = f.getInt("Delay", useDelay);
			wait = f.getString("DelayMessage", wait);
			wait = ChatColor.translateAlternateColorCodes('&', wait);
			grenadeName = f.getString("GrenadeName", grenadeName);
			grenadeName = ChatColor.translateAlternateColorCodes('&', grenadeName);
			useCommandDelay = f.getInt("CommandDelay", useCommandDelay);
			waitCommandMessage = f.getString("CommandDelayMessage", waitCommandMessage);
			waitCommandMessage = ChatColor.translateAlternateColorCodes('&', waitCommandMessage);
		}
		
		// Print enable Message
		Bukkit.getConsoleSender().sendMessage(ChatColor.WHITE + "[SimpleSmokeGrenade] " + ChatColor.GREEN + "Enabled!");
		
		// Register Events in this Class
		Bukkit.getPluginManager().registerEvents(this, this);
		
		// Creating Task
		task = Integer.valueOf(new BukkitRunnable() 
		{
			@Override
			public void run()
			{
				try
				{
					for (UUID id : Grenades.keySet()) 
					{
						if (id != null && Grenades.get(id) != null) 
						{
							Object[] o = (Object[])Grenades.get(id);
							if (o.length == 2) 
							{
								if (o[0] instanceof Integer && o[1] instanceof World) 
								{
									Integer t = (Integer) o[0];
									World w = (World) o[1];
									//
									if (t != null && w != null) 
									{
										Item it = getItem(w, id);
										if (it != null && !it.isDead()) 
										{
											if (t.intValue() > 0) 
											{
												if (visibleTime)
													it.setCustomName(ChatColor.DARK_RED + String.valueOf((t.intValue() - 1)));
												//
												Grenades.put(id, new Object[] {Integer.valueOf(t.intValue() - 1), it.getWorld()});
												//
												if (VersionUtils.isNewSpigotVersion())
													it.getLocation().getWorld().playSound(it.getLocation(), Sound.UI_BUTTON_CLICK, 1.4F, 2.0F);
												else
													it.getLocation().getWorld().playSound(it.getLocation(), Sound.valueOf("CLICK"), 1.4F, 2.0F);
											}
											else
											{
												if (VersionUtils.isNewSpigotVersion())
													it.getLocation().getWorld().playSound(it.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 8.0F);
												else
													it.getLocation().getWorld().playSound(it.getLocation(), Sound.valueOf("EXPLODE"), 2.0F, 8.0F);
												//
												it.remove();
												Grenades.remove(id);
											}
										}
									}
								}
							}
						}
					}
				}
				catch(ConcurrentModificationException t) {}
			}
		}.runTaskTimer(instance, 20L, 20L).getTaskId());
	}
	
	private Item getItem(World w, UUID id) 
	{
		Item tor = null;
		//
		if (w != null && id != null) 
		{
			for (Entity ent : w.getEntities())
				if (ent != null && ent instanceof Item)
					if (id.equals(ent.getUniqueId()))
						tor = (Item)ent;
		}
		//
		return tor;
	}
	
	public static Main getInstance() 
	{
		return instance;
	}
	
	public static Integer getTaskID() 
	{
		return task;
	}
	
	public boolean isGrenade(ItemStack stack) 
	{
		if (stack == null || stack.getItemMeta() == null || stack.getItemMeta().getDisplayName() == null)
			return false;
		//
		return stack.getType() == Material.TRIPWIRE_HOOK && stack.getItemMeta().getDisplayName().equals(grenadeName);
	}
	
	public Long getCommandDelay(final Player p) 
	{	
		return cDelay.get(p.getUniqueId());
	}
	
	public void addCommandDelay(final Player p)
	{
		cDelay.put(p.getUniqueId(), System.currentTimeMillis());
	}
	
	public boolean isWatingCommand(final Player p) 
	{
		if (p == null || p.getUniqueId() == null)
			return false;
		//
		Long l = cDelay.get(p.getUniqueId());
		if (l != null)
		{
			return ((int)((System.currentTimeMillis() - l) / 1000)) < useCommandDelay;
		}
		//
		return false;
	}
	
	public ItemStack getGrenade() 
	{
		ItemStack tor = new ItemStack(Material.TRIPWIRE_HOOK, 1);
		//
		// Getting ItemMeta
		ItemMeta meta = tor.getItemMeta();
		if (meta == null)
			meta = Bukkit.getItemFactory().getItemMeta(Material.TRIPWIRE_HOOK);
		
		// Set DisplayName
		meta.setDisplayName(Main.getInstance().grenadeName);
		
		// Set ItemMeta
		tor.setItemMeta(meta);
		
		// Returning "tor"
		return tor;
	}
	
	@EventHandler
	public void onThrowGrenade(final PlayerInteractEvent eve) 
	{
		final Player p = eve.getPlayer();
		final ItemStack i = eve.getItem();

		// Verify Grenade Item
		if (!isGrenade(i))
			return;
		
		// Verify Delay
		Long l = delay.get(p.getUniqueId());
		if (l != null) 
		{
			int ll = (int)((System.currentTimeMillis() - l) / 1000);
			if (ll < useDelay)
			{
				p.sendMessage(wait.replace("{SECONDS}", ""+(useDelay - ll)));
				return;
			}
		}
		
		// Canceling the Event
		eve.setCancelled(true);
		
		// Throwing it!
		final Item t = eve.getPlayer().getWorld().dropItem(p.getEyeLocation(), new ItemStack(Material.TRIPWIRE_HOOK, 1));
		final SmokeGrenade Grenade = new SmokeGrenade(t);
		t.setVelocity(p.getEyeLocation().getDirection().multiply(1.2));
		t.setPickupDelay(Integer.MAX_VALUE);
		
		// Add Use Delay
		delay.put(p.getUniqueId(), System.currentTimeMillis());
		
		// Set Name
		if (visibleTime)
			t.setCustomName(ChatColor.DARK_RED + Integer.valueOf(timeToExplosion).toString());
		t.setCustomNameVisible(visibleTime);
		
		// Register
		final Object[] put = new Object[]{Integer.valueOf(timeToExplosion), t.getWorld()};
		Grenades.put(t.getUniqueId(), put);
		
		// Remove a Grenade
		p.getInventory().removeItem(getGrenade());
		
		// Update Player Inventory
		p.updateInventory();
		
		// add Smoke
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				if (t == null || t.isDead())
				{
					cancel();
					Grenade.explode();
				}
			}
		}.runTaskTimer(instance, 2L, 2L);
	}
}
