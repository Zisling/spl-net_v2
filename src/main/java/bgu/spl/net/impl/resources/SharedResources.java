package bgu.spl.net.impl.resources;

import java.util.concurrent.ConcurrentHashMap;

public class SharedResources {
    private ConcurrentHashMap<String ,String > userNamePassMap;
    private ConcurrentHashMap<String,Boolean> userConnectedMap;
    private ConnectionMaps<?> connectionMaps;


    public ConnectionMaps<?> getConnectionMaps() {
        return connectionMaps;
    }

    public void setConnectionMaps(ConnectionMaps<?> connectionMaps) {
        this.connectionMaps = connectionMaps;
    }

    private SharedResources() {
            this.userNamePassMap = new ConcurrentHashMap<>();
            this.userConnectedMap=new ConcurrentHashMap<>();
    }

    private static class SharedResourcesHolder{
        private static final SharedResources instance =new SharedResources();
    }
    public static SharedResources getInstance(){
        return SharedResourcesHolder.instance;
    }



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
    public boolean isLogin(String userName){
        return userConnectedMap.get(userName);
    }
    public void logout(String userName){
        if (userName!=null){
        userConnectedMap.remove(userName);
        userConnectedMap.put(userName, false);
        }
    }

}
