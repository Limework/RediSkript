package net.limework.rediskript.skript.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import redis.clients.jedis.Jedis;

@SuppressWarnings("unchecked")
public class ExprJedis extends SimpleExpression<Jedis> {

    static {

    }

    private Jedis jedis;


    @Nullable
    @Override
    protected Jedis[] get(Event event) {
        return new Jedis[]{jedis};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Jedis> getReturnType() {
        return Jedis.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        return false;
    }
}
