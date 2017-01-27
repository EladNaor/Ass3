package bgu.spl171.net.impl.TFTP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.management.RuntimeErrorException;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.packet.Packet;

public class dataHandler {
	private int connectionId;
	private String action;
	private short countOfblockExpected;
	private ConcurrentLinkedQueue<byte[]> devidedDataQueue;
	private static ConcurrentMap<Integer, String> fileUploading;
	private String fileName;
	private static Connections<Packet> connections;

	
	public dataHandler(String action, String fileName, Connections<Packet> pConnections, int connId, ConcurrentMap<Integer, String> pFileUploading){
		this.action = action;
		this.connectionId = connId;
		this.fileName = fileName;
		this.countOfblockExpected = 0;
		this.devidedDataQueue = new ConcurrentLinkedQueue<byte[]>();
		fileUploading = pFileUploading;
		pFileUploading.put(connectionId, fileName);
		connections = pConnections;
		
		
	}

	public String getAction() {
		return this.action;
	}
	
	public void devideRawDataIntoBlocksAndSendFirst(byte[] rawData) {
		int sumOfBlocks = (int) Math.ceil((rawData.length +1)/512);
		short rem = (short) (rawData.length%512);
		for(int i = 0; i< sumOfBlocks-1; i++){
			byte[] dataBlock = new byte[512];
			System.arraycopy(rawData, 512 * i + 0, dataBlock, 0, 512);
			this.devidedDataQueue.add(dataBlock);
		}
		//last packet sent can't be 512 bytes long
			byte[] dataBlock = new byte[rem];
			for(int j = 0; j< dataBlock.length; j++){
				dataBlock[j] = rawData[512*(sumOfBlocks-1) +j];
			this.devidedDataQueue.add(dataBlock);
		}
		Packet pack = new Packet();
		byte[] firstBlock = this.devidedDataQueue.poll();
		pack.createDATApacket((short) firstBlock.length, (short) 1, firstBlock);
		connections.send(connectionId, pack);
	}
	
	public void reset(){
		this.action = "resting";
		this.countOfblockExpected = 0;
		this.devidedDataQueue.clear();
		fileUploading.remove(connectionId);
		this.fileName = "empty";
	}

	public void addToFileUploading(byte[] data) {
		if(data.length > 512){
			Packet pack = new Packet();
			pack.createERRORpacket((short) 1,  "Data packet size above Maximum");
			connections.send(connectionId,pack);
		} else if(data.length == 512) {
			this.devidedDataQueue.add(data);
		} else {
			this.devidedDataQueue.add(data);
			this.uploadFile();
		}
	}

	// uploads the file, sends a broadcast of it and resets the class
	private void uploadFile() {
		byte[] bytes = this.turnQueueToBytes();
		Path path = Paths.get("./Files/",this.fileName);
	    try {
			Files.write(path, bytes);
		} catch (IOException e) {
			Packet pack = new Packet();
			pack.createERRORpacket((short) 1, "couldn't write to disc");
			connections.send(connectionId, pack);
			e.printStackTrace();
		}		Packet pack = new Packet();
	    Packet bcast = new Packet();
		bcast.createBCASTpacket(true, fileName);
		connections.send(connectionId, pack);
		this.reset();		
	}

	//turns the queue of all data blocks to 1 array data
	private byte[] turnQueueToBytes() {
		short rem = (short) (devidedDataQueue.size()%512);
		byte[] fileBytes  = new byte[512*(devidedDataQueue.size()-1) + rem];
		int next = 0;
		while(!devidedDataQueue.isEmpty()){
			byte[] dataBlock =this.devidedDataQueue.poll();
			for (byte aDataBlock : dataBlock) {
				fileBytes[next] = aDataBlock;
				next++;
			}
		}
		return fileBytes;
	}

	protected void setAction(String string) {
		this.action= string;
	}

	public void sendNext() {
		if(!devidedDataQueue.isEmpty())
		{
			byte[] data = this.devidedDataQueue.poll();
			Packet pack = new Packet();
			pack.createDATApacket((short) data.length, (short) this.countOfblockExpected, data);
			connections.send(connectionId, pack);
			this.countOfblockExpected++;
		} else {
			this.reset();
		}
	}
	
}