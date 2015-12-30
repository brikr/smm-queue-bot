package irc;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.json.JSONArray;
import org.json.JSONObject;
import smm.Channel;
import smm.Submission;

import java.io.*;
import java.net.URL;
import java.util.*;

public class SMMQueueBot extends PircBot {

    private static final long MESSAGE_INTERVAL = 2000;      // 2 seconds
    private static final long CHECKPOINT_INTERVAL = 600000; // 10 minutes
    private static final long PRIORITY_INTERVAL = 300000;   // 5 minutes
    private static final long HELP_INTERVAL = 60000;        // 1 minute
    private long lastHelp = 0;
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
                    if(command.length > 2) {
                        this.submitLevel(channel, sender, command[1], (command[2].equals("present")));
                    } else {
                        this.submitLevel(channel, sender, command[1], false);
                    }
                    break;
                case "next":
                    if (channel.equals("#" + sender)) {
                        Submission s = this.nextLevel(channel);
                        if (s != null) {
                            this.send(channel, "Next level: " + s);
                        } else {
                            this.send(channel, "No available levels!");
                        }
                    }
                    break;
                case "optin":
                    // check for presence
                    if (channel.equals("#" + this.name)) {
                        if (this.addChannel("#" + sender)) {
                            this.send(channel, sender + ", I'm now processing queues in your channel. Type !optout to cancel at any time.");
                        } else {
                            this.send(channel, sender + ", you are already opted in!");
                        }
                    }
                    break;
                case "optout":
                    if (channel.equals("#" + this.name)) {
                        if (this.leaveChannel("#" + sender)) {
                            this.send(channel, sender + ", I'm no longer processing queues in your channel. Type !optin to rejoin.");
                        } else {
                            this.send(channel, sender + ", you are already opted out!");
                        }
                    }
                    break;
                case "saveall":
                    if (sender.equals("minikori")) {
                        this.send(channel, "Saving queues...");
                        this.checkpoint();
                        this.send(channel, "Queues saved.");
                    }
                    break;
                case "help":
                    long now = System.currentTimeMillis();
                    if (now - this.lastHelp > HELP_INTERVAL) {
                        this.send(channel, "I'm SMMQueueBot by minikori. " +
                                "Viewers, use !submit <level code> to submit a level. " +
                                "Streamers, use !next to pull the next level from the queue. " + "" +
                                "If you want to use the bot in your channel, go to twitch.tv/" + this.name + " and type !optin in the chat. " +
                                "To follow my development or see more commands, checkout github.com/brikr/smm-queue-bot or follow @_minikori on Twitter.");
                    }
                    this.lastHelp = now;
                    break;
            }
        }
    }

    private boolean addChannel(String channel, boolean checkpoint) {
        if (this.getChannel(channel) != null) return false;
        this.joinChannel(channel);
        this.channels.add(new Channel(channel));
        if (checkpoint) {
            this.checkpoint();
        }
        return true;
    }

    private boolean addChannel(String channel) {
        return this.addChannel(channel, true);
    }

    private boolean leaveChannel(String channel) {
        if (this.getChannel(channel) == null) return false;
        this.partChannel(channel);
        this.channels.remove(this.getChannel(channel));
        this.checkpoint();
        return true;
    }

    private Submission nextLevel(String channel) {
        Channel c = this.getChannel(channel);
        if (c != null) {
            return c.getNextLevel(this.getViewers(channel));
        }
        return null;
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

    private void submitLevel(String channel, String sender, String courseID, boolean present) {
        Submission s = new Submission(sender, courseID, present);
        this.submitLevel(channel, s);
    }

    private void submitLevel(String channel, Submission submission) {
        Channel c = this.getChannel(channel);
        if (c != null && submission.id != null) {
            c.submitLevel(submission);
        }
    }

    private HashSet<String> getViewers(String channel) {
        HashSet<String> rval = new HashSet<>();
        try {
            String raw = new Scanner(new URL("http://tmi.twitch.tv/group/user/" + channel.substring(1) + "/chatters").openStream(), "UTF-8").useDelimiter("\\A").next();
            JSONObject json = new JSONObject(raw);
            JSONArray viewers = json.getJSONObject("chatters").getJSONArray("viewers");
            for(Object v : viewers) {
                rval.add((String) v);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rval;
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
                    br.write(s.submitter + " " + s.id + " " + s.time + " " +  s.present + "\n");
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
                    this.submitLevel(channel, new Submission(submission[0], submission[1], Long.parseLong(submission[2]), submission[3].equals("true")));
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
        this.smmQueueBot.sendMessage(m.channel, m.message);
        System.out.printf("<!%s> %s: %s\n", m.channel, smmQueueBot.getName(), m.message);
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
    String channel, message;

    public Message(String channel, String text) {
        this.channel = channel;
        this.message = text;
    }
}