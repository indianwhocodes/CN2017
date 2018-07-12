package p2p.peer_files;

import java.io.IOException;

/*

 */
public class peerProcess {
	public static void main(String args[]) throws Exception{
		//takes peer ID as arguments
		String peerId = args[0];
		Config config = new Config(Integer.parseInt(peerId));
		PeerConnectionsHandler conn = new PeerConnectionsHandler(Integer.parseInt(peerId), config);
		Thread handlePeerConnections = new Thread(conn);
		handlePeerConnections.start();
	}
}
