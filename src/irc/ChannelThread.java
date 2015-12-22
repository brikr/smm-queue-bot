package irc;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import smm.Submission;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class ChannelThread extends PircBot {
    String channel;
    User user;

    LinkedList<Submission> submissionQueue = new LinkedList<>();

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
        if(message.startsWith("!")) {
            String[] command = message.substring(1).split(" "); // strip ! and separate into parameters
            switch(command[0]) {
                case "submit":
                    submissionQueue.offer(new Submission(sender, command[1])); // add submission to queue
                    break;
                case "print":
                    System.out.println(submissionQueue);
                    break;
            }
        }
    }
}
