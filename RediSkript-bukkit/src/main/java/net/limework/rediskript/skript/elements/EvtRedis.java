package net.limework.rediskript.skript.elements;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import net.limework.rediskript.events.RedisMessageEvent;
import org.bukkit.event.Event;

public class EvtRedis extends SkriptEvent {

    @Override
    public boolean init(final Literal<?>[] literals, final int i, final SkriptParser.ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(Event event) {
        return (event instanceof RedisMessageEvent);
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "redis message";
    }
}
