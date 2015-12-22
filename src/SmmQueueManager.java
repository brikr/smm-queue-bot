import irc.BotChannelThread;
import irc.ChannelThread;
import irc.User;
import org.jibble.pircbot.PircBot;
import smm.Submission;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SmmQueueManager {
    public static boolean on;
    private static LinkedList<ChannelThread> channels;

    public static void main(String[] args) {
        channels = new LinkedList<>();
        User queueManager = new User("userinfo");
        LinkedList<String> channelNames = loadChannels();

        BotChannelThread botChannel = new BotChannelThread(queueManager, channels);

        channels.addAll(channelNames.stream()
                .map(c -> new ChannelThread(queueManager, c, botChannel))
                .collect(Collectors.toList()));

        loadQueues();

        try {
            while(on) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void loadQueues() {
        try {
            Scanner scan = new Scanner(new File("queues"));
            ChannelThread curr = channels.get(0);
            while(scan.hasNextLine()) {
                String line = scan.nextLine();
                if(line.startsWith("#")) {
                    // update channel we are adding submission for
                    curr = channels.stream()
                            .filter(c -> c.channel.equals(line))
                            .findAny()
                            .get();
                } else {
                    // add submission entry
                    String[] submission = line.split(" ");
                    curr.submissionQueue.offer(new Submission(submission[0], submission[1], Long.parseLong(submission[2])));
                }
            }
        } catch (FileNotFoundException e) {}
    }

    private static LinkedList<String> loadChannels() {
        LinkedList<String> channelNames = new LinkedList<>();

        try {
            Scanner scan = new Scanner(new File("channels"));
            while(scan.hasNextLine()) {
                channelNames.add(scan.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return channelNames;
    }
}
