package bgu.spl.net.srv;

import bgu.spl.net.impl.resources.ConnectionMaps;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsImpl<T> implements Connections<T> {
    ConnectionMaps<T> maps;

    public ConnectionsImpl(ConnectionMaps maps) {
        this.maps = maps;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if(maps.getId_ClientMap().containsKey(connectionId)) {
            maps.getId_ClientMap().get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, T msg) {
        String msgStr = msg.toString();
        if (maps.getChannelMap().containsKey(channel))
        {
            for (ConnectionHandler<T> tConnectionHandler : maps.getChannelMap().get(channel)) {
                String sub = "subscription:";
                int subIdPos=msgStr.indexOf(sub);
                subIdPos = msgStr.indexOf(':', subIdPos);
                int end = msgStr.indexOf('\n', subIdPos);

                String subID = maps.getSubscriptionId(maps.getClientMap_Id().get(tConnectionHandler),channel);
                String msg2= msgStr.substring(0,subIdPos+1)+subID+msgStr.substring(end);
                tConnectionHandler.send((T) msg2);
            }
        }
        else
        {
            System.out.println("Error:No such Channel exists");
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if(maps.getId_ClientMap().containsKey(connectionId)) {
            ConnectionHandler<T> ClientToRemove=maps.getId_ClientMap().get(connectionId);
            maps.getId_ClientMap().remove(connectionId);

            //removing client from Id topic map and channel map
            ConcurrentLinkedQueue<Pair<String,String>> ListofSubscribedTopicsPairs =maps.getId_TopicMap().get(connectionId);
            for (Pair<String,String> aPair: ListofSubscribedTopicsPairs) {
                maps.getChannelMap().get(aPair.getValue()).remove(ClientToRemove);
            }

            maps.getId_TopicMap().remove(connectionId);
        }
        else
        {
            System.out.println("Error:User isn't logged in");
        }
    }
}
