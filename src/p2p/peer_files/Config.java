package p2p.peer_files;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import p2p.config_files.CommonConfigReader;
import p2p.config_files.PeerInfoConfigReader;


//every peer has its own config
public class Config {
	
	private CommonConfigReader commonConfig;
	private PeerInfoConfigReader peerInfoConfig;
	private int peerId;
	private int numPeers;
	private ArrayList<Peer> peers;

	//read configuration files
	public Config(int peerId) throws UnknownHostException, IOException {
		// TODO Auto-generated constructor stub
		try {
			this.commonConfig = new CommonConfigReader("/Users/jaspreetbajwa/eclipse-workspace/P2P_sample/CN/src/p2p/Common.cfg");
			this.peerInfoConfig = new PeerInfoConfigReader("/Users/jaspreetbajwa/eclipse-workspace/P2P_sample/CN/src/p2p/PeerInfo.cfg");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.peerId = peerId;
		this.peers = new ArrayList<>();

		for (int i=0; i<peerInfoConfig.getNumPeers(); i++) {
			this.peers.add(new Peer(peerInfoConfig.getIds().get(i),
					peerInfoConfig.getHostnames().get(i),
					peerInfoConfig.getPorts().get(i),
					peerInfoConfig.getHasFileStatus().get(i),
					commonConfig.getNumPieces()));
		}

		this.numPeers = this.peers.size();
	}

	public ArrayList<Peer> getPeers() {
		return peers;
	}

	public CommonConfigReader getCommonConfig() {
		return commonConfig;
	}

	public PeerInfoConfigReader getPeerConfig() {
		return peerInfoConfig;
	}

	public int getNumPeers() {
		return numPeers;
	}

}
