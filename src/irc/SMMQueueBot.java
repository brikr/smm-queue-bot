package irc;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import smm.Channel;
import smm.Submission;

import java.io.*;
import java.util.*;

public class SMMQueueBot extends PircBot {

    private static final long MESSAGE_INTERVAL = 2000;
    private static final long CHECKPOINT_INTERVAL = 600000;
    private static final long PRIORITY_INTERVAL = 300000;
    private final String name;
    private final LinkedList<Channel> channels;
    private final MessageProcessor messageProcessor;

    public SMMQueueBot(String name, String Oauth) {
        this.name = name;
        this.channels = new LinkedList<>();

        this.setName(this.name);
        try {
            this.connect("irc.twitch.tv", 6667, Oauth);
        } catch (IOException | IrcException e) {
            e.printStackTrace();
        }

        this.loadChannels();

        Timer upkeepTimer = new Timer();
        messageProcessor = new MessageProcessor(this);
        upkeepTimer.scheduleAtFixedRate(messageProcessor, 0, MESSAGE_INTERVAL); // process message queue every 2 seconds
        upkeepTimer.scheduleAtFixedRate(new AutoCheckpointer(this), (CHECKPOINT_INTERVAL + PRIORITY_INTERVAL) / 2, CHECKPOINT_INTERVAL); // auto checkpoint every 10 minutes
        upkeepTimer.scheduleAtFixedRate(new PriorityManager(this), PRIORITY_INTERVAL, PRIORITY_INTERVAL); // update priorities every 5 minutes
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
                    if (channel.equals("#" + this.name)) {
                        this.addChannel(sender);
                    }
                    break;
                case "optout":
                    if (channel.equals("#" + this.name)) {
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

    private HashSet<String> getViewers(String channel) {
        User[] users = this.getUsers(channel);
        HashSet<String> viewers = new HashSet<>();

        for (User user : users) {
            viewers.add(user.getNick());
        }

        return viewers;
    }

    public void updatePriorities() {
        for (Channel channel : this.channels) {
            HashSet<String> viewers = this.getViewers(channel.channel);
            channel.updatePriorities(viewers, PRIORITY_INTERVAL);
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
        this.addChannel("#" + this.name, false);
        try {
            Scanner scan = new Scanner(new File("channels"));
            String channel = null;
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("#")) {
                    // update channel we are adding submission for
                    channel = line;
                    this.addChannel(line, false);
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
    SMMQueueBot smmQueueBot;

    public MessageProcessor(SMMQueueBot SMMQueueBot) {
        this.smmQueueBot = SMMQueueBot;

        this.messageQueue = new LinkedList<>();
    }

    public void send(String channel, String message) {
        this.messageQueue.offer(new Message(channel, message));
    }

    @Override
    public void run() {
        Message m = this.messageQueue.poll();
        if (m == null) return;
        this.smmQueueBot.sendMessage(m.channel, m.text);
    }
}

class AutoCheckpointer extends TimerTask {
    SMMQueueBot smmQueueBot;

    public AutoCheckpointer(SMMQueueBot smmQueueBot) {
        this.smmQueueBot = smmQueueBot;
    }

    @Override
    public void run() {
        smmQueueBot.checkpoint();
    }
}

class PriorityManager extends TimerTask {
    SMMQueueBot smmQueueBot;

    public PriorityManager(SMMQueueBot smmQueueBot) {
        this.smmQueueBot = smmQueueBot;
    }

    @Override
    public void run() {
        smmQueueBot.updatePriorities();
    }
}

class Message {
    String channel, text;

    public Message(String channel, String text) {
        this.channel = channel;
        this.text = text;
    }
}