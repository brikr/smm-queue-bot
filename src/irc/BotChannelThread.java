package irc;

import smm.Submission;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class BotChannelThread extends ChannelThread {

    private LinkedList<ChannelThread> channels;

    public BotChannelThread(User user, LinkedList<ChannelThread> channels) {
        super(user, "#" + user.name, null);
        this.channels = channels;
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        System.out.printf("<!%s> %s: %s\n", channel, sender, message);
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
                case "optin":
                    // check for presence
                    if(!this.containsChannel("#" + sender)) {
                        this.channels.add(new ChannelThread(this.user, "#" + sender, this));
                        this.checkpoint();
                    }
                    break;
                case "saveall":
                    this.checkpoint();
                    break;
            }
        }
    }

    private void checkpoint() {
        System.out.println("Saving all data...");
        // save channel names
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(new File("channels")));
            for(ChannelThread c : this.channels) {
                br.write(c.channel + "\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // save queues
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(new File("queues")));
            for(ChannelThread c : this.channels) {
                br.write(c.channel + "\n");
                for(Submission s : c.submissionQueue) {
                    br.write(s.user + " " + s.level + " " + s.time + "\n");
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean containsChannel(String channel) {
        for(ChannelThread c : this.channels) {
            if(c.channel.equals(channel)) return true;
        }
        return false;
    }
}
