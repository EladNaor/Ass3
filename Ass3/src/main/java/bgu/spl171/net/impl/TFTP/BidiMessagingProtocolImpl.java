package bgu.spl171.net.impl.TFTP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import java.nio.file.Path;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.packet.Packet;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

public class BidiMessagingProtocolImpl implements  BidiMessagingProtocol<Packet>{
	private int connectionId;
	private ConnectionHandler<Packet> handler;
	private static HashMap<Integer, String> logedInUsersMap;
	private static final File FilesDir = new File("/Files");
	
	private dataHandler dataHandler;
	private static Connections<Packet> connections;
	
	@Override
	public void start(int connectionId, Connections connections) {
		this.connectionId=connectionId;
		this.connections=connections;
	}

	@Override
	public void process(Packet message) {
		//TODO wrap with verification that user is logged in  
		short opCode = message.getOpCode();
		Packet pack = new Packet();
		byte[] rawData = null;
		switch(opCode){
		
		//RRQ- read
		case 1:
			File wfile = new File(FilesDir+"/"+message.getString());
			try {
				rawData = Files.readAllBytes(wfile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!wfile.exists()){
				pack.createERRORpacket((short)1,"1");
				this.connections.send(this.connectionId, pack);
			} else {
				this.dataHandler = new dataHandler("reading", message.getString(), this.connections);
				this.dataHandler.devideRawDataIntoBlocksAndSendFirst(rawData);
			}
			this.connections.send(this.connectionId, pack);
			break;
			
		//WRQ -write
		case 2:
			File file = new File(FilesDir+"/"+message.getString());
			if(file.exists()){
				pack.createERRORpacket((short)5,"5");
				this.connections.send(this.connectionId, pack);
			}
			pack.createACKpacket((short) 0);
			this.connections.send(this.connectionId, pack);
			this.dataHandler.setAction("writing");
			break;
			
			//DATA 
		case 3: 
			if(this.dataHandler.getAction().equals("write")){
				short blockCount = dataHandler.getAndIncCountOfblockExpected();
				dataHandler.addToFileUploading(message.getData());
				pack.createACKpacket(blockCount);
				this.connections.send(4, pack);
			} else throw new RuntimeException("protocol trying to do more than one action at the same time");
			

			//ACK
		case 4:
			if(!(message.getBlockNumber() == this.dataHandler.getAndIncCountOfblockExpected())){
				pack.createERRORpacket((short)1,"1");
				break;
			}
			
			//ERROR
		case 5: this.dataHandler.reset();
		
			//DIRQ
		case 6: 
			String dirList = new String("");
			
			File allFiles = new File("./Files"); // directory of the files folfer
			File[] allFilesArray = allFiles.listFiles();
			for(File fileName: allFilesArray){
				//TODO only if current file is not uploading
				dirList= dirList+"\n"+fileName.getName();
			}
			rawData = dirList.getBytes();
			if(rawData.length <= 512){
				pack.createDATApacket((short) rawData.length, (short) 1, rawData);
			} else {
				this.dataHandler.devideRawDataIntoBlocksAndSendFirst(rawData);
			}
			connections.send(3,pack);
			//LOGRQ
		case 7:
			String userName = message.getString();
			if(logedInUsersMap.containsValue(userName)){
				pack.createERRORpacket((short) 7, "7");
				connections.send(connectionId, pack);
			} else {
				logedInUsersMap.put(this.connectionId, userName);
				pack.createACKpacket((short) 0);
			}
			break;
		//DELRQ
		case 8: 
			Path path = Paths.get(FilesDir+"/"+message.getString());
			if(!path.toFile().exists()){
				pack.createERRORpacket((short)1,"1");
				this.connections.send(this.connectionId, pack);
			} else { 
				path.toFile().delete();
				pack.createBCASTpacket(false, message.getString());
				connections.send(9, pack);
			}
			break;
			
			//DISC
		case 10:
			logedInUsersMap.remove(this.connectionId);
			this.connections.disconnect(connectionId);
			break;
		}
	}



	@Override
	public boolean shouldTerminate() {
		// TODO Auto-generated method stub
		return false;
	}
}
