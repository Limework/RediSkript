package net.limework.rediskript.managers;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import net.limework.rediskript.RediSkript;
import net.limework.rediskript.data.Encryption;
import net.limework.rediskript.events.RedisMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.scheduler.BukkitTask;
import org.cryptomator.siv.UnauthenticCiphertextException;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedisController extends BinaryJedisPubSub implements Runnable {


    //Jedis Pool to be used by every another class.
    private final JedisPool jedisPool;

    //this seems useless unless tls is OFF!

    // class author is govindas :/
    private final Encryption encryption;

    private byte[][] channelsInByte;

    private final AtomicBoolean isConnectionBroken;
    private final AtomicBoolean isConnecting;
    private final RediSkript plugin;
    private final BukkitTask ConnectionTask;


    public RedisController(RediSkript plugin) {
        this.plugin = plugin;
        Configuration config = plugin.getConfig();
        JedisPoolConfig JConfig = new JedisPoolConfig();
        int maxConnections = config.getInt("Redis.MaxConnections");

        //do not allow less than 2 max connections as that causes issues
        if (maxConnections < 2) { maxConnections = 2; }

        JConfig.setMaxTotal(maxConnections);
        JConfig.setMaxIdle(maxConnections);
        JConfig.setMinIdle(1);
        JConfig.setBlockWhenExhausted(true);
        this.jedisPool = new JedisPool(JConfig,
                config.getString("Redis.Host"),
                config.getInt("Redis.Port"),
                config.getInt("Redis.TimeOut"),
                config.getString("Redis.Password"),
                config.getBoolean("Redis.useTLS"));
        encryption = new Encryption(config.getBoolean("Redis.EncryptMessages"),
                config.getString("Redis.EncryptionKe"),
                config.getString("Redis.MacKey"));
        setupChannels(config);
        isConnectionBroken = new AtomicBoolean(true);
        isConnecting = new AtomicBoolean(false);
        //Start the main task on async thread
        ConnectionTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this, 0, 20 * 5);
    }

    @Override
    public void run() {
        if (!isConnectionBroken.get() || isConnecting.get()) {
            return;
        }
        plugin.sendLogs("Connecting to Redis server...");
        isConnecting.set(true);
        try (Jedis jedis = jedisPool.getResource()) {
            isConnectionBroken.set(false);
            plugin.sendLogs("&aConnection to Redis server has established! Success!");
            jedis.subscribe(this, channelsInByte);
        } catch (Exception e) {
            isConnecting.set(false);
            isConnectionBroken.set(true);
            plugin.sendErrorLogs("Connection to Redis server has failed! Please check your details in the configuration.");
            e.printStackTrace();
        }
    }

    public void shutdown() {
        ConnectionTask.cancel();
        if (this.isSubscribed()) {
            try {
                this.unsubscribe();
            } catch (Exception e) {
                plugin.sendErrorLogs("Something went wrong during unsubscribing...");
                e.printStackTrace();
            }
        }
        jedisPool.close();
    }

    @Override
    public void onMessage(byte[] channel, byte[] message){
            String channelString = new String(channel, StandardCharsets.UTF_8);
            String receivedMessage = null;
            try {
                //if encryption is enabled, decrypt the message, else just convert binary to string
                if (this.encryption.isEncryptionEnabled()) {
                    try {
                        receivedMessage = encryption.decrypt(message);
                    } catch (UnauthenticCiphertextException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                    }

                } else {
                    //encryption is disabled, so let's just get the string
                    receivedMessage = new String(message, StandardCharsets.UTF_8);
                }
                if (receivedMessage != null) {
                    JSONObject j = new JSONObject(receivedMessage);
                    if (j.get("Type").equals("Skript")) {
                        JSONArray messages = j.getJSONArray("Messages");
                        RedisMessageEvent event;
                        for (int i = 0; i < messages.length(); i++) {
                            event = new RedisMessageEvent(channelString, messages.get(i).toString(), j.getLong("Date"));
                            //if plugin is disabling, don't call events anymore
                            if (plugin.isEnabled()) {
                                RedisMessageEvent finalEvent = event;
                                Bukkit.getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(finalEvent));
                            }
                        }
                    } else if (j.get("Type").equals("SkriptVariables")) {

                        //Transfer variables between servers

                        JSONArray variableNames = j.getJSONArray("Names");
                        Object inputValue;
                        String changeValue = null;
                        JSONArray variableValues = null;
                        if (!j.isNull("Values")) {
                            variableValues = j.getJSONArray("Values");
                        }
                        for (int i = 0; i < variableNames.length(); i++) {

                            if (j.isNull("Values")) {
                                //only check for SET here, because null has to be ignored in all other cases

                                if (j.getString("Operation").equals("SET")) {
                                    Variables.setVariable(variableNames.get(i).toString(), null, null, false);
                                }

                            } else {
                                if (!variableValues.isNull(i)) {
                                    changeValue = variableValues.get(i).toString();
                                }
                                String[] inputs = changeValue.split("\\^", 2);
                                inputValue = Classes.deserialize(inputs[0], Base64.getDecoder().decode(inputs[1]));
                                switch (j.getString("Operation")) {
                                    case "ADD":
                                        //I will add this once someone tells me how to remove from Skript variable
                                        //because using SET operation has issues with inconvertible types (Double and Long)
                                        //variable = (Variable) Variables.getVariable(variableNames.get(i).toString(), null, false);
                                        // variable.change(null, (Object[]) inputValue, Changer.ChangeMode.REMOVE);
                                    case "REMOVE":
                                        //I will add this once someone tells me how to remove from Skript variable
                                        //because using SET operation has issues with inconvertible types (Double and Long)
                                        //variable = (Variable) Variables.getVariable(variableNames.get(i).toString(), null, false);
                                        // variable.change(null, (Object[]) inputValue, Changer.ChangeMode.REMOVE);
                                        break;
                                    case "SET":
                                        String variableName = variableNames.get(i).toString();
                                        
                                        //this is needed, because setting a {variable::*} causes weird behavior, like
                                        //1st set operation is no data, 2nd has data, etc.
                                        //if you set it to null before action, it works correctly

                                        if (variableName.charAt(variableName.length()-1) == '*') {
                                            Variables.setVariable(variableName, null, null, false);
                                       }
                                        Variables.setVariable(variableNames.get(i).toString(), inputValue, null, false);

                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.sendErrorLogs("&cI got a message that was empty from channel " + channelString + " please check your code that you used to send the message. Message content:");
                plugin.sendErrorLogs(receivedMessage);
                e.printStackTrace();
            }
        }

    public void sendMessage(String[] message, String channel) {
        JSONObject json = new JSONObject();
        json.put("Messages", new JSONArray(message));
        json.put("Type", "Skript");
        json.put("Date", System.currentTimeMillis()); //for unique string every time & PING calculations
        finishSendMessage(json, channel);
    }

    public void sendVariables(String[] variableNames, String[] variableValues, String channel, String operation) {
        JSONObject json = new JSONObject();
        json.put("Names", new JSONArray(variableNames));
        if (variableValues != null) {
            json.put("Values", new JSONArray(variableValues));
        }

        json.put("Type", "SkriptVariables");
        json.put("Date", System.currentTimeMillis()); //for unique string every time & PING calculations
        json.put("Operation", operation);
        finishSendMessage(json, channel);
    }

    public void finishSendMessage(JSONObject json, String channel) {
        try {
            byte[] message;
            if (encryption.isEncryptionEnabled()) {
                message = encryption.encrypt(json.toString());
            } else {
                message = json.toString().getBytes(StandardCharsets.UTF_8);
            }

            //sending a redis message blocks main thread if there's no more connections available
            //so to avoid issues, it's best to do it always on separate thread
            if (plugin.isEnabled()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try (BinaryJedis j = jedisPool.getResource()) {
                        j.publish(channel.getBytes(StandardCharsets.UTF_8), message);
                    } catch (Exception e) {
                        plugin.sendErrorLogs("Error sending redis message!");
                        e.printStackTrace();
                        }
                });
            } else {
                //execute sending of redis message on the main thread if plugin is disabling
                //so it can still process the sending
                try (BinaryJedis j = jedisPool.getResource()) {
                    j.publish(channel.getBytes(StandardCharsets.UTF_8), message);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (JedisConnectionException exception) {
            exception.printStackTrace();
        }
    }

    private void setupChannels(Configuration config) {
        List<String> channels = config.getStringList("Channels");
        channelsInByte = new byte[channels.size()][1];
        for (int x = 0; x < channels.size(); x++) {
            channelsInByte[x] = channels.get(x).getBytes(StandardCharsets.UTF_8);
        }
    }

    public Boolean isRedisConnectionOffline() {
        return isConnectionBroken.get();
    }

    // for skript reflect :)
    public JedisPool getJedisPool() {
        return jedisPool;
    }
    //
}
