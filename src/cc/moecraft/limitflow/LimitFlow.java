package cc.moecraft.limitflow;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * 此类由 Hykilpikonna 在 2017/06/07 创建!
 * Created by Hykilpikonna on 2017/06/07!
 * Twitter: @Hykilpikonna
 * QQ/Wechat: 871674895
 */
public class LimitFlow
        extends JavaPlugin
        implements Listener
{

    public void onEnable()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void checkConfig()
    {
        if (getConfig().getBoolean("DefaultConfig"))
        {

            ArrayList<String> worldList = new ArrayList<>();
            //把世界加入配置文件里
            for (World world : getServer().getWorlds())
            {
                worldList.add(world.getName());
                getConfig().addDefault(world.getName() + ".useDefaults", false);
            }
            getConfig().addDefault("WorldList", worldList);
        }
        else
        {
            ArrayList<String> worldList = new ArrayList<>();
            //把世界加入配置文件里
            for (World world : getServer().getWorlds())
            {
                worldList.add(world.getName());
            }
            getConfig().addDefault("WorldList", worldList);
        }
    }

    public void onDisable() {}

    @EventHandler(priority= EventPriority.LOW, ignoreCancelled=true)
    public void onBlockFromToEvent(BlockFromToEvent event)
    {
        Material material = event.getBlock().getType();
        Location loc = event.getToBlock().getLocation();
        String wn = loc.getWorld().getName();

        ConfigurationSection Config = getConfig();
        String path;
        if ((material == Material.WATER) || (material == Material.STATIONARY_WATER))
        {
            path = "water";
        }
        else
        {
            if ((material == Material.LAVA) || (material == Material.STATIONARY_LAVA)) {
                path = "lava";
            } else {
                path = String.valueOf(event.getBlock().getType());
            }
        }
        if ((Config.get("Worlds." + wn) != null) && (Config.get("Worlds." + wn + "." + path) != null))
        {
            path = "Worlds." + wn + "." + path;
        }
        else
        {
            path = "Default." + path;
            if (Config.get(path) == null)
            {
                Bukkit.getConsoleSender().sendMessage("Find new fluid: " + event.getBlock().getType());
                Config.set(path + ".Limit", Boolean.TRUE);
                Config.set(path + ".MinYFlow", 50);
                Config.set(path + ".MaxYFlow", 70);
                Config.set(path + ".Flow", 3);
                saveConfig();
            }
        }
        if (Config.getBoolean(path + ".Limit")) {
            if ((loc.getY() >= Config.getInt(path + ".MinYFlow")) && (loc.getY() <= Config.getInt(path + ".MaxYFlow")))
            {
                if (event.getBlock().getData() >= Config.getInt(path + ".Flow")) {
                    event.setCancelled(true);
                }
            }
            else {
                event.setCancelled(true);
            }
        }
    }
}
