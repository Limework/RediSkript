package net.limework.rediskript.skript.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import net.limework.rediskript.RediSkript;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;

public class EffSendMessage extends Effect {
    static {
        Skript.registerEffect(EffSendMessage.class, "send redis message[s] %strings% to [channel] %string%");
    }


    private Expression<String> channel;
    private Expression<String> message;


    @Override
    protected void execute(Event event) {

        RediSkript plugin = RediSkript.getAPI();

        String[] message = this.message.getAll(event);
        String channel = this.channel.getSingle(event);
        if (message[0] == null) {
            Bukkit.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "&2[&aRediSkript&a] &cRedis message was empty. Please check your code."));
            return;
        }
        if (channel == null) {
            Bukkit.getLogger().warning(ChatColor.translateAlternateColorCodes('&', "&2[&aRediSkript&a] &cChannel was empty. Please check your code."));
            return;
        }
        plugin.getRC().sendMessage(message, channel);
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