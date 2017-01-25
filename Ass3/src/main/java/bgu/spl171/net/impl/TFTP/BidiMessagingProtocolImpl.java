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
	private boolean isWriting = false; 
	private short countOfblockExpected = 0;
	private ConcurrentLinkedQueue<byte[]> devidedDataQueue;
	
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
		
		switch(opCode){
		case 1:
			try {
				Path path = Paths.get(message.getString());
				rawData = Files.readAllBytes(path);
			} catch (IOException e) {
				pack.createERRORpacket((short)1,"1");
				connections.send(this.connectionId, pack);
			}
			if(rawData.length <= 512){
				pack.createDATApacket((short) rawData.length, (short) 1, rawData);
			} else {
				byte[] firstBlock = this.devideRawDataIntoBlocksAndGetFirst(rawData);
				pack.createDATApacket((short) firstBlock.length, (short) 1, firstBlock);
			}
			this.connections.send(this.connectionId, pack);
			break;
			
		case 2:
			File file = new File(message.getString());
			if(file.exists()){
				pack.createERRORpacket((short)5,"5");
				this.connections.send(this.connectionId, pack);
			}
			pack.createACKpacket((short) 0);
			this.connections.send(this.connectionId, pack);
			this.isWriting = true;
			
		case 3: // TODO understand what to do with data received
			
		case 4:
			if(!(message.getBlockNumber() == this.countOfblockExpected)){
				pack.createERRORpacket((short)1,"1");
			}
			
		case 5: //TODO what to do when client sends an error
			
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
				byte[] firstBlock = this.devideRawDataIntoBlocksAndGetFirst(rawData);
				pack.createDATApacket((short) firstBlock.length, (short) 1, firstBlock);
			}
			connections.send(3,pack);

		case 7:
			
		
		}
	}

	private byte[] devideRawDataIntoBlocksAndGetFirst(byte[] rawData) {
		int sumOfBlocks = (int) Math.ceil(rawData.length/512);
		for(int i = 0; i< sumOfBlocks; i++){
			byte[] dataBlock = new byte[512];
			for(int j = 0; j< 512; j++){
				dataBlock[j] = rawData[512*i +j]; 
			}
			this.devidedDataQueue.add(dataBlock);
		}
		return this.devidedDataQueue.poll();
	}

	@Override
	public boolean shouldTerminate() {
		// TODO Auto-generated method stub
		return false;
	}
}
