package net.limework.rediskript.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public class RedisMessageEvent extends Event {
    private final static HandlerList HANDLERS = new HandlerList();

    private String channelName;
    private String message;
    private long date;

    public RedisMessageEvent(String channelName , String message, long date) {
        super(false);
        this.channelName = channelName;
        this.message = message;
        this.date = date;
    }


    @Override
    public String getEventName() {
        return super.getEventName();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public String getChannelName() {
        return channelName;
    }

    public long getDate() { return date;}

    public String getMessage() {
        return message;
    }
}
