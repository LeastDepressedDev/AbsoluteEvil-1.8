package me.qigan.abse;

import me.qigan.abse.config.ConfigManager;
import me.qigan.abse.config.KeybindManager;
import me.qigan.abse.config.MuConfig;
import me.qigan.abse.config.PositionConfig;
import me.qigan.abse.crp.MainWrapper;
import me.qigan.abse.fr.other.BWTeamTracker;
import me.qigan.abse.fr.other.BowPracticeMod;
import me.qigan.abse.fr.other.FireballDetector;
import me.qigan.abse.mapping.MappingController;
import me.qigan.abse.pathing.MovementController;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mod(modid = Index.MODID, version = Index.VERSION)
public class Index
{
    public static MuConfig MAIN_CFG;
    public static ConfigManager CFG_MANAGER;
    public static KeybindManager KEY_MANAGER;
    public static MovementController MOVEMENT_CONTROLLER;
    public static MappingController MAPPING_CONTROLLER;
    public static PositionConfig POS_CFG;

    public static final String MODID = "abse";
    public static final String VERSION = "2.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MainWrapper.initialise(event);


    }

    public static void relog(String username, String token, String pid) {
        Session session = new Session(username, pid, token, "mojang");
        Field sessionField = ReflectionHelper.findField(Minecraft.class, "session", "field_71449_j");
        ReflectionHelper.setPrivateValue(Field.class, sessionField, sessionField.getModifiers() & ~Modifier.FINAL, "modifiers");
        ReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getMinecraft(), session, "session", "field_71449_j");
    }

    public static void absoluteFix() {
        FireballDetector.scan.clear();
        BWTeamTracker.team.clear();
        BowPracticeMod.tracking.clear();
        MainWrapper.setCustomEntRenderer();

        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText("\u00A76Absolute fix applied!"));
    }



    public static boolean isSafe() {
        return Index.MAIN_CFG.getBoolVal("safe_mod");
    }
}
