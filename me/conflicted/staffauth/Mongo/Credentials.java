package me.conflicted.staffauth.Mongo;

import me.conflicted.staffauth.*;

public class Credentials
{
    public String user;
    public String pass;
    public String ip;
    public int port;
    public String db;
    private StaffAuth auth;
    public static Credentials creds;
    
    public Credentials() {
        this.auth = StaffAuth.getInstance();
        Credentials.creds = this;
        this.user = this.auth.getConfig().getString("Database.user");
        this.pass = this.auth.getConfig().getString("Database.pass");
        this.db = this.auth.getConfig().getString("Database.database");
        this.port = this.auth.getConfig().getInt("Database.port");
        this.ip = this.auth.getConfig().getString("Database.host");
    }
    
    public static Credentials getCreds() {
        return Credentials.creds;
    }
}
