package me.qigan.abse.fr.auto.routes;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ARRCmd extends CommandBase {
    @Override
    public String getCommandName() {
        return "arr";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/arr <args>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {

        } else {

        }
    }
}
