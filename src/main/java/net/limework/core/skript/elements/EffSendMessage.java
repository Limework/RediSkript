package net.limework.core.skript.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.limework.core.RediSkript;
import net.limework.core.managers.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.json.JSONObject;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.nio.charset.StandardCharsets;

public class EffSendMessage extends Effect {
    static {
        Skript.registerEffect(EffSendMessage.class, "send redis message %string% to [channel] %string%");
    }


    private Expression<String> channel;
    private Expression<String> message;


    @Override
    protected void execute(Event event) {

        RediSkript plugin = (RediSkript) Bukkit.getPluginManager().getPlugin("RediSkript");

        String message = this.message.getSingle(event);
        String channel = this.channel.getSingle(event);

        if (message == null) {
            Bukkit.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "&2[&aRediSkript&a] &cRedis message was empty. Please check your code."));
            return;
        }
        if (channel == null) {
            Bukkit.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "&2[&aRediSkript&a] &cChannel was empty. Please check your code."));
            return;
        }
        assert plugin != null;
        JSONObject json = new JSONObject();
        json.put("Message", message);
        json.put("Type", "Skript");
        json.put("Date", System.currentTimeMillis()); //for unique string every time & PING calculations
        byte[] msg;
        RedisManager manager = plugin.getRm();

        if (manager.getEncryption().isEncryptionEnabled()) {
            msg = manager.getEncryption().encrypt(json.toString());
        } else {
            msg = json.toString().getBytes(StandardCharsets.UTF_8);
        }
        try {
            BinaryJedis j = manager.getJedisPool().getResource();

            j.publish(channel.getBytes(StandardCharsets.UTF_8), msg);
            j.close();
        } catch (JedisConnectionException exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public String toString(Event event, boolean debug) {
        return "send redis message " + message.toString(event, debug) + " to channel " + channel.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(final Expression<?>[] expressions, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
        this.message = (Expression<String>) expressions[0];
        this.channel = (Expression<String>) expressions[1];
        return true;
    }

}