package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.api.MessageEncDec;
import bgu.spl171.net.impl.TFTP.BidiMessagingProtocolImpl;
import bgu.spl171.net.srv.Server;

public class ReactorMain {

    public static void main(String[] args) {
        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                Integer.valueOf(args[0]), //port
                BidiMessagingProtocolImpl::new, //protocol factory
                MessageEncDec::new //message encoder decoder factory
        ).serve();
    }
}
