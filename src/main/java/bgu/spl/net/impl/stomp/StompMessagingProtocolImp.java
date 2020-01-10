package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.resources.ConnectionMaps;
import bgu.spl.net.impl.resources.SharedResources;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.HashMap;

public class StompMessagingProtocolImp implements StompMessagingProtocol {
    private boolean ConnectedFrameReceived = false;
    private boolean shouldTerminate=false;
    private Connections<String > connections;
    private int connectionId;
    private ConnectionMaps connectionMaps;
    private ConnectionHandler handler;
    private String userName;
    private SharedResources sharedResources;


    public StompMessagingProtocolImp() {
        sharedResources = SharedResources.getInstance();
        connectionMaps = sharedResources.getConnectionMaps();
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connections=connections;
        this.connectionId=connectionId;
    }

    @Override
    public void process(String message) {

            FrameCreator frame=null;
            int at = message.indexOf('\n');
            String command = message.substring(0, at);
            String body = message.substring(at+1);
            if (!ConnectedFrameReceived){
                if (command.equals("STOMP")||command.equals("CONNECT")){
                    String frameSt = ProcessConnect(body);
                    connections.send(connectionId, frameSt);
                }else {
                    connections.send(connectionId,"ERROR\nmessage:you need to send a CONNECT frame first\n\n");
                    shouldTerminate=true;
                }


                    ConnectedFrameReceived = true;
            }
            else {

            if (command.equals("SUBSCRIBE")){
                frame = ProcessSubscribe(body);
            }
            else if (command.equals("UNSUBSCRIBE")){
                frame=ProcessUnsubscribe(body);
            }else if (command.equals("DISCONNECT")){
                frame=ProcessDisconnect(body);
            }else if (command.equals("SEND")){
                frame=ProcessSend(body);
            }


            if(frame.getCommand().equals("ERROR")){
                connections.send(connectionId, frame.toString());
                shouldTerminate=true;
                sharedResources.logout(userName);
            }
            else if (frame.getCommand().equals("RECEIPT")){
                connections.send(connectionId, frame.toString());
            }
        }
        }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }


    public String  ProcessConnect(String body){
        if (body!=null&&!body.isEmpty()) {
            HashMap<String ,String > messageMap = cutFrame(body);
            System.out.println(messageMap.get("login")+":"+messageMap.get("passcode"));
            if(messageMap.get("accept-version").equals("1.2")&sharedResources.login(messageMap.get("login"),messageMap.get("passcode")))
            {
                userName=messageMap.get("login");
                return FrameCreator.FrameCreatorConnected().toString();
        }else{
                String error;
                if (sharedResources.isLogin(messageMap.get("login"))){
                    error="ERROR\nmessage:user is already logged in\n\n";
                    System.out.println(error);
                }else {
                    error = "ERROR\nmessage: password is incorrect or version isn't compatible\n\n";
                }
                return error;
            }
        }
        return "ERROR\nmessage:your frame is curated\n\n";
    }



    /**
     *Processes a SEND Frame.
     * @param body -The entire frame except the Stomp Command.
     * @return FrameCreator- A FrameCreator object of the correct Frame.
     */
    public FrameCreator ProcessSend(String body){
        FrameCreator out = null;
        if (body!=null&&!body.isEmpty()) {
            HashMap<String ,String > keyMap = cutFrame(body);
            if (keyMap.containsKey("destination")&&keyMap.get("destination").length()>0){
                String subId =connectionMaps.getSubscriptionId(connectionId, keyMap.get("destination"));
                if (subId!=null){
                out = FrameCreator.FrameCreatorMessage(
                        subId,keyMap.get("destination"),keyMap.get("body"));
                connections.send(keyMap.get("destination"), out.toString());
                return out;
                }else {
                    return FrameCreator.FrameCreatorError("no id in send","destination don't exist",body,"didn't subscribe to chanel");
                }

            }else {
                return FrameCreator.FrameCreatorError("no id in send","destination don't exist",body,"destination may be write wrong or don't exist at all");
            }
        }
        else{
            return FrameCreator.FrameCreatorError("null","null or empty body",body,"");
        }
    }

    /**
     *Process a DISCONNECT Frame sent from the Client.
     * @param body -The entire frame except the Stomp Command.
     * @return FrameCreator- A FrameCreator object of the correct Frame.
     */
    public FrameCreator ProcessDisconnect(String body){
        if (body!=null&&!body.isEmpty()) {
            HashMap<String ,String > keyMap = cutFrame(body);
            if (keyMap.containsKey("receipt")){
                connections.send(connectionId, FrameCreator.FrameCreatorReceipt(keyMap.get("receipt")).toString());
                shouldTerminate=true;
                connections.disconnect(connectionId);
                sharedResources.logout(userName);
                return FrameCreator.emptyFrame();
            }
            else {
                return FrameCreator.FrameCreatorError("dont have a receipt","no receipt-id",body,"");
            }
        }else {
            return FrameCreator.FrameCreatorError("null","null or empty body",body,"");
        }
    }

    /**
     *Process an UNSUBSCRIBE Frame sent from the Client.
     * @param body -The entire frame except the Stomp Command.
     * @return FrameCreator- A FrameCreator object of the correct Frame.
     */
    public FrameCreator ProcessUnsubscribe(String body){
        if (body!=null&&!body.isEmpty()) {
            HashMap<String ,String > keyMap = cutFrame(body);
            if (keyMap.containsKey("id")){
                connectionMaps.removeClientFromTopic(connectionId, keyMap.get("id"));
                if (keyMap.containsKey("receipt-id")){
                    return FrameCreator.FrameCreatorReceipt(keyMap.get("receipt-id"));
                }else {
                    return FrameCreator.emptyFrame();
                }
            }else {
                return FrameCreator.FrameCreatorError("don't have a id", "don't have a id", body, "");
            }

        }else {
            return FrameCreator.FrameCreatorError("null","null or empty body",body,"");
        }

    }

    /**
     *Process a SUBSCRIBE Frame sent from the Client.
     * @param body-The entire frame except the Stomp Command.
     * @return FrameCreator for the correct response
     */
    public FrameCreator ProcessSubscribe(String body){
        System.out.println("start subing");
        System.out.println(body);
        if (body!=null&&!body.isEmpty()) {
            HashMap<String ,String > keyMap = cutFrame(body);
            if (keyMap.containsKey("destination")){
                if (keyMap.containsKey("id")){
                    System.out.println("did sub");
                connectionMaps.addClientToTopic(connectionId, keyMap.get("destination"),keyMap.get("id"));
                }
                if (keyMap.containsKey("receipt")){
                    return FrameCreator.FrameCreatorReceipt(keyMap.get("receipt"));
                }
                else {
                    return FrameCreator.emptyFrame();
                }
            }else {
                return FrameCreator.FrameCreatorError(keyMap.get("id"),"destination don't exist",body,"destination may be write wrong or don't exist at all");
            }


        }else {
            return FrameCreator.FrameCreatorError("null","null or empty body",body,"");
        }

    }


    /**
     *
     * @param body-The entire frame except the Stomp Command.
     * @return an Hashmap representing the Frame.
     */
    public static HashMap<String ,String > cutFrame(String body){
        HashMap<String ,String > out= new HashMap<>();
        int at = body.indexOf('\n');
        while (body.substring(0,at).length()>0){
            String line = body.substring(0, at);
            body = body.substring(at + 1);
            String header= line.substring(0, line.indexOf(':'));
            String value= line.substring(line.indexOf(':')+1);
            out.put(header,value);
            at = body.indexOf('\n');
        }
        body=body.substring(at+1);//see if we need it
        out.put("body", body);

        return out;
    }


}
