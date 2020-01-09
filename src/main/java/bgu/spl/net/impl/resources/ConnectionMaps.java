package bgu.spl.net.impl.resources;

import bgu.spl.net.srv.ConnectionHandler;
import org.apache.commons.lang3.tuple.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionMaps<T> {

    private AtomicInteger idCounter;

    private ConcurrentHashMap<Integer, ConnectionHandler<T>> Id_ClientMap=new ConcurrentHashMap<>();// a Concurrent Map that maps Client Id to his connection Handler.

    //First Entry of pair is topic subscription id and Second is the topic.
    private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Pair<String,String>>> Id_TopicMap=new ConcurrentHashMap<>();// a Concurrent Map that maps Client Id to topics he's subscribed to.

    private ConcurrentHashMap<String, Set<ConnectionHandler<T>>> ChannelMap=new ConcurrentHashMap<>();// a Concurrent Map that maps a topic to it's subscribed clients.

    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getId_ClientMap() {
        return Id_ClientMap;
    }

    public ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Pair<String, String>>> getId_TopicMap() {
        return Id_TopicMap;
    }

    public ConcurrentHashMap<String, Set<ConnectionHandler<T>>> getChannelMap() {
        return ChannelMap;
    }

    public ConnectionMaps() {
        idCounter = new AtomicInteger(0);
        Id_ClientMap = new ConcurrentHashMap<>();
        Id_TopicMap = new ConcurrentHashMap<>();
        ChannelMap = new ConcurrentHashMap<>();
    }


    /**
     * Adds the Topic to the client's Topic map with the subscription Id and the topic as a Pair.
     * also adds the Client to the ChannelMap to associate the Client with the Topic.
     * @param connectionId-The Unique connection id of the Client.
     * @param topic-The Topic to subscribe to.
     * @param subscribeId-The Subscription Id of the Client to the Topic.
     */
    public void addClientToTopic(int connectionId, String topic,String subscribeId) {
        if (Id_TopicMap.containsKey(connectionId)){
            Id_TopicMap.get(connectionId).add(new ImmutablePair<>(subscribeId,topic));
            if (!ChannelMap.containsKey(topic)) {
                ChannelMap.put(topic, new HashSet<>());
            }
            ChannelMap.get(topic).add(Id_ClientMap.get(connectionId));
        }else {
            System.out.println("Error:trying to add Topic to non existing client");
        }
    }

    /**
     * Removes the Topic from the Client's topic map since he isn't subscribed to it anymore,
     * also removes the Client from ChannelMap since he isn't longer associated to this Topic/Channel.
     * @param connectionId-The Unique connection id of the Client.
     * @param subscribeId-The Subscription Id of the Client to the Topic.
     */
    public void removeClientFromTopic(int connectionId, String subscribeId) {
        if(Id_ClientMap.containsKey(connectionId)) {
            ConnectionHandler<T> ClientToRemoveFromTopic=Id_ClientMap.get(connectionId);

            if (Id_TopicMap.containsKey(connectionId)) {
                ConcurrentLinkedQueue<Pair<String, String>> clientTopics = Id_TopicMap.get(connectionId);
                for (Pair<String, String> aPair : clientTopics) {
                    if (aPair.getKey().equals(subscribeId)) {
                        //removes the topic Pair from the client's TopicMap.
                        Id_TopicMap.get(connectionId).remove(aPair);

                        //removes the client from the ChannelMap so he won't get messages regarding this topic.
                        ChannelMap.get(aPair.getValue()).remove(ClientToRemoveFromTopic);

                    }
                }

            }
        }
        else
        {
            System.out.println("Error:trying to remove Client that doesn't exist ");
        }
    }

    /**
     *Adds the client specified to the corresponding maps.
     * @param connectionId-The Unique connection id of the Client.
     * @param toAdd-The Client to be added to the maps.
     */
    public void addClientToIdMaps(int connectionId,ConnectionHandler<T> toAdd)
    {
        Id_ClientMap.put(connectionId,toAdd);
        Id_TopicMap.put(connectionId,new ConcurrentLinkedQueue<>());
    }

    /**
     *
     * @param connectionId-The Unique connection id of the Client.
     * @param Topic-The topic to search the subscriptionId of the Client.
     * @return Null if not found or the TopicMap doesn't contain the Client, or the subscriptionId of the Client to the topic.
     */
    public String getSubscriptionId(int connectionId,String Topic)
    {
        if(Id_TopicMap.containsKey(connectionId))
        {
            for (Pair<String, String> pair : Id_TopicMap.get(connectionId)) {
                if(pair.getValue().equals(Topic))
                {
                    return pair.getKey();
                }
            }
        }
        return null;
    }

    public int getIdCounter() {
        int val;
        do {
            val = idCounter.get();
        }while (idCounter.compareAndSet(val,val+1));
        return val;
    }
}
