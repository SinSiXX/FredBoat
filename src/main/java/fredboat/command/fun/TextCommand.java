package fredboat.command.fun;

import fredboat.commandmeta.Command;
import java.io.File;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class TextCommand extends Command {

    public String msg;

    public TextCommand(String msg) {
        this.msg = msg;
    }
    
    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        channel.sendMessage(msg);
    }
    
}
