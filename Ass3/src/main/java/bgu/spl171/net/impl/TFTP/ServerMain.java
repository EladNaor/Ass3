package bgu.spl171.net.impl.TFTP;

import bgu.spl171.net.api.MessageEncDec;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.impl.newsfeed.NewsFeed;
import bgu.spl171.net.srv.Server;

public class ServerMain {

    public static void main(String[] args) {

// you can use any server...
        Server.threadPerClient(
                Integer.valueOf(args[0]), //port
                BidiMessagingProtocolImpl::new, //protocol factory
                MessageEncDec::new //message encoder decoder factory
        ).serve();

//        Server.reactor(
//                Runtime.getRuntime().availableProcessors(),
//                7777, //port
//                () ->  new RemoteCommandInvocationProtocol<>(feed), //protocol factory
//                ObjectEncoderDecoder::new //message encoder decoder factory
//        ).serve();

    }
}
