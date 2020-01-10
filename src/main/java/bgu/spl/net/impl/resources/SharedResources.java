package bgu.spl.net.impl.resources;

import java.util.concurrent.ConcurrentHashMap;

public class SharedResources {

    private ConcurrentHashMap<String ,String > userNamePassMap;
    private ConcurrentHashMap<String,Boolean> userConnectedMap;
    private ConnectionMaps<?> connectionMaps;



    private SharedResources() {
            this.userNamePassMap = new ConcurrentHashMap<>();
            this.userConnectedMap= new ConcurrentHashMap<>();
    }

    /**
     * Holds SharedResources singleton instance.
     */
    private static class SharedResourcesHolder{

        private static final SharedResources instance =new SharedResources();
    }

    /**
     * Returns the singleton instance of SharedResources.
     * @return - SharedResourcesHolder.instance -the only instance of SharedResources.
     */
    public static SharedResources getInstance()
    {
        return SharedResourcesHolder.instance;
    }


    /**
     * Login a Client by userName and password , if the userNamePassMap doesn't contains the provided user it creates a new user and login.
     * @param userName-Client's User Name to login by
     * @param password- Client's password.
     * @return - returns True if login is successful, returns false if user exists but password is wrong or user is already logged in.
     */
    public boolean login(String userName,String password){
        if (userNamePassMap.containsKey(userName)){
            if (!isLogin(userName)){
                userConnectedMap.remove(userName);
                userConnectedMap.put(userName, true);
            return password.equals(userNamePassMap.get(userName));
            }
            else {
                return false;
            }
        }else {
            userNamePassMap.put(userName, password);
            userConnectedMap.put(userName, true);
            return true;
        }
    }

    public boolean isLogin(String userName)
    {
        return userConnectedMap.get(userName);
    }

    /**
     * Logs out a user.
     * @param userName-username to logout.
     */
    public void logout(String userName){
        if (userName!=null){
        userConnectedMap.remove(userName);
        userConnectedMap.put(userName, false);
        }
    }

    public ConnectionMaps<?> getConnectionMaps() {
        return connectionMaps;
    }

    public void setConnectionMaps(ConnectionMaps<?> connectionMaps) {
        this.connectionMaps = connectionMaps;
    }
}
