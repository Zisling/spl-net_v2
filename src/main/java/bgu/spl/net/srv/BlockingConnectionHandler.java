package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.resources.ConnectionMaps;
import bgu.spl.net.impl.resources.SharedResources;
import bgu.spl.net.impl.stomp.StompMessagingProtocolImp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final StompMessagingProtocol protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private final Object lock=new Object();
    private ConcurrentLinkedQueue<T> messageQueue=new ConcurrentLinkedQueue<>();
    private volatile boolean connected = true;
    private SharedResources sharedResources;
    private ConnectionMaps connectionMaps;
    private boolean init;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, StompMessagingProtocol protocol) {
        init=false;
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        sharedResources=SharedResources.getInstance();
        this.connectionMaps =  sharedResources.getConnectionMaps();
    }

    @Override
    public void run() {

        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            if (!init){
                init= true;
                int id = connectionMaps.getIdCounter();
                System.out.println("this is my id  "+ id);
                connectionMaps.addClientToIdMaps(id,this);
                protocol.start(id,new ConnectionsImpl<>(connectionMaps));
            }
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            while (!protocol.shouldTerminate() &&connected) {
                while (messageQueue.isEmpty()&&connected&&in.available()>0&&(read = in.read()) >= 0){
                    T nextMessage = encdec.decodeNextByte((byte) read);
                    if (nextMessage!=null){
                        protocol.process(nextMessage.toString());
                    }
                }
                while (!messageQueue.isEmpty()&&connected) {
                    out.write(encdec.encode(messageQueue.poll()));
                    out.flush();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
            messageQueue.add(msg);
    }
}
