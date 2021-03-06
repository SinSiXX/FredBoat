package fredboat.command.util;

import com.mashape.unirest.http.exceptions.UnirestException;
import fredboat.commandmeta.Command;
import fredboat.util.TextUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.Message.Attachment;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class DumpCommand extends Command {

    public static final int MAX_DUMP_SIZE = 2000;

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        //Interpret arguments
        boolean isQuiet = args[1].equals("-q");
        int dumpSize = Integer.valueOf(args[args.length - 1]);
        int realDumpSize = Math.min(dumpSize, MAX_DUMP_SIZE);
        MessageChannel outputChannel = isQuiet ? invoker.getPrivateChannel() : channel;
        outputChannel.sendTyping();
        
        //Quick hack to allow infinite messages if invoked by owner:
        if(invoker.getId().equals(fredboat.FredBoat.OWNER_ID)){
            realDumpSize = dumpSize;
        }
        
        try {

            MessageHistory mh = new MessageHistory(channel);
            int availableMessages = mh.getRecent().size();

            while (availableMessages < realDumpSize) {
                int nextMessages = Math.min(100, realDumpSize - availableMessages);
                availableMessages = nextMessages + availableMessages;
                mh.retrieve(nextMessages);
            }

            String dump = "**------BEGIN DUMP------**\n";
            List<Message> messages = new ArrayList<>(mh.getRecent());
            Collections.reverse(messages);
            messages = messages.subList(0, Math.min(realDumpSize, messages.size()));
            dump = dump + "Size = " + messages.size() + "\nTimes are in UTC\n\n";

            int i = 1;
            for (Message msg : messages) {
                String authr = "[UNKNOWN USER]";
                String time = "[UNKNOWN TIME]";
                String content = "[COULD NOT DISPLAY CONTENT!]";

                try {
                    authr = msg.getAuthor().getUsername() + "#" + msg.getAuthor().getDiscriminator();
                } catch (NullPointerException ex) {
                }
                
                try {
                    time = formatTimestamp(msg.getTime());
                } catch (NullPointerException ex) {
                }
                
                try {
                    content = msg.getContent();
                } catch (NullPointerException ex) {
                }

                dump = dump + "--Msg #" + i + " by " + authr
                        + " at " + time + "--\n" + content + "\n";
                if (msg.getAttachments().size() > 0) {
                    dump = dump + "Attachments:\n";
                    int j = 1;
                    for (Attachment attach : msg.getAttachments()) {
                        dump = dump + "[" + j + "] " + attach.getUrl();
                    }
                }
                dump = dump + "\n\n";
                i++;
            }
            dump = dump + "**------END DUMP------**\n";

            MessageBuilder mb = new MessageBuilder();
            mb.appendString("Successfully found and dumped `" + messages.size() + "` messages.\n");
            mb.appendString(TextUtils.postToHastebin(dump, true) + ".txt\n");
            if (!isQuiet) {
                mb.appendString("Hint: You can call this with `-q` to instead get the dump in a DM\n");
            }
            outputChannel.sendMessage(mb.build());
        } catch (UnirestException ex) {
            outputChannel.sendMessage("Failed to connect to Hastebin: " + ex.getMessage());
        }
    }

    public String formatTimestamp(OffsetDateTime t) {
        String str;
        if (LocalDateTime.now(Clock.systemUTC()).getDayOfYear() != t.getDayOfYear()) {
            str = "[" + t.getMonth().name().substring(0, 3).toLowerCase() + " " + t.getDayOfMonth() + " " + forceTwoDigits(t.getHour()) + ":" + forceTwoDigits(t.getMinute()) + "]";
        } else {
            str = "[" + forceTwoDigits(t.getHour()) + ":" + forceTwoDigits(t.getMinute()) + "]";
        }
        return str;
    }

    private String forceTwoDigits(int i) {
        String str = String.valueOf(i);

        if (str.length() == 1) {
            str = "0" + str;
        }

        return str;
    }

}
