package net.limework.rediskript.skript.elements;


import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import net.limework.rediskript.events.RedisMessageEvent;
import org.bukkit.event.Event;

public class ExprMessage extends SimpleExpression<String> {


    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "redis message";
    }

    @Override
    public boolean init(final Expression<?>[] expressions, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        if (!ScriptLoader.isCurrentEvent(RedisMessageEvent.class)) {
            Skript.error("Cannot use 'redis message' outside of a redis message event", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }


    @Override
    protected String[] get(Event e) {
        if (e instanceof RedisMessageEvent){
            return new String[]{((RedisMessageEvent) e).getMessage()};
        }
        return null;
    }
}
