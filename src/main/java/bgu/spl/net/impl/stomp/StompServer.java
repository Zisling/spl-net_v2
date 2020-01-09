package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.srv.ConnectionsImpl;
import bgu.spl.net.srv.Reactor;
import bgu.spl.net.srv.Server;


public class StompServer {


    public static void main(String[] args) {

        if(args[1].equals("reactor"))
        {
            Reactor<String> reactor= new Reactor<>(4, Integer.parseInt(args[0]), StompMessagingProtocolImp::new, LineMessageEncoderDecoder::new);
            reactor.serve();
        }
        else if(args[1].equals("tpc"))
        {
        Server.threadPerClient(Integer.parseInt(args[0]), StompMessagingProtocolImp::new, LineMessageEncoderDecoder::new).serve();
        }

    }


}
