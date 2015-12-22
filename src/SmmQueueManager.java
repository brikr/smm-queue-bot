import irc.ChannelThread;
import irc.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class SmmQueueManager {
    public static boolean on;
    private static LinkedList<ChannelThread> channels;

    public static void main(String[] args) {
        channels = new LinkedList<>();
        User queueManager = new User(args[0]);
        LinkedList<String> channelNames = loadChannels(args[1]);

        for(String c : channelNames) {
            ChannelThread channel = new ChannelThread(queueManager, c);
            channels.add(channel);
        }

        while(on) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static LinkedList<String> loadChannels(String filename) {
        LinkedList<String> channelNames = new LinkedList<>();

        try {
            Scanner scan = new Scanner(new File(filename));
            while(scan.hasNextLine()) {
                channelNames.add("#" + scan.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return channelNames;
    }
}
