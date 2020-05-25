package me.conflicted.staffauth.events;

import me.conflicted.staffauth.*;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import me.conflicted.staffauth.utils.*;
import org.bukkit.entity.*;
import com.mongodb.*;
import org.bukkit.event.player.*;

public class PlayerEvents implements Listener
{
    private StaffAuth auth;
    private DBCollection emails;
    
    public PlayerEvents() {
        this.auth = StaffAuth.getInstance();
        this.auth.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.auth);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(final PlayerCommandPreprocessEvent e) {
        if (this.auth.auth.contains(e.getPlayer().getUniqueId())) {
            if (e.getMessage().toLowerCase().startsWith("/login") || e.getMessage().toLowerCase().startsWith("/register")) {
                e.setCancelled(false);
            }
            else {
                e.getPlayer().sendMessage(this.auth.color("&cYou cannot use this command until you have logged in!"));
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("staffauth.auth")) {
            final Player p = e.getPlayer();
            this.auth.auth.add(p.getUniqueId());
            final String code = Code.getCode();
            if (Mail.hasEmail(p.getUniqueId())) {
                if (this.auth.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("file")) {
                    Mail.sendCode(this.auth.getConfig().getString("Players-Mail." + p.getUniqueId()), p.getName(), code);
                }
                else if (this.auth.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("database")) {
                    this.emails = this.auth.getMongo().getDatabase().getCollection("emails");
                    final BasicDBObject query = new BasicDBObject("UUID", p.getUniqueId().toString());
                    final DBCursor c = this.emails.find(query);
                    try {
                        if (c.hasNext()) {
                            String found = c.next().toString();
                            found = found.substring(found.indexOf("email"));
                            found = found.replace("\"", "");
                            found = found.replace("}", "");
                            found = found.replace("email", "");
                            found = found.replace(":", "");
                            found = found.replace(" ", "");
                            Mail.sendCode(found, p.getName(), code);
                        }
                    }
                    finally {
                        c.close();
                    }
                }
                this.auth.code.put(p.getUniqueId(), code);
                p.sendMessage(this.auth.color("&aHey, please login using /login <code>, where <code> is provide the code that was sent via email :)"));
            }
            else {
                p.sendMessage(this.auth.color("&cHey, please register using /register <email> then re-log:)"));
            }
        }
    }
    
    @EventHandler
    public void onMove(final PlayerMoveEvent e) {
        if (this.auth.auth.contains(e.getPlayer().getUniqueId())) {
            e.setTo(e.getFrom());
        }
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        if (this.auth.auth.contains(e.getPlayer().getUniqueId())) {
            this.auth.auth.remove(e.getPlayer().getUniqueId());
        }
    }
}
