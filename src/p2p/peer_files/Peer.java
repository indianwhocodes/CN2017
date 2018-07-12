package p2p.peer_files;

import p2p.logging.LogFile;
import p2p.messages.Bitfield;
import p2p.messages.Handshake;
import p2p.messages.Interested;
import p2p.messages.Message;
//import sun.rmi.runtime.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Peer {
	private int peerId;
	private String hostName;
	private int downloadingPort;
	private int uploadingPort;
	private int havePort;
	private boolean hasFile;
	private Bitfield bitfield;
	private ArrayList<Neighbor> neighbors;
	int numPieces;

	public Peer(int pId, String pAddress, int peerPort, boolean hasFile, int numPieces) {
		peerId = pId;
		hostName = pAddress;
		downloadingPort = peerPort;
		uploadingPort = peerPort + 1;
		havePort = peerPort + 2;
		this.hasFile = hasFile;
		neighbors = new ArrayList<>();
		bitfield = new Bitfield(numPieces);
		this.numPieces = numPieces;
	}

	// Establish handshaking between peer and neighbor
	public void initHandshakeAsClient(Neighbor neighbor, int neighborPeerId, LogFile logFile) throws IOException, Exception {
		Handshake handshake = new Handshake();

		handshake.setSenderId(peerId);
		handshake.sendHandshake(neighbor.getDownloadSocket());

		logFile.sentHandshake(neighborPeerId);

		handshake.receiveHandshake(neighbor.getUploadSocket());
		logFile.receivedHandshake(neighborPeerId);

		if(handshake.getSenderId() != neighborPeerId) {
			throw new Exception("Handshaking failure!");
		}
	}

	// send bitfield msg after handshake is established
	public void initBitfieldAsClient(Socket neighborUploadSocket, Neighbor neighbor, LogFile logFile) throws IOException {
		Message bitfieldMessage = new Message(Message.BITFIELD, bitfield.toBytes());
		bitfieldMessage.send(neighbor.getDownloadSocket());
		logFile.sentBitfield(neighbor.getPeerId());

		bitfieldMessage.receive(neighborUploadSocket);
		logFile.receiveBitfield(neighbor.getPeerId());

		Bitfield bitfield = new Bitfield(numPieces);
		bitfield.setBoolBitfield(bitfieldMessage.getPayload());
		neighbor.setBitfield(bitfield);

	}

	public void initInterestAsClient(Neighbor neighbor, Bitfield neighborBitfield, LogFile logFile) throws IOException {
		Message interestMessage = new Interested();
		interestMessage.setPayload(null);

		if(bitfield.getMissingIndex(neighborBitfield) != -1) {
			interestMessage.setMessageType(Message.INTERESTED);
		}
		else {
			interestMessage.setMessageType(Message.NOTINTERESTED);
		}
		interestMessage.send(neighbor.getDownloadSocket());
		logFile.sentInterest(neighbor.getPeerId());

		interestMessage.receive(neighbor.getUploadSocket());
		logFile.receiveInterest(neighbor.getPeerId());

		if(interestMessage.getMessageType() == Message.INTERESTED){
			logFile.receiveInterested(neighbor.getPeerId());
		}else{
			logFile.receiveNotInterested(neighbor.getPeerId());
		}
	}

	// returns id of neighbor that sent the handshake
	public int initHandshakeAsServer(Socket downloadSocket, Socket uploadSocket, Socket haveSocket, LogFile logFile) throws IOException {
		Handshake handshake = new Handshake();

		// server wants to receive handshake at its downloadSocket
		handshake.receiveHandshake(downloadSocket);
		int sendingNeighborId = handshake.getSenderId();

		logFile.receivedHandshake(sendingNeighborId);

		// send back a handshake to neighbor
		handshake.setSenderId(this.peerId);

		// server sends handshake at its upload socket
		handshake.sendHandshake(uploadSocket);
		logFile.sentHandshake(sendingNeighborId);

		return sendingNeighborId;
	}

	public void initBitfieldAsServer(Socket downloadSocket, Socket uploadSocket, Neighbor neighbor, LogFile logFile) throws IOException {
		Message bitfieldMessage = new Message();

		// receive the message coming through downloadSocket
		bitfieldMessage.receive(downloadSocket);
		logFile.receiveBitfield(neighbor.getPeerId());
		Bitfield bitfield = new Bitfield(numPieces);

		// if received message is a bitfield, set neighbor's bitfield with recvd payload
		if(bitfieldMessage.getMessageType() == Message.BITFIELD) {
			bitfield.setBoolBitfield(bitfieldMessage.getPayload());
			neighbor.setBitfield(bitfield);
		}

		// Current peer sends its bitfield through downloadSocket
		bitfieldMessage.setMessageType(Message.BITFIELD);
		bitfieldMessage.setPayload(this.bitfield.toBytes());
		bitfieldMessage.send(uploadSocket);
		logFile.sentBitfield(neighbor.getPeerId());
	}

	public void initInterestAsServer(int senderId, Socket downloadSocket, Socket uploadSocket, Neighbor neighbor, LogFile logFile) throws IOException {
		Message interestMessage = new Interested();
		interestMessage.receive(downloadSocket);
		logFile.receiveInterest(senderId);

		if(interestMessage.getMessageType() == Message.INTERESTED) {
			logFile.receiveInterested(senderId);
		} else {
			logFile.receiveNotInterested(senderId);
		}

		interestMessage.setPayload(null);

		// If neighbor has some piece that I don't
		if(this.bitfield.getMissingIndex(neighbor.getBitfield()) != -1) {
			interestMessage.setMessageType(Message.INTERESTED);
		}
		else {
			interestMessage.setMessageType(Message.NOTINTERESTED);
		}
		interestMessage.send(uploadSocket);
		logFile.sentInterest(neighbor.getPeerId());
	}

	public int getPeerId() {
		return peerId;
	}

	public String getHostName() {
		return hostName;
	}

	public boolean hasFile() {
		return hasFile;
	}

	public Bitfield getBitfield() {
		return bitfield;
	}

	public int getDownloadingPort() {
		return downloadingPort;
	}

	public int getUploadingPort() {
		return uploadingPort;
	}

	public int getHavePort() {
		return havePort;
	}

	public void addNeighbor(Neighbor neighbor) {
		neighbors.add(neighbor);
	}

	public ArrayList<Neighbor> getNeighbors() {
		return neighbors;
	}
}
