package bgu.spl171.net.impl.packet;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Packet {
	
	//All fields associated with a given packet
	private short opcode;
	private short blockNumber;
	private String string;
	// true if the packet has 1 byte of 0 there
	private boolean endByte;
	//only for BCAST packet
	private boolean addedOrDeleted;
	//only for DATA packet
	private byte[] data;
	private short packetSize;
	//only for ERROR packet
	private short errCode;

	

	//Client requests to read from server
	private static final short RRQopcode = 1;
	//Client requests to write to server
	private static final short RRWopcode = 2;
	//Client to server upload or server to client download of data;
	private static final short DATAopcode = 3;
	//Acknowledges a msg has been received
	private static final short ACKopcode = 4;
	//Some kind of Error occurred 	
	private static final short ERRORopcode = 5;
	//Return a string list of all files in the File directory(using a data packet to do so)
	private static final short DIRQopcode = 6;
	//logs in to server using a Username 
	private static final short LOGRQopcode = 7;
	//Clients asks to delete a specific file from server
	private static final short DELRQopcode = 8;
	//A server msg to all logged clients notifying a file has been added/deleted
	private static final short BCASTopcode = 9;
	//Client asking server to terminate
	private static final short DISCopcode = 10;
	
	//maximum size of a single data sent through the DATA packet
	private static final int MaxDataBlockByteSize =512;
	
	
	public void createRRQpacket(String filename){
		this.opcode = 1;
		this.string = filename;
		this.endByte = true;
	}
	
	public void createWRQpacket(String filename){
		this.opcode = 2;
		this.string = filename;
		this.endByte = true;
	}
	
	public void createDATApacket(short packetSize, short blockNumber, byte[] data){
		this.opcode=3;
		this.packetSize=packetSize;
		this.blockNumber=blockNumber;
		this.data = new byte[data.length];
		for(int i=0; i<this.data.length; i++)
			this.data[i]=data[i];
		this.endByte = true;
	}
	
	public void createACKpacket(short numOfBlocks){
		this.opcode = 4;
		this.blockNumber = numOfBlocks;
		this.endByte=false;
	}

	public void createERRORpacket(short errCode, String errMsg ){
		this.opcode = 5;
		this.endByte = true;
		this.errCode=errCode;
		this.string=errMsg;
		/* switch(errType){
			case 0: this.string= "Not defined";
			break;
			case 1: this.string= "File not found";
			break;
			case 2: this.string= "Access violation � File cannot be written, read or deleted";
			break;
			case 3: this.string= "Disk full or allocation exceeded � No room in disk";
			break;
			case 4: this.string= "Illegal TFTP operation � Unknown Opcode";
			break;
			case 5: this.string= "File already exists � File name exists on WRQ";
			break;
			case 6: this.string= "User not logged in � Any opcode received before Login completes";
			break;
			case 7: this.string= "User already logged in � Login username already connected";
			break;
		}*/
	}

	public void createDIRQpacket(){
		this.opcode = 6;
		this.endByte = false;
	}
	
	public void createLOGRQpacket(String username){
		this.opcode = 7;
		this.string = username;
		this.endByte = true;
	}
	
	public void createDELRQpacket(String filename){
		this.opcode = 8;
		this.string = filename;
		this.endByte = true;
		
	}
	
	public void createBCASTpacket(boolean isAdded, String filename){
		this.opcode = 9;
		this.string= filename;
		this.addedOrDeleted = isAdded;
		this.endByte = true;
	}
	
	public void createDISCpacket(){
		this.opcode = 10;
		this.endByte = false;
	}
	
	
	public void send(){
		//TODO: send packet
	}

	public short getOpCode() {
		return this.opcode;
	}

	public String getString() {
		return this.string;
	}
	
	public short getBlockNumber(){
		return this.blockNumber;
	}
	public short getPacketSize(){
		return this.packetSize;
	}

<<<<<<< HEAD
	public byte[] getData() {
		return this.data;
	}
	
	
	
=======
	public byte[] getData(){
		return this.data;
	}

	public short getErrCode(){
		return this.errCode;
	}

	public boolean getAddedOrDeleted(){
		return this.addedOrDeleted;
	}



>>>>>>> branch 'master' of https://github.com/EladNaor/Ass3
}

