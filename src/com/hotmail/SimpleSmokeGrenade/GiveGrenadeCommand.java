package com.hotmail.SimpleSmokeGrenade;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GiveGrenadeCommand implements CommandExecutor
{
	public GiveGrenadeCommand(JavaPlugin p) 
	{
		p.getCommand("SmokeGrenade").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player) 
		{
			if (sender.hasPermission("smokegrenade.use") || sender.isOp()) 
			{
				Player p = (Player)sender;
				//
				if (Main.getInstance().isWatingCommand(p)) 
				{
					p.sendMessage(Main.getInstance().waitCommandMessage.replace("{SECONDS}", ""+(Main.getInstance().useCommandDelay - (int)((System.currentTimeMillis() - Main.getInstance().getCommandDelay(p)) / 1000))));
					return true;
				}
				//
				Main.getInstance().addCommandDelay(p);
				p.getInventory().addItem(Main.getInstance().getGrenade());
				p.updateInventory();
			}
			else
				sender.sendMessage(ChatColor.RED + "You dont have permission to use this command!");
		}
		else
			sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
		return true;
	}

}
