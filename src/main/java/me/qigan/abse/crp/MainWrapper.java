package me.qigan.abse.crp;

import me.qigan.abse.Holder;
import me.qigan.abse.InCmd;
import me.qigan.abse.Index;
import me.qigan.abse.PathCmd;
import me.qigan.abse.ant.LoginScreen;
import me.qigan.abse.config.ConfigManager;
import me.qigan.abse.config.KeybindManager;
import me.qigan.abse.config.MuConfig;
import me.qigan.abse.config.PositionConfig;
import me.qigan.abse.crp.ovr.CustomEntRender;
import me.qigan.abse.crp.ovr.MCMainMenu;
import me.qigan.abse.events.CoreEventProfiler;
import me.qigan.abse.fr.exc.*;
import me.qigan.abse.fr.mining.AutoMining;
import me.qigan.abse.mapping.Rooms;
import me.qigan.abse.mapping.mod.M7Route;
import me.qigan.abse.gui.inst.NewMainMenu;
import me.qigan.abse.gui.overlay.GuiNotifier;
import me.qigan.abse.gui.inst.LegacyGui;
import me.qigan.abse.mapping.MappingConstants;
import me.qigan.abse.mapping.MappingController;
import me.qigan.abse.pathing.MovementController;
import me.qigan.abse.mapping.routing.RouteUpdater;
import me.qigan.abse.events.PacketHandler;
import me.qigan.abse.sync.SoundUtils;
import me.qigan.abse.sync.Sync;
import me.qigan.abse.sync.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainWrapper {

    public static LoginScreen ls = new LoginScreen();
    public static Map<String, Runnable> linkedScripts = new HashMap<>();

    public static void setCustomEntRenderer() {
        Minecraft.getMinecraft().entityRenderer = new CustomEntRender(Minecraft.getMinecraft(), Minecraft.getMinecraft().getResourceManager());
    }

    public static void initialise(FMLInitializationEvent e) {

        setCustomEntRenderer();

        Utils.setupRoman();
        MappingConstants.setup();
        TagConstants.init();
        M7Route.setup();
        Rooms.setup();

        //ClientSync.active();
        MinecraftForge.EVENT_BUS.register(new MainWrapper());
        MinecraftForge.EVENT_BUS.register(new GuiNotifier());
        MinecraftForge.EVENT_BUS.register(new Sync());
        MinecraftForge.EVENT_BUS.register(new Index());
        MinecraftForge.EVENT_BUS.register(new MCMainMenu());
        MinecraftForge.EVENT_BUS.register(new SmoothAimControl());
        MinecraftForge.EVENT_BUS.register(new ClickSimTick());
        MinecraftForge.EVENT_BUS.register(new TickTasks());
        MinecraftForge.EVENT_BUS.register(new RouteUpdater());
        MinecraftForge.EVENT_BUS.register(new Alert());
        MinecraftForge.EVENT_BUS.register(new CoreEventProfiler());
        MinecraftForge.EVENT_BUS.register(new PhantomAim());
        //MinecraftForge.EVENT_BUS.register(new Mapping());


        ClientCommandHandler.instance.registerCommand(new InCmd());
        ClientCommandHandler.instance.registerCommand(new PathCmd());

        File file = new File(Loader.instance().getConfigDir() + "/abse/configs");
        if (!file.exists()) file.mkdirs();

        AutoMining.init();

        Holder.link();
        System.out.println("ABSE SOUND REG: " + SoundUtils.initialise() + " sounds registered.");

        Index.CFG_MANAGER = new ConfigManager("abse/configs");

        Index.MAIN_CFG = new MuConfig();
        Index.POS_CFG = new PositionConfig();
        Index.POS_CFG.load().defts(true).update();
        Index.MOVEMENT_CONTROLLER = new MovementController();
        MinecraftForge.EVENT_BUS.register(Index.MOVEMENT_CONTROLLER);
        Index.MAPPING_CONTROLLER = new MappingController();
        MinecraftForge.EVENT_BUS.register(Index.MAPPING_CONTROLLER);
        Index.KEY_MANAGER = new KeybindManager();
        MinecraftForge.EVENT_BUS.register(Index.KEY_MANAGER);
        Index.KEY_MANAGER.after();

//        int x0 = 0;
//        if (QGuiScreen.register(MainGui.class, new MainGui(0, null))) x0++;
//        if (QGuiScreen.register(PositionsGui.class, new PositionsGui(MainGui.class))) x0++;
//        System.out.println("QGuiScreen: Registered " + x0 + " screens.");
    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (Minecraft.getMinecraft().getCurrentServerData() == null) return;
        event.manager.channel().pipeline().addBefore("packet_handler", "abse_packet_handler", new PacketHandler());
    }

    @SubscribeEvent
    public void tick(InputEvent.KeyInputEvent e) {
        if (LegacyGui.queue) {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    Minecraft.getMinecraft().displayGuiScreen(new LegacyGui(0, null));
                }
            });
            LegacyGui.queue = false;
        }
        if (NewMainMenu.queue) {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    Minecraft.getMinecraft().displayGuiScreen(new NewMainMenu(null));
                }
            });
            NewMainMenu.queue = false;
        }
    }
}