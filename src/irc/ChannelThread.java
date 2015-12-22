package irc;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import smm.Submission;

import java.io.IOException;
import java.util.LinkedList;

public class ChannelThread extends PircBot {
    public String channel;
    User user;
    long lastSent;
    private BotChannelThread botChannel;

    public LinkedList<Submission> submissionQueue = new LinkedList<>();

    public ChannelThread(User user, String channel, BotChannelThread botChannel) {
        this.user = user;
        this.channel = channel;
        this.botChannel = botChannel;
        this.lastSent = 0;
//        this.setVerbose(true);

        try {
            this.setName(user.name);
            this.connect("irc.twitch.tv", 6667, user.Oauth);
            System.out.println("Joining channel " + this.channel);
            this.joinChannel(this.channel);
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
                    System.out.println("Received submission " + command[1] + " from " + sender);
                    submissionQueue.offer(new Submission(sender, command[1])); // add submission to queue
                    break;
                case "print":
                    System.out.println(submissionQueue);
                    break;
                case "next":
                    System.out.println("Next level requested by " + sender + " in channel " + channel);
                    // check for owner
                    if(sender.toLowerCase().equals(channel.substring(1).toLowerCase())) {
                        Submission next = submissionQueue.poll();
                        if (next != null) {
                            this.send("Next level: " + next);
                        } else {
                            this.send("No more levels in queue!");
                        }
                    }
                    break;
            }
        }
    }

    private void send(String message) {
        long now = System.currentTimeMillis();

        if(now - this.lastSent > 2000) {
            this.sendMessage(this.channel, message + "\n");
            this.lastSent = now;
        } else {
            System.out.println("Eating message: " + message);
        }
    }

    @Override
    public boolean equals(Object o) {
        return this.channel.equals(((ChannelThread) o).channel);
    }
}
