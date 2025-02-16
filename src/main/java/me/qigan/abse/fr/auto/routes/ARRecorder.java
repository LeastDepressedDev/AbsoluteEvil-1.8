package me.qigan.abse.fr.auto.routes;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ARRecorder extends CommandBase {

    public static List<String> HELP_LINES = new ArrayList<>(Arrays.asList(
       "[Routes by qigan]",
            "/arr clear",
            "/arr begin <ref>",
            "/arr add <action>",
            "/arr load file"
    ));

    @Override
    public String getCommandName() {
        return "arr";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/arr <arg>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {

        } else {
            for (String s : HELP_LINES) {
                sender.addChatMessage(new ChatComponentText(s));
            }
        }
    }
}
