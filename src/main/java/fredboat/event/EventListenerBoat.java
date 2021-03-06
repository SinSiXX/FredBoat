/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fredboat.event;

import fredboat.command.fun.TalkCommand;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.CommandManager;
import fredboat.commandmeta.CommandRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.ReconnectedEvent;
import net.dv8tion.jda.events.audio.AudioConnectEvent;
import net.dv8tion.jda.events.message.MessageDeleteEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import fredboat.FredBoat;
import static fredboat.FredBoat.jdaBot;
import java.util.regex.Matcher;

public class EventListenerBoat extends ListenerAdapter {

    public static HashMap<String, Message> messagesToDeleteIfIdDeleted = new HashMap<>();
    public static HashMap<VoiceChannel, Runnable> toRunOnConnectingToVoice = new HashMap<>();
    public User lastUserToReceiveHelp;
    public final int scope;
    public final String prefix;
    private final Pattern commandNamePrefix;

    public EventListenerBoat(int scope, String prefix) {
        this.scope = scope;
        this.prefix = prefix;
        this.commandNamePrefix = Pattern.compile("(\\w+)");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getPrivateChannel() != null) {
            System.out.println("PRIVATE" + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
            return;
        }

        if (event.getAuthor().getUsername().equals(event.getJDA().getSelfInfo().getUsername())) {
            System.out.println(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
            return;
        }

        if (event.getMessage().getContent().length() < prefix.length()) {
            return;
        }

        if (event.getMessage().getContent().endsWith("(╯°□°）╯︵ ┻━┻")) {
            tableflip(event);
            return;
        }

        if (event.getMessage().getContent().substring(0, prefix.length()).equals(prefix)) {
            String cmdName;
            Command invoked = null;
            try {
                System.out.println(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
                Matcher matcher = commandNamePrefix.matcher(event.getMessage().getContent());
                matcher.find();

                invoked = CommandRegistry.getCommandFromScope(scope, matcher.group()).command;
            } catch (NullPointerException ex) {

            }

            if (invoked == null) {
                return;
            }

            CommandManager.prefixCalled(invoked, event.getGuild(), event.getTextChannel(), event.getAuthor(), event.getMessage());
        } else if (FredBoat.myUser != null && event.getMessage().getRawContent().startsWith("<@" + FredBoat.myUser.getId() + ">")) {
            System.out.println(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
            CommandManager.commandsExecuted++;
            TalkCommand.talk(event.getAuthor(), event.getTextChannel(), event.getMessage().getRawContent().substring(FredBoat.myUser.getAsMention().length() + 1));
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (messagesToDeleteIfIdDeleted.containsKey(event.getMessageId())) {
            Message msg = messagesToDeleteIfIdDeleted.remove(event.getMessageId());
            if (msg.getJDA() == jdaBot) {
                msg.deleteMessage();
            }
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        //Ignore self
        if (event.getAuthor().getUsername().equals(event.getJDA().getSelfInfo().getUsername())) {
            return;
        }

        //Ignore invites (handled elsewhere)
        if (event.getMessage().getContent().contains("discord.gg")) {
            return;
        }

        if (event.getAuthor() == lastUserToReceiveHelp) {
            //Ignore, just got help!
            return;
        }

        event.getChannel().sendMessage(FredBoat.helpMsg);
        lastUserToReceiveHelp = event.getAuthor();
    }

    @Override
    public void onInviteReceived(InviteReceivedEvent event) {
        if (event.getMessage().isPrivate()) {
            event.getAuthor().getPrivateChannel().sendMessage("Sorry! Since the release of the official API, registered bots must now be invited by someone with Manage **Server permissions**. If you have permissions, you can invite me at:\n"
                    + "https://discordapp.com/oauth2/authorize?&client_id=" + FredBoat.CLIENT_ID + "&scope=bot");
            /*
            //System.out.println(event.getInvite().getUrl());
            //InviteUtil.join(event.getInvite(), FredBoat.jda);
            Guild guild = null;
            try {
                guild = FredBoat.jda.getGuildById(event.getInvite().getGuildId());
            } catch (NullPointerException ex) {
                event.getAuthor().getPrivateChannel().sendMessage("That invite is not valid!");
                return;
            }

            boolean isNotInGuild = true;

            if (isNotInGuild) {
                event.getAuthor().getPrivateChannel().sendMessage("Invite accepted!");
                InviteUtil.join(event.getInvite(), FredBoat.jda, null);
            } else {
                event.getAuthor().getPrivateChannel().sendMessage("Already in that channel!");
            }
             */
        }
    }

    public HashMap<String, ArrayList<Integer>> recentTableFlips = new HashMap<>();

    public void pruneRecentTableflips(Guild guild, int seconds) {
        ArrayList<Integer> recent = recentTableFlips.containsKey(guild.getId()) ? recentTableFlips.get(guild.getId()) : new ArrayList<>();
        for (int time : recent) {

        }
    }

    public void getRecentTableflips(Guild guild, int seconds) {

    }

    public void tableflip(MessageReceivedEvent event) {
        //System.out.println(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
        //event.getChannel().sendMessage("┬─┬﻿ ノ( ゜-゜ノ)");
    }

    @Override
    public void onReady(ReadyEvent event) {
        FredBoat.init();
        jdaBot.getAccountManager().setGame("Say ;;help");
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        jdaBot.getAccountManager().setGame("Say ;;help");
    }

    public static Runnable onUnrequestedConnection = new Runnable() {
        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    @Override
    public void onAudioConnect(AudioConnectEvent event) {
        Runnable run = toRunOnConnectingToVoice.getOrDefault(event.getConnectedChannel(), onUnrequestedConnection);
        run.run();
    }

}
