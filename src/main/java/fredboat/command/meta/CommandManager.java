package fredboat.command.meta;

import fredboat.FredBoat;
import fredboat.util.TextUtils;
import java.util.HashMap;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class CommandManager {

    public static HashMap<String, ICommand> commands = new HashMap<>();
    public static ICommand defaultCmd = new UnknownCommand();
    public static int commandsExecuted = 0;

    public static void registerCommand(String name, ICommand cmd) {
        System.out.println("Registered new command " + name );
        commands.put(name, cmd);
    }
    
    public static void registerAlias(String aliasName, String cmdName) {
        System.out.println("Registered new alias " + aliasName + " for " + cmdName);
        commands.put(aliasName, commands.get(cmdName));
    }

    public static void prefixCalled(Guild guild, TextChannel channel, User invoker, Message message) {
        String[] args = message.getRawContent().replace("\n", " ").split(" ");
        ICommand invoked = commands.getOrDefault(args[0].substring(fredboat.FredBoat.PREFIX.length()), defaultCmd);
        commandsExecuted++;
        if (invoked instanceof ICommandOwnerRestricted) {
            //This command is restricted to only Frederikam
            //Check if invoker is actually Frederikam
            if(!invoker.getId().equals(FredBoat.OWNER_ID)){
                channel.sendMessage(TextUtils.prefaceWithMention(invoker, " you are not allowed to use that command!"));
                return;
            }
        }
        try {
            invoked.onInvoke(guild, channel, invoker, message, message.getContent().split(" "));
        } catch (Exception e) {
            MessageBuilder builder = new MessageBuilder();
            
            builder.appendMention(invoker);
            builder.appendString(" an error occured :anger: ```java\n"+e.toString()+"\n");
            
            //builder.appendString("```java\n");
            for(StackTraceElement ste : e.getStackTrace()){
                builder.appendString("\t"+ste.toString()+"\n");
                if("prefixCalled".equals(ste.getMethodName())){
                    break;
                }
            }
            builder.appendString("\t...```");
            
            channel.sendMessage(builder.build());
        }
        

    }

}
