package net.limework.rediskript.skript.elements;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.SerializedVariable;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.limework.rediskript.RediSkript;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.Base64;

public class ExprVariableInChannel extends SimpleExpression<Object> {
    private Expression<String> name;
    private Expression<String> channel;
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
        name = (Expression<String>) expressions[0];
        channel = (Expression<String>) expressions[1];
        return true;

    }

    @Override
    protected Object[] get(Event event) {
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Object> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "variable " + name.toString(event,debug) + " in redis channel " + channel.toString(event, debug);
    }
    @Override
    public void change(Event e, Object[] changer, Changer.ChangeMode mode) {
        RediSkript plugin = RediSkript.getPlugin(RediSkript.class);
        switch (mode) {
            case ADD:
            case SET:
            case REMOVE:
                SerializedVariable.Value serialized;
                String encoded;
                String[] values = new String[changer.length+1];
                for( int i = 0; i < changer.length; i++) {
                    if (changer[i] != null) {
                        serialized = Classes.serialize(changer[i]);
                        encoded = Base64.getEncoder().encodeToString(serialized.data);
                        encoded = serialized.type + "^" + encoded;
                        values[i] = encoded;
                    }
                }
                String operation = mode.toString();
                plugin.getRC().sendVariables(name.getAll(e), values, channel.getSingle(e), operation);
                break;
            case DELETE:
                plugin.getRC().sendVariables(name.getAll(e), null, channel.getSingle(e), "SET");
                break;
        }
    }
    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        //if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE)
        if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.SET)
            return CollectionUtils.array(Object.class);

        return null;
    }
}