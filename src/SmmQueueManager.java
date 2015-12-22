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
    private static BotChannelThread botChannel;
    private static User queueManager;

    public static void main(String[] args) {
        channels = new LinkedList<>();
        queueManager = new User("userinfo");

        botChannel = new BotChannelThread(queueManager, channels);
        loadChannels();
        System.out.println("Data loaded.");

        try {
            while (on) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void loadChannels() {
        try {
            Scanner scan = new Scanner(new File("channels"));
            ChannelThread curr = null;
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.startsWith("#")) {
                    // update channel we are adding submission for
                    curr = new ChannelThread(queueManager, line, botChannel);
                    channels.add(curr);
                } else {
                    // add submission entry
                    String[] submission = line.split(" ");
                    curr.submissionQueue.offer(new Submission(submission[0], submission[1], Long.parseLong(submission[2])));
                }
            }
        } catch (FileNotFoundException ignored) {
        }
    }
}
