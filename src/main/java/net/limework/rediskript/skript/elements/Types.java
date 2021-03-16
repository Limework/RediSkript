package net.limework.rediskript.skript.elements;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.registrations.Classes;
import redis.clients.jedis.Jedis;

public class Types {
    static {
        Classes.registerClass(
                new ClassInfo<>(Jedis.class, "jedis")
                        .user("JedisInstances?")
                        .parser(new Parser<Jedis>() {
            @Override
            public String toString(Jedis t, int i) {
                return t.toString();
            }

            @Override
            public String toVariableNameString(Jedis t) {
                return t.toString();
            }

            @Override
            public String getVariableNamePattern() {
                return "jedis:+";
            }
        }));

    }


}
