package net.limework.rediskript;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Getter;
import net.limework.rediskript.commands.CommandReloadRedis;
import net.limework.rediskript.events.RedisMessageEvent;
import net.limework.rediskript.managers.RedisManager;
import net.limework.rediskript.skript.elements.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;

public class RediSkript extends JavaPlugin {

    //Redis manager
    private RedisManager rm;

    public void startRedis(boolean reload) {
        if (reload) { reloadConfig(); }
        rm = new RedisManager(this);
        rm.start();
    }
    public void registerSyntax() {
        SkriptAddon addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("net.limework.rediskript.skript", "elements");
            Skript.registerEvent("redis message", EvtRedis.class, RedisMessageEvent.class, "redis message");
            Skript.registerExpression(ExprVariableInChannel.class, Object.class, ExpressionType.PROPERTY, "variable[s] %strings% in [redis] [channel] %string%");

            Skript.registerExpression(ExprChannel.class, String.class, ExpressionType.SIMPLE, "redis channel");
            EventValues.registerEventValue(RedisMessageEvent.class, String.class, new Getter<String, RedisMessageEvent>() {
                @Override
                public String get(RedisMessageEvent e) {
                    return e.getChannelName();
                }
            }, 0);
            Skript.registerExpression(ExprMessage.class, String.class, ExpressionType.SIMPLE, "redis message");
            EventValues.registerEventValue(RedisMessageEvent.class, String.class, new Getter<String, RedisMessageEvent>() {
                @Override
                public String get(RedisMessageEvent e) {
                    return e.getMessage();
                }
            }, 0);
            Skript.registerExpression(ExprMessageDate.class, Date.class, ExpressionType.SIMPLE, "redis message date");
            EventValues.registerEventValue(RedisMessageEvent.class, Date.class, new Getter<Date, RedisMessageEvent>() {
                @Override
                public Date get(RedisMessageEvent e) {
                    return new Date(e.getDate());
                }
            }, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SetRedisManager(RedisManager rm)
    {
        this.rm = rm;
    }
    @Override
    public void onEnable() {
        saveDefaultConfig();
        startRedis(false);
        getServer().getPluginCommand("reloadredis").setExecutor(new CommandReloadRedis(this));
        registerSyntax();
    }

    @Override
    public void onDisable() {
        if (rm != null) rm.shutdown();
    }

    public RedisManager getRedisManager() {
        return rm;
    }
}