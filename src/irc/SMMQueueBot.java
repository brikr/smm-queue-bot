package irc;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import smm.Channel;
import smm.Submission;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class SMMQueueBot extends PircBot {

    private final User user;
    private final LinkedList<Channel> channels;
    private final MessageProcessor messageProcessor;

    public SMMQueueBot(User user) {
        this.user = user;
        this.channels = new LinkedList<>();
        this.setName(user.name);
        try {
            this.connect("irc.twitch.tv", 6667, this.user.Oauth);
        } catch (IOException | IrcException e) {
            e.printStackTrace();
        }

        this.loadChannels();

        Timer messageProcessorTimer = new Timer();
        messageProcessor = new MessageProcessor(this);
        messageProcessorTimer.scheduleAtFixedRate(messageProcessor, 0, 2000);
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        System.out.printf("<!%s> %s: %s\n", channel, sender, message);
        if (message.startsWith("!")) {
            String[] command = message.substring(1).split(" "); // strip ! and separate into parameters
            switch (command[0]) {
                case "submit":
                    this.submitLevel(channel, sender, command[1]);
                    break;
                case "next":
                    if (channel.equals("#" + sender)) {
                        this.nextLevel(channel);
                    }
                    break;
                case "print":
//                    System.out.println(submissionQueue);
                    break;
                case "optin":
                    // check for presence
                    if (channel.equals("#" + user.name)) {
                        this.addChannel(sender);
                    }
                    break;
                case "optout":
                    if (channel.equals("#" + user.name)) {
                        this.leaveChannel(sender);
                    }
                    break;
                case "saveall":
                    if (sender.equals("minikori")) {
                        this.checkpoint();
                    }
                    break;
            }
        }
    }

    private void addChannel(String channel, boolean checkpoint) {
        this.joinChannel(channel);
        this.channels.add(new Channel(channel));
        if (checkpoint) {
            this.checkpoint();
        }
    }

    private void addChannel(String channel) {
        this.addChannel(channel, true);
    }

    private void leaveChannel(String channel) {
        this.partChannel(channel);
        this.channels.remove(this.getChannel(channel));
        this.checkpoint();
    }

    private void nextLevel(String channel) {
        Channel c = this.getChannel(channel);
        if (c != null) {
            Submission s = c.getNextLevel();
            if (s != null) {
                this.send(channel, "Next level: " + s);
            } else {
                this.send(channel, "Queue is empty!");
            }
        }
    }

    private void send(String channel, String message) {
        this.messageProcessor.send(channel, message);
    }

    private Channel getChannel(String channel) {
        for (Channel c : this.channels) {
            if (c.channel.equals(channel)) return c;
        }
        return null;
    }

    private void submitLevel(String channel, String sender, String courseID) {
        Submission s = new Submission(sender, courseID);
        Channel c = this.getChannel(channel);
        if (c != null) {
            c.submitLevel(s);
        }
    }

    private void submitLevel(String channel, Submission submission) {
        Channel c = this.getChannel(channel);
        if (c != null) {
            c.submitLevel(submission);
        }
    }

    public synchronized void checkpoint() {
        System.out.println("Saving all data...");
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(new File("channels")));
            for (Channel c : this.channels) {
                br.write(c.channel + "\n");
                for (Submission s : c.submissionQueue) {
                    br.write(s.user + " " + s.level + " " + s.time + "\n");
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadChannels() {
        try {
            Scanner scan = new Scanner(new File("channels"));
            String channel = null;
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("#")) {
                    // update channel we are adding submission for
                    channel = line;
                    this.addChannel(line);
                } else {
                    // add submission entry
                    String[] submission = line.split(" ");
                    this.submitLevel(channel, new Submission(submission[0], submission[1], Long.parseLong(submission[2])));
                }
            }
        } catch (FileNotFoundException ignored) {
        }
    }
}

class MessageProcessor extends TimerTask {
    LinkedList<Message> messageQueue;
    SMMQueueBot SMMQueueBot;

    public MessageProcessor(SMMQueueBot SMMQueueBot) {
        this.SMMQueueBot = SMMQueueBot;

        this.messageQueue = new LinkedList<>();
    }

    public void send(String channel, String message) {
        this.messageQueue.offer(new Message(channel, message));
    }

    @Override
    public void run() {
        Message m = this.messageQueue.poll();
        if (m == null) return;
        this.SMMQueueBot.sendMessage(m.channel, m.text);
    }
}

class Message {
    String channel, text;

    public Message(String channel, String text) {
        this.channel = channel;
        this.text = text;
    }
}