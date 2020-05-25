package me.conflicted.staffauth.cmds;

import me.conflicted.staffauth.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import me.conflicted.staffauth.utils.*;
import com.mongodb.*;
import java.util.regex.*;
import java.util.*;

public class Register implements CommandExecutor
{
    private StaffAuth auth;
    private DBCollection emails;
    
    public Register() {
        this.auth = StaffAuth.getInstance();
        this.auth.getCommand("register").setExecutor((CommandExecutor)this);
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player)sender;
            if (p.hasPermission("staffauth.register")) {
                if (args.length == 0 || args.length > 1) {
                    p.sendMessage(this.auth.color("&cUsage: /register <email>"));
                    return false;
                }
                final String email = args[0];
                if (this.emailChecker(email) && !Mail.hasEmail(p.getUniqueId())) {
                    if (this.auth.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("file")) {
                        this.auth.getConfig().set("Players-Mail." + p.getUniqueId(), (Object)email);
                        this.auth.saveConfig();
                    }
                    else if (this.auth.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("database")) {
                        this.emails = this.auth.getMongo().getDatabase().getCollection("emails");
                        final BasicDBObject obj = new BasicDBObject();
                        ((HashMap<String, UUID>)obj).put("UUID", p.getUniqueId());
                        ((HashMap<String, String>)obj).put("email", email);
                        this.emails.insert(obj);
                    }
                    p.sendMessage(this.auth.color("&bRegistered with the following email: " + email));
                }
                else if (!this.emailChecker(email)) {
                    p.sendMessage(this.auth.color("&cInvalid email!"));
                }
                else if (Mail.hasEmail(p.getUniqueId())) {
                    p.sendMessage(this.auth.color("&cYou already have an email registered to this account, if you need to change it contact an administrator."));
                }
            }
            else {
                p.sendMessage(this.auth.color("&cNo permission!"));
            }
        }
        return false;
    }
    
    public boolean emailChecker(final String s) {
        final Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        final Matcher mat = pattern.matcher(s);
        return mat.matches();
    }
}
