package bgu.spl171.net.impl.TFTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.management.RuntimeErrorException;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
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
		int sumOfBlocks = (int) Math.ceil((double)(rawData.length +1)/512);
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
		}
		this.devidedDataQueue.add(dataBlock);
		Packet pack = new Packet();
		byte[] firstBlock = this.devidedDataQueue.poll();
		this.countOfblockExpected++;
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
		try {
			FileOutputStream fileOutputStream =
					new FileOutputStream(BidiMessagingProtocolImpl.FilesDir +"/" +fileName,false);
			fileOutputStream.write(bytes);
			fileOutputStream.close();
		} catch (IOException e) {
			Packet pack = new Packet();
			pack.createERRORpacket((short) 1, "couldn't write to disc");
			connections.send(connectionId, pack);
			e.printStackTrace();
		}
	    Packet bcast = new Packet();
		bcast.createBCASTpacket(true, fileName);
		for (Map.Entry<Integer, String> entry : BidiMessagingProtocolImpl.logedInUsersMap.entrySet()) {
			connections.send(entry.getKey(), bcast);
		}
		this.reset();
	}

	//turns the queue of all data blocks to 1 array data
	private byte[] turnQueueToBytes() {
		int size = devidedDataQueue.size();
		byte[] fileBytes  = new byte[512*(devidedDataQueue.size())];
		int next = 0;
		short rem =0;
		while(!devidedDataQueue.isEmpty()){
			byte[] dataBlock =this.devidedDataQueue.poll();
			for (byte aDataBlock : dataBlock) {
				fileBytes[next] = aDataBlock;
				next++;
			}

			if (dataBlock.length < 512)
				rem= (short) dataBlock.length;
		}

		return Arrays.copyOfRange(fileBytes,0, 512*(size -1)+rem);
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