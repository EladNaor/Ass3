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
	private static HashMap<Integer, String> logedInUsersMap;
	private static final File FilesDir = new File("/Files");
	private static HashMap<Integer, String> fileUploading;
	
	private dataHandler dataHandler;
	private boolean shouldTerminate = false;
	private static Connections<Packet> connections;
	
	@Override
	public void start(int connectionId, Connections connections) {
		this.connectionId=connectionId;
		this.connections=connections;
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
			}
		} else if(!logedInUsersMap.containsKey(this.connectionId)){
			pack.createERRORpacket((short) 7, "");
			this.connections.send(5, pack);
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
					this.connections.send(this.connectionId, pack);
				} else {
					this.dataHandler = new dataHandler("reading", message.getString(), this.connections, this.fileUploading);
					this.dataHandler.devideRawDataIntoBlocksAndSendFirst(rawData);
				}
				break;
				
			//WRQ -write
			case 2:
				File file = new File(FilesDir+"/"+message.getString());
				if(file.exists()){
					pack.createERRORpacket((short)5,"5");
					this.connections.send(this.connectionId, pack);
				} else {
				pack.createACKpacket((short) 0);
				this.connections.send(this.connectionId, pack);
				this.dataHandler = new dataHandler("writing", message.getString(), this.connections, this.fileUploading);
				}
				break;
				
				//DATA 
			case 3: 
				if(this.dataHandler.getAction().equals("writing")){
					dataHandler.addToFileUploading(message.getData());
					pack.createACKpacket(message.getBlockNumber());
					this.connections.send(connectionId, pack);
				} else {
					pack.createERRORpacket((short)5,"protocol trying to write and do another action at the same time");
				this.connections.send(this.connectionId, pack);
				}
	
				//ACK
			case 4: this.dataHandler.sendNext();
					break;
				
				
				//ERROR
			case 5: this.dataHandler.reset();
			
				//DIRQ
			case 6: 
				String dirList = new String("");
				
				File allFiles = new File("./Files"); // directory of the files folder
				File[] allFilesArray = allFiles.listFiles();
				for(File fileName: allFilesArray){
					if(!fileUploading.containsValue(fileName)){
						dirList= dirList+"\n"+fileName.getName();
					}
				}
				rawData = dirList.getBytes();
				this.dataHandler = new dataHandler("dirq", "", this.connections, this.fileUploading);
				this.dataHandler.devideRawDataIntoBlocksAndSendFirst(rawData);
				
			//DELRQ
			case 8: 
				Path path = Paths.get(FilesDir+"/"+message.getString());
				if(!path.toFile().exists()){
					pack.createERRORpacket((short)1,"1");
					this.connections.send(this.connectionId, pack);
				} else { 
					path.toFile().delete();
					pack.createBCASTpacket(false, message.getString());
					connections.send(connectionId, pack);
				}
				break;
				
				//DISC
			case 10:
				logedInUsersMap.remove(this.connectionId);
				this.connections.disconnect(connectionId);
				this.shouldTerminate = true;
				break;
				
				default:
					pack.createERRORpacket((short)4,"4");
					this.connections.send(this.connectionId, pack);
					
					break;
			
			}
		}
	}
	



	@Override
	public boolean shouldTerminate() {
		return this.shouldTerminate;
	}
}
