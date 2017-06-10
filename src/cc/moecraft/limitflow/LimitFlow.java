package cc.moecraft.limitflow;

import cc.moecraft.hykilpikonna.essentials.logger.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

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

    Logger logger;

    public void onEnable()
    {
        try
        {
            logger = new Logger("HyLimitFlow", true);
            logger.log("此插件正在加载......");
        }
        catch (java.lang.NoClassDefFoundError error)
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[HyFluidLimit]此插件需要HyEssentials前置!");
        }
        getConfig().options().copyDefaults(true);
        checkConfig();
        saveConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void checkConfig()
    {
        if (getConfig().getBoolean("DefaultConfig"))
        {
            getConfig().addDefault("Debug", true);
            getConfig().set("DefaultConfig", false);
            writeConfig(true);
        }
        else
        {
            logger.setDebug(getConfig().getBoolean("Debug"));
            writeConfig(false);
        }
        saveConfig();
    }

    private void writeConfig(boolean firstTimeCreatingConfigFile)
    {
        logger.Debug(String.format("[配置][生成]开始写入配置, 生成初始配置: %s", firstTimeCreatingConfigFile));

        ArrayList<String> worldList = new ArrayList<>();
        //把世界加入配置文件里
        logger.Debug("[配置][生成]正在把世界列表存进缓存....");
        for (World world : getServer().getWorlds())
        {
            logger.Debug(String.format("[配置][生成][世界]已将: %s存入世界列表", world.getName()));
            worldList.add(world.getName());
            if (firstTimeCreatingConfigFile)
            {
                saveWorldConfig(world);
            }
        }
        logger.Debug("[配置][生成]已将世界列表存入缓存");
        if (firstTimeCreatingConfigFile)
        {
            logger.Debug("[配置][生成]已将世界列表存入配置文件");
            getConfig().addDefault("WorldList", worldList);
        }
        else
        {
            logger.Debug("[配置][修改]正在检测世界列表是不是最新....");
            List<String> oldWorldListTemp = (List<String>)(getConfig().getList("WorldList"));
            ArrayList<String> oldWorldList = new ArrayList<>(oldWorldListTemp.size());
            oldWorldList.addAll(oldWorldListTemp);
            if (!oldWorldList.equals(worldList))
            {
                logger.Debug("[配置][修改]不是最新!");
                logger.Debug("[配置][修改]正在检查每一个世界的配置...");
                for (String world : worldList)
                {
                    logger.Debug(String.format("[配置][修改][世界]正在检查%s的配置...", world));
                    if (oldWorldList.contains(world))
                    {
                        logger.Debug(String.format("[配置][修改][世界]%s的配置已存在!", world));
                    }
                    else
                    {
                        logger.Debug(String.format("[配置][修改][世界]%s的配置不存在!", world));
                        saveWorldConfig(world);
                    }
                }
                getConfig().set("WorldList", worldList);
                logger.Debug("[配置][修改]已将世界列表更新");
            }
            else
            {
                logger.Debug("[配置][修改]是最新!");
            }
        }
    }

    private void saveWorldConfig(World world)
    {
        saveWorldConfig(world.getName());
    }

    private void saveWorldConfig(String world)
    {
        getConfig().addDefault(world + ".Water.Limit", true);
        getConfig().addDefault(world + ".Water.HorizontalLimit", 3);
        getConfig().addDefault(world + ".Water.VerticalLimit", 5);
        getConfig().addDefault(world + ".Lava.Limit", true);
        getConfig().addDefault(world + ".Lava.HorizontalLimit", 3);
        getConfig().addDefault(world + ".Lava.VerticalLimit", 5);
        logger.Debug(String.format("[配置][生成][世界]已生成%s的默认配置", world));
    }

    public void onDisable() {}

    @EventHandler(priority= EventPriority.LOW, ignoreCancelled=true)
    public void onBlockFromToEvent(BlockFromToEvent event)
    {
        logger.Debug("[事件]BlockFromToEvent激发");
        Material material = event.getBlock().getType();
        if (material.equals(Material.WATER) || material.equals(Material.STATIONARY_WATER) || material.equals(Material.LAVA) || material.equals(Material.STATIONARY_LAVA))
        {
            Block toBlock = event.getToBlock();
            logger.Debug("[事件][处理]方块是液体类");
            String blockName = material.name();
            logger.Debug(String.format("[事件][处理]方块名被存为%s", blockName));
            Location location = toBlock.getLocation();
            logger.Debug(String.format("[事件][处理]方块位置为: [X=%s, Y=%s, Z=%s] 在%s世界", location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()));
            if (getConfig().getBoolean(location.getWorld().getName() + "." + blockName + ".Limit"))
            {
                logger.Debug(String.format("[事件][处理]世界%s的%s限制为真", location.getWorld(), blockName));
                if (event.getBlock().getData() >= getConfig().getInt(location.getWorld().getName() + "." + blockName + ".Limit"))
                {
                    event.setCancelled(true);
                    logger.Debug("[事件][处理]横向流动已检测, 此事件已被取消!");
                }
                else
                {
                    Location tempLocation = toBlock.getLocation();
                    logger.Debug(String.format("[事件][处理]缓存方块位置为: [X=%s, Y=%s, Z=%s] 在%s世界", tempLocation.getBlockX(), tempLocation.getBlockY(), tempLocation.getBlockZ(), tempLocation.getWorld().getName()));
                    tempLocation.setY(toBlock.getLocation().getBlockY() + getConfig().getInt(location.getWorld().getName() + "." + blockName + ".VerticalLimit"));
                    logger.Debug(String.format("[事件][处理]缓存方块位置已改为: [X=%s, Y=%s, Z=%s] 在%s世界", tempLocation.getBlockX(), tempLocation.getBlockY(), tempLocation.getBlockZ(), tempLocation.getWorld().getName()));
                    Material higherBlock = tempLocation.getBlock().getType();
                    logger.Debug(String.format("[事件][处理]更高的方块名被存为%s", higherBlock.name()));
                    if (higherBlock.equals(Material.WATER) || higherBlock.equals(Material.STATIONARY_WATER) || higherBlock.equals(Material.LAVA) || higherBlock.equals(Material.STATIONARY_LAVA))
                    {
                        event.setCancelled(true);
                        logger.Debug(String.format("[事件][处理]更高的方块是液体, 此事件已被取消"));
                    }
                    else
                    {
                        logger.Debug(String.format("[事件][处理]更高的方块不是液体."));
                    }
                }
            }
            else
            {
                logger.Debug(String.format("[事件][处理]世界%s的%s限制为假!", location.getWorld(), blockName));
            }
        }
    }
}
