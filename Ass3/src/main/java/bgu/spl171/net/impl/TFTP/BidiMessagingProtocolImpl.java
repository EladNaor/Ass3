package bgu.spl171.net.impl.TFTP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import java.nio.file.Path;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.packet.Packet;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

public class BidiMessagingProtocolImpl implements  BidiMessagingProtocol<Packet>{
	private int connectionId;
	private static ConcurrentMap<Integer, String> logedInUsersMap = new ConcurrentHashMap<>();
	private static final File FilesDir = new File("/Files");
	private static ConcurrentMap<Integer, String> fileUploading = new ConcurrentHashMap<>();
	
	private dataHandler dataHandler;
	private boolean shouldTerminate = false;
	private static Connections<Packet> connections;
	
	@Override
	public void start(int connectionId, Connections<Packet> pConnections) {
		this.connectionId=connectionId;
		connections=pConnections;
	}

	@Override
	public void process(Packet message) {

		short opCode = message.getOpCode();
		Packet pack = new Packet();
		byte[] rawData = null;
		
		//checks user doesn't have logging in issues (LOGRQ)
		if(message.getOpCode() == 7){
			String userName = message.getString();
			if(logedInUsersMap.containsKey(this.connectionId) 
					|| logedInUsersMap.containsValue(userName)){
				pack.createERRORpacket((short) 7, "7");
				connections.send(connectionId, pack);
			} else {
				logedInUsersMap.put(this.connectionId, userName);
				pack.createACKpacket((short) 0);
				connections.send(connectionId,pack);
			}
		} else if(!logedInUsersMap.containsKey(this.connectionId)){
			pack.createERRORpacket((short) 7, "");
			connections.send(5, pack);
		}
		//check what type of packet he got and acts accordingly
		else {
			switch(opCode){
			
			//RRQ- read
			case 1:
				File wfile = new File(FilesDir+"/"+message.getString());
				try {
					rawData = Files.readAllBytes(wfile.toPath());
				} catch (IOException e) {
					System.out.println("coudn't turn files into bytes");
					e.printStackTrace();
				}
				if(!wfile.exists()){
					pack.createERRORpacket((short)1,"1");
					connections.send(this.connectionId, pack);
				} else {
					this.dataHandler = new dataHandler("reading", message.getString(), connections, connectionId, fileUploading);
					this.dataHandler.devideRawDataIntoBlocksAndSendFirst(rawData);
				}
				break;
				
			//WRQ -write
			case 2:
				File file = new File(FilesDir+"/"+message.getString());
				if(file.exists()){
					pack.createERRORpacket((short)5,"5");
					connections.send(this.connectionId, pack);
				} else {
				pack.createACKpacket((short) 0);
				connections.send(this.connectionId, pack);
				this.dataHandler = new dataHandler("writing", message.getString(), connections,connectionId, fileUploading);
				}
				break;
				
				//DATA 
			case 3: 
				if(this.dataHandler.getAction().equals("writing")){
					dataHandler.addToFileUploading(message.getData());
					pack.createACKpacket(message.getBlockNumber());
					connections.send(connectionId, pack);
				} else {
					pack.createERRORpacket((short)5,"protocol trying to write and do another action at the same time");
				connections.send(this.connectionId, pack);
				}
	
				//ACK
			case 4: this.dataHandler.sendNext();
					break;
				
				
				//ERROR
			case 5: this.dataHandler.reset();
			
				//DIRQ
			case 6: 
				String dirList = "";
				
				File allFiles = new File("./Files"); // directory of the files folder
				File[] allFilesArray = allFiles.listFiles();
				if (allFilesArray!= null && allFilesArray.length > 0) {
					for (File fileName : allFilesArray) {
						if (!fileUploading.containsValue(fileName.getName())) {
							dirList = dirList + "\n" + fileName.getName();
						}
					}
					rawData = dirList.getBytes();
					this.dataHandler = new dataHandler("dirq", "", connections, connectionId, fileUploading);
					this.dataHandler.devideRawDataIntoBlocksAndSendFirst(rawData);
				}
			//DELRQ
			case 8: 
				Path path = Paths.get(FilesDir+"/"+message.getString());
				if(!path.toFile().exists()){
					pack.createERRORpacket((short)1,"1");
					connections.send(this.connectionId, pack);
				} else { 
					if(path.toFile().delete()) {
						pack.createBCASTpacket(false, message.getString());
						connections.send(connectionId, pack);
					}
					else
					{
						pack.createERRORpacket((short) 1, "");
						connections.send(connectionId, pack);
					}
				}
				break;
				
				// DISC
			case 10:
				logedInUsersMap.remove(this.connectionId);
				connections.disconnect(connectionId);
				this.shouldTerminate = true;
				pack.createACKpacket((short) 0);
				connections.send(connectionId, pack);
				break;
				
				default:
					pack.createERRORpacket((short)4,"4");
					connections.send(this.connectionId, pack);
					
					break;
			
			}
		}
	}
	



	@Override
	public boolean shouldTerminate() {
		return this.shouldTerminate;
	}
}
