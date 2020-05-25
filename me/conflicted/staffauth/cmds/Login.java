package me.conflicted.staffauth.cmds;

import me.conflicted.staffauth.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public class Login implements CommandExecutor
{
    private StaffAuth auth;
    
    public Login() {
        this.auth = StaffAuth.getInstance();
        this.auth.getCommand("login").setExecutor((CommandExecutor)this);
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player)sender;
            if (p.hasPermission("staffauth.auth")) {
                if (args.length == 0 || args.length > 1) {
                    p.sendMessage(this.auth.color("&cUsage: /login <code>"));
                }
                else if (this.auth.auth.contains(p.getUniqueId())) {
                    final String code = this.auth.code.get(p.getUniqueId());
                    final String codeEntered = args[0];
                    if (codeEntered.equalsIgnoreCase(code)) {
                        this.auth.auth.remove(p.getUniqueId());
                        p.sendMessage(this.auth.color("&aLogged in!"));
                    }
                    else {
                        p.sendMessage(this.auth.color("&cError! Wrong code.."));
                    }
                }
                else {
                    p.sendMessage(this.auth.color("&cYou are already logged in!"));
                }
            }
            else {
                p.sendMessage(this.auth.color("&cNo permission!"));
            }
        }
        return false;
    }
}
