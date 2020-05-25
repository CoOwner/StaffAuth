package me.conflicted.staffauth.cmds;

import me.conflicted.staffauth.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import me.conflicted.staffauth.utils.*;
import com.mongodb.*;
import java.util.regex.*;
import java.util.*;

public class ResetMail implements CommandExecutor
{
    private DBCollection emails;
    private StaffAuth auth;
    
    public ResetMail() {
        this.auth = StaffAuth.getInstance();
        this.auth.getCommand("resetmail").setExecutor((CommandExecutor)this);
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 0 || args.length > 2 || args.length < 2) {
                sender.sendMessage(this.auth.color("&cUsage: /resetmail <player> <email>"));
                return false;
            }
            final Player p = Bukkit.getPlayer(args[0]);
            if (p != null) {
                final String email = args[1];
                if (this.emailChecker(email)) {
                    if (this.auth.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("database") && Mail.hasEmail(p.getUniqueId())) {
                        this.emails = this.auth.getMongo().getDatabase().getCollection("emails");
                        final BasicDBObject query = new BasicDBObject("UUID", p.getUniqueId().toString());
                        final DBCursor c = this.emails.find(query);
                        try {
                            if (c.hasNext()) {
                                this.emails.remove(c.next());
                            }
                        }
                        finally {
                            c.close();
                        }
                        final BasicDBObject obj = new BasicDBObject();
                        ((HashMap<String, String>)obj).put("UUID", p.getUniqueId().toString());
                        ((HashMap<String, String>)obj).put("email", email);
                        this.emails.insert(obj);
                        sender.sendMessage(this.auth.color("&cReset " + p.getName() + "'s email to " + email));
                    }
                    else if (this.auth.getConfig().getString("Settings.storage-sys").equalsIgnoreCase("file")) {
                        this.auth.getConfig().set("Players-Mail." + p.getUniqueId(), (Object)email);
                        this.auth.saveConfig();
                        sender.sendMessage(this.auth.color("&cReset " + p.getName() + "'s email to " + email));
                    }
                    else if (!Mail.hasEmail(p.getUniqueId())) {
                        sender.sendMessage(this.auth.color("&cPlayer does not have an email registered."));
                    }
                }
                else {
                    sender.sendMessage(this.auth.color("&cInvalid email!"));
                }
            }
            else {
                sender.sendMessage(this.auth.color("&cInvalid player!"));
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
