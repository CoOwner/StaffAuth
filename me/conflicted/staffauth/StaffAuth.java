package me.conflicted.staffauth;

import org.bukkit.plugin.java.*;
import me.conflicted.staffauth.Mongo.*;
import me.conflicted.staffauth.events.*;
import me.conflicted.staffauth.cmds.*;
import java.util.*;
import me.conflicted.staffauth.utils.*;
import java.io.*;
import org.bukkit.*;

public class StaffAuth extends JavaPlugin
{
    public static StaffAuth instance;
    private String version;
    private Credentials creds;
    public Mongo mongo;
    private Login login;
    private Register register;
    private PlayerEvents playerEvents;
    private ResetMail resetMail;
    public ArrayList<UUID> auth;
    public HashMap<UUID, String> code;
    
    public StaffAuth() {
        this.auth = new ArrayList<UUID>();
        this.code = new HashMap<UUID, String>();
    }
    
    public void onEnable() {
        StaffAuth.instance = this;
        if (this.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("database")) {
            try {
                this.creds = new Credentials();
                (this.mongo = new Mongo(this.creds.user, this.creds.pass, this.creds.db, this.creds.ip, this.creds.port)).setDatabase(this.creds.db);
                System.out.println("Successfully connected to mongo.");
            }
            catch (Exception e2) {
                System.out.println("Error! Failed to connect to mongo.");
                this.mongo.close();
            }
        }
        this.registerConfig();
        this.registerListeners();
        this.registerCmds();
        this.setVersion(this.getServer().getPluginManager().getPlugin("StaffAuth").getDescription().getVersion());
        if (this.version()) {
            try {
                System.out.println("This version of StaffAuth is not up to date, your version is " + this.getVersion() + " the latest is " + URLConnectionReader.get("https://pastebin.com/raw/WBtLmrrq/"));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void onDisable() {
        if (this.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("database")) {
            this.mongo.close();
        }
    }
    
    private boolean version() {
        try {
            if (URLConnectionReader.get("https://pastebin.com/raw/WBtLmrrq/") != this.getVersion()) {
                return false;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    private void registerConfig() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }
    
    private void registerListeners() {
        this.playerEvents = new PlayerEvents();
    }
    
    private void registerCmds() {
        this.login = new Login();
        this.register = new Register();
        this.resetMail = new ResetMail();
    }
    
    private void setVersion(final String string) {
        this.version = string;
    }
    
    private String getVersion() {
        return this.version;
    }
    
    public String color(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    
    public static StaffAuth getInstance() {
        return StaffAuth.instance;
    }
    
    public Mongo getMongo() {
        return this.mongo;
    }
}
