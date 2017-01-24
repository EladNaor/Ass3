package bgu.spl171.net.impl.TFTP;

import java.util.HashMap;

import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.BlockingConnectionHandler;
import bgu.spl171.net.srv.NonBlockingConnectionHandler;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {
	
	private HashMap<Integer, ConnectionHandler<T>> connectionsIdMap;
	private int lastIdGiven = 0;
	
	
	public ConnectionsImpl() {
		this.connectionsIdMap = new HashMap<>();
	}

	@Override
	public boolean send(int connectionId, T msg) {
		boolean massageExists = this.connectionsIdMap.containsKey(connectionId);
		if(massageExists){
			this.connectionsIdMap.get(connectionId).send(msg);
		}
		return massageExists;
		
	}

	@Override
	public void broadcast(T msg) {
		for(Integer connectionId: this.connectionsIdMap.keySet()){
			this.send(connectionId, msg);
		}
	}

	@Override
	public void disconnect(int connectionId) {
		this.connectionsIdMap.remove(connectionId);
	}
	
	public int getLastIdGiven(){
		return this.lastIdGiven;
	}

	public void addConnection(ConnectionHandler handler) {
		this.lastIdGiven++;
		this.connectionsIdMap.put(this.lastIdGiven, handler);
	}


}
