package irc;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

import java.io.IOException;

public class ChannelThread extends PircBot {
    String channel;
    User user;

    public ChannelThread(User user, String channel) {
        this.user = user;
        this.channel = channel;
//        this.setVerbose(true);
        try {
            this.setName(user.name);
            this.connect("irc.twitch.tv", 6667, user.Oauth);
            this.joinChannel(channel);
        } catch (IOException | IrcException e) {
            e.printStackTrace();
        }
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        System.out.printf("<%s> %s: %s\n", channel, sender, message);
    }
}
