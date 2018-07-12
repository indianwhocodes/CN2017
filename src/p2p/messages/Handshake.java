package p2p.messages;

import p2p.ByteManipulation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Handshake {

	int senderId;
	String header = "P2PFILESHARINGPROJ";

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	// send to neighbor's download socket
	public void sendHandshake(Socket downloadSocket) throws IOException {
		OutputStream out = downloadSocket.getOutputStream();
		out.write(header.getBytes());
		out.write(new byte[10]);
		out.write(ByteManipulation.int2bytes(senderId));
		out.flush();
	}

	public int getSenderId() {
		return senderId;
	}

	//receive from neighbor's upload socket
	public void receiveHandshake(Socket uploadSocket) throws IOException {
		InputStream in = uploadSocket.getInputStream();
		byte[] handshakeMessage = new byte[28];
		int totalReceived = 0;
		int receivedSoFar = 0;
		while(totalReceived<28){
			receivedSoFar = in.read(handshakeMessage,totalReceived,28-totalReceived);
			totalReceived=totalReceived+receivedSoFar;
		}
		byte[] handshakeId=new byte[4];
		totalReceived=0;
		while(totalReceived<4){
			receivedSoFar=in.read(handshakeId,totalReceived,4-totalReceived);
			totalReceived=totalReceived+receivedSoFar;
		}

		senderId = ByteManipulation.bytes2int(handshakeId);
	}
}
