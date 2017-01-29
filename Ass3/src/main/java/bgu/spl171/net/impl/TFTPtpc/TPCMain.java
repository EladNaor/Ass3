package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.api.MessageEncDec;
import bgu.spl171.net.impl.TFTP.BidiMessagingProtocolImpl;
import bgu.spl171.net.srv.Server;

public class TPCMain {

    public static void main(String[] args) {
        Server.threadPerClient(
                Integer.valueOf(args[0]), //port
                BidiMessagingProtocolImpl::new, //protocol factory
                MessageEncDec::new //message encoder decoder factory
        ).serve();
    }
}
