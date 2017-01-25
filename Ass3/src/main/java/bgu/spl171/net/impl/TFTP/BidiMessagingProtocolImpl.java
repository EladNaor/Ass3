package bgu.spl171.net.impl.TFTP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.packet.Packet;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

public class BidiMessagingProtocolImpl implements  BidiMessagingProtocol<Packet>{
	private int connectionId;
	private Connections connections;
	private ConnectionHandler<Packet> handler;

	@Override
	public void start(int connectionId, Connections connections) {
		this.connectionId=connectionId;
		this.connections=connections;
	}

	@Override
	public void process(Packet message) {
		short opCode = message.getOpCode();
		Packet pack = new Packet();
		byte[] data = null;
		
		switch(opCode){
		case 1:
			try {
				Path path = Paths.get(message.getString());
				data = Files.readAllBytes(path);
			} catch (IOException e) {
				pack.createERRORpacket(1);
				this.connections.send(this.connectionId, pack);
			}
			//pack.createDATApacket((short)data.length,data);
			this.connections.send(this.connectionId, pack);
			break;
			
		case 2:
			File file = new File(message.getString());
			if(file.exists()){
				pack.createERRORpacket(5);
				this.connections.send(this.connectionId, pack);
			}
			pack.createACKpacket((short) 0);
			this.connections.send(this.connectionId, pack);
		}

		
		
		}

	@Override
	public boolean shouldTerminate() {
		// TODO Auto-generated method stub
		return false;
	}
}
