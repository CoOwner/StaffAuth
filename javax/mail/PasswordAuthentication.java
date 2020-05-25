package javax.mail;

public final class PasswordAuthentication
{
    private String userName;
    private String password;
    
    public PasswordAuthentication(final String userName, final String password) {
        this.userName = userName;
        this.password = password;
    }
    
    public String getUserName() {
        return this.userName;
    }
    
    public String getPassword() {
        return this.password;
    }
}
