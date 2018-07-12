//package p2p.peer_files;
//
//
//import java.io.*;
//import java.util.*;
//
///*
// * The StartRemotePeers class begins remote peer processes.
// * It reads configuration file PeerInfo.cfg and starts remote peer processes.
// * You must modify this program a little bit if your peer processes are written in C or C++.
// * Please look at the lines below the comment saying IMPORTANT.
// */
//public class StartRemotePeers {
//
//	public Vector<Peer> peerInfoVector;
//
//	public void getConfiguration()
//	{
//		String st;
//		int i1;
//		peerInfoVector = new Vector<Peer>();
//		try {
//			BufferedReader in = new BufferedReader(new FileReader("edu/cise/ufl/p2p/PeerInfo.cfg"));
//			while((st = in.readLine()) != null) {
//
//				 String[] tokens = st.split("\\s+");
//		    	 //System.out.println("tokens begin ----");
//			     //for (int x=0; x<tokens.length; x++) {
//			     //    System.out.println(tokens[x]);
//			     //}
//		         //System.out.println("tokens end ----");
//
//			     peerInfoVector.addElement(new Peer(tokens[0], tokens[1], tokens[2]));
//
//			}
//
//			in.close();
//		}
//		catch (Exception ex) {
//			System.out.println(ex.toString());
//		}
//	}
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		try {
//			StartRemotePeers myStart = new StartRemotePeers();
//			myStart.getConfiguration();
//
//			// get current path
//			String path = System.getProperty("user.dir");
//
//			// start clients at remote hosts
//			for (int i = 0; i < myStart.peerInfoVector.size(); i++) {
//				Peer pInfo = (Peer) myStart.peerInfoVector.elementAt(i);
//
//				System.out.println("Start remote peer " + pInfo.peerId +  " at " + pInfo.peerAddress );
//
//				// *********************** IMPORTANT *************************** //
//				// If your program is JAVA, use this line.
//				Runtime.getRuntime().exec("ssh " + pInfo.peerAddress + " cd " + path + "; java peerProcess " + pInfo.peerId);
//
//				// If your program is C/C++, use this line instead of the above line.
//				//Runtime.getRuntime().exec("ssh " + pInfo.peerAddress + " cd " + path + "; ./peerProcess " + pInfo.peerId);
//			}
//			System.out.println("Starting all remote peers has done." );
//
//		}
//		catch (Exception ex) {
//			System.out.println(ex);
//		}
//	}
//
//}
