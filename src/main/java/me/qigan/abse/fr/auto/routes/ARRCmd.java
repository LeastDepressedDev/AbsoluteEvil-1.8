package me.qigan.abse.fr.auto.routes;

import me.qigan.abse.Index;
import me.qigan.abse.fr.auto.routes.elems.*;
import me.qigan.abse.sync.CommandRoute;
import me.qigan.abse.sync.GenCommandDispatcher;
import me.qigan.abse.sync.Sync;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.Loader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

public class ARRCmd extends GenCommandDispatcher {

    public static ARoute route;

    public ARRCmd() {
        super("arr");
    }

    @CommandRoute(route = "/")
    public void def(String[] args) {
        Sync.player().addChatMessage(new ChatComponentText("\u00A7aQigan auto routes. Type /arr help for help."));
    }

    @CommandRoute(route = "/clear")
    public void clearRoute(String[] args) {
        route.clear();
    }

    @CommandRoute(route = "/begin")
    public void beginRoute(String[] args) {
        try {
            ARoute.Referer ref = ARoute.Referer.valueOf(args[0]);
            route = new ARoute(ref, args.length > 1 ? Integer.parseInt(args[1]) : 0, Sync.player().getPositionVector());
            route.add(new ARENull(route.startingPos));
        } catch (IllegalArgumentException ex) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7cIllegal referrer type. \u00A76Allowed type: [general, dungeon, floor]"));
        }
    }

    @CommandRoute(route = "/save")
    public void saveRoute(String[] args) {
        if (args.length > 0) {
            File file = new File(ARController.URL+"/"+args[0]+".json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.flush();
                writer.write(route.saveObj().toString());
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7cRoute name required as first argument."));
        }
    }

    @CommandRoute(route = "/help")
    public void helpCmd(String[] args) {
        Sync.player().addChatMessage(new ChatComponentText("\u00A7aThere will be help."));
    }


    /*

        Route parts



     */

    @CommandRoute(route = "/add")
    public void addRoutePart(String[] args) {

    }

    @CommandRoute(route = "/undo")
    public void addRouteUndo(String[] args) {
        route.elems.remove(route.elems.size()-1);
        Sync.player().addChatMessage(new ChatComponentText("\u00A7aUndo."));
    }

    @CommandRoute(route = "/add/walk")
    public void addRouteWalk(String[] args) {
        ARElement pre = route.elems.get(route.elems.size()-1);
        boolean skip = false, jump = false, sprint = true;
        for (String str : args) {
            if (str.equalsIgnoreCase("+skip")) skip = true;
            else if (str.equalsIgnoreCase("+jump")) jump = true;
            else if (str.equalsIgnoreCase("-sprint")) sprint = false;
        }
        ARWalk ele = new ARWalk(pre.endPos, Sync.player().getPositionVector(), jump, sprint);
        ele.skip = skip;
        route.add(ele);
        Sync.player().addChatMessage(new ChatComponentText("\u00A7aAdded walk."));
    }

    @CommandRoute(route = "/add/wait")
    public void addRouteWait(String[] args) {
        if (args.length > 0) {
            ARWait ele = new ARWait(Sync.player().getPositionVector(), Long.parseLong(args[0]));
            for (String str : args) {
                if (str.equalsIgnoreCase("+skip")) ele.skip = true;
            }
            route.add(ele);
            Sync.player().addChatMessage(new ChatComponentText(String.format("\u00A7aAdded delay %d.", Long.parseLong(args[0]))));
        } else {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7cDelay required."));
        }
    }

    @CommandRoute(route = "/add/click")
    public void addRouteClick(String[] args) {
        MovingObjectPosition mpos = Minecraft.getMinecraft().objectMouseOver;
        if (mpos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mpos.getBlockPos() == null) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7cYou must be looking on block."));
            return;
        }
        ARClick ele = new ARClick(Sync.player().getPositionVector(), mpos.getBlockPos(), args.length > 0 ? Boolean.parseBoolean(args[0]) : true);
        for (String str : args) {
            if (str.equalsIgnoreCase("+skip")) ele.skip = true;
        }
        route.add(ele);
        Sync.player().addChatMessage(new ChatComponentText(String.format("\u00A7aAdded click (%d, %d, %d).",
                mpos.getBlockPos().getX(), mpos.getBlockPos().getY(), mpos.getBlockPos().getZ())));
    }

    @CommandRoute(route = "/add/ewp")
    public void addRouteEwp(String[] args) {
        MovingObjectPosition mpos = Sync.player().rayTrace(61, 1f);
        if (mpos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mpos.getBlockPos() == null) {
            Sync.player().addChatMessage(new ChatComponentText("\u00A7cYou must be looking on block."));
            return;
        }
        BlockPos posUp = mpos.getBlockPos().add(0, 1, 0);
        ARWarp ele = new ARWarp(Sync.player().getPositionVector(), new Vec3(posUp.getX()+0.5, posUp.getY(), posUp.getZ()+0.5),
                new Float[]{Sync.player().rotationYaw, Sync.player().rotationPitch});
        for (String str : args) {
            if (str.equalsIgnoreCase("+skip")) ele.skip = true;
        }
        route.add(ele);
        Sync.player().addChatMessage(new ChatComponentText("\u00A7aWarp point set."));
    }

    @Override
    protected Method[] methods() {
        return this.getClass().getMethods();
    }
}
