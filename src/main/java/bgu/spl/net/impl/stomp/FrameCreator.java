package bgu.spl.net.impl.stomp;

import java.util.concurrent.atomic.AtomicInteger;

public class FrameCreator {
    private static final AtomicInteger id=new AtomicInteger(0);
    private String Command;
    private String body;
    private FrameCreator(String Command){

        this.Command=Command;
    }

//    for debug
    public static FrameCreator emptyFrame(){

        return new FrameCreator("empty");
    }

    /**
     * crate a FrameCreator that return a Connected
     * @return out FrameCreator for Connected
     */
    public static FrameCreator FrameCreatorConnected(){
        FrameCreator out = new FrameCreator("CONNECTED");
        out.setBody("version:1.2"+'\n'+"\n");
        return out;
    }


    /**
     *
     * @param receiptId that id for the RECEIPT frame
     * @return out FrameCreator for Receipt frame
     */
    public static FrameCreator FrameCreatorReceipt(String receiptId){
        FrameCreator out = new FrameCreator("RECEIPT");
        StringBuilder zeros = new StringBuilder();
        for (int i = 0;i<5-receiptId.length();i++){
            zeros.append("0");
        }
        out.setBody("receipt-id:"+zeros+receiptId+'\n'+"\n");
        return out;
    }

    /**
     *
     * @param Id - the id of the client
     * @param reason - the reason for the error
     * @param TheMessage - the message that give the erroe
     * @param detailed - more info for the problome
     * @return out FrameCreator for ERROR frame
     */
    public static FrameCreator FrameCreatorError(String Id, String reason, String TheMessage, String detailed){
        FrameCreator out = new FrameCreator("ERROR");
        out.setBody("receipt-id:"+Id+"\nmessage:"+reason+'\n'+"\nThe message:"+"\n-----\n"+TheMessage+"\n-----\n"+detailed+"\n");
        return out;
    }

    /**
     *
     * @param subscription - the id of the subscription
     * @param destination - the destination of the message
     * @param body - the body of the frame
     * @return out FrameCreator for MESSAGE frame
     */
    public static FrameCreator FrameCreatorMessage(String subscription, String destination, String body){
        FrameCreator out = new FrameCreator("MESSAGE");
        out.setBody("subscription:"+subscription+"\nmessage-id:"+idToString(id.get())+"\ndestination:"+destination+"\n\n"+body);
        return out;
    }


    public String toString(){

        return getCommand()+'\n'+getBody();
    }

    public String getCommand() {

        return Command;
    }

    public String getBody() {

        return body;
    }

    public void setCommand(String command) {

        Command = command;
    }

    public void setBody(String body) {

        this.body = body;
    }

    private static String idToString(int Id){
        String IdString = id.toString();
        String toAdd="";
        for (int i = 0; i < 5-IdString.length(); i++) {
            toAdd="0"+toAdd;
        }
        return toAdd+IdString;
    }

    /**
     * increase the id of the message
     */
    public static void increase(){
        int val;
        do {
            val = id.get();
        }while (!id.compareAndSet(val, val + 1));
    }
    }

