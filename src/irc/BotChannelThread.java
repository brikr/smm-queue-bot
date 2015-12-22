package irc;

import java.util.LinkedList;

public class BotChannelThread extends ChannelThread {

    private LinkedList<ChannelThread> channels;

    public BotChannelThread(User user, LinkedList<ChannelThread> channels) {
        super(user, "#" + user.name, null);
        this.channels = channels;
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        System.out.print("!");
        super.onMessage(channel, sender, login, hostname, message);
    }
}
