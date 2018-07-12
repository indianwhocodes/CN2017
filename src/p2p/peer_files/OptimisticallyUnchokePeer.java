package p2p.peer_files;

import java.net.Socket;
import java.util.concurrent.Callable;

import p2p.ByteManipulation;
import p2p.file_handling.FileManager;
import p2p.logging.LogFile;
import p2p.messages.Bitfield;
import p2p.messages.Message;
import p2p.messages.Piece;

public class OptimisticallyUnchokePeer implements Callable<Object> {

	private Peer callingPeer;
	private static int TRIALTIME = 10;
	private LogFile logFile;
	private FileManager fileManager;
	private int optimallyUnchokeInterval;

	public OptimisticallyUnchokePeer(Peer callingPeer, LogFile logFile, FileManager fileManager, int optimallyUnchokeInterval) {
		this.callingPeer = callingPeer;
		this.logFile = logFile;
		this.fileManager = fileManager;
		this.optimallyUnchokeInterval = optimallyUnchokeInterval;
	}

	@Override
	public Object call() throws Exception {

		while (true) {
			boolean allPeersDownloaded = true;
			for (Neighbor neighbor : callingPeer.getNeighbors()) {
				if (!neighbor.getBitfield().isFinishedDownloading()) {
					allPeersDownloaded = false;
					break;
				}
			}

			if (allPeersDownloaded) {
				break;
			}

			//randomly search a neighbor
			int randomNeighborIndex = -1;
			boolean isNeighborOptimisticallyUnchoked = false;
			for (int i=0; i<TRIALTIME; i++) {
				randomNeighborIndex = (int) (Math.random() * callingPeer.getNeighbors().size());
				Neighbor neighbor = callingPeer.getNeighbors().get(randomNeighborIndex);
				if ((neighbor.getBitfield().getMissingIndex(callingPeer.getBitfield()) != -1) &&
						neighbor.getChockingState().compareAndSet(0, 2)) {
					isNeighborOptimisticallyUnchoked = true;
					break;
				}
			}

			if (!isNeighborOptimisticallyUnchoked) {
				Thread.sleep(1000);
				continue;
			}

			logFile.optimisticallyUnchokedNeighbor(callingPeer.getNeighbors().get(randomNeighborIndex).getPeerId());

			Neighbor optimallyUnchokedNeighbor;

			// sending unchoked neighbor unchoke msg
			Message unchokeMessage = new Message();
			optimallyUnchokedNeighbor = callingPeer.getNeighbors().get(randomNeighborIndex);
			Socket luckyNeighborDownloadSocket = optimallyUnchokedNeighbor.getDownloadSocket();

			unchokeMessage.setMessageType(Message.UNCHOKE);
			unchokeMessage.setPayload(null);
			unchokeMessage.send(luckyNeighborDownloadSocket);

			long startTime = System.currentTimeMillis();

			while (true) {
				// receive neighbor's response
				unchokeMessage.receive(optimallyUnchokedNeighbor.getUploadSocket());

				// neighbor not interested
				if (unchokeMessage.getMessageType() == Message.NOTINTERESTED) {
					logFile.receiveNotInterested(optimallyUnchokedNeighbor.getPeerId());
					break;
				}

				// neighbor requested something; sending the requested missing piece
				if (unchokeMessage.getMessageType() == Message.REQUEST) {
					int missingPieceIndex = ByteManipulation.bytes2int(unchokeMessage.getPayload());
					Piece missingPiece = fileManager.readChunk(missingPieceIndex);
					unchokeMessage.setMessageType(Message.PIECE);
					unchokeMessage.setPayload(missingPiece.getPayload());
					unchokeMessage.send(luckyNeighborDownloadSocket);
				}

				// after opt unchoke interval
				if ((System.currentTimeMillis() - startTime) > optimallyUnchokeInterval*1000) {
					unchokeMessage.setMessageType(Message.CHOKE);
					unchokeMessage.setPayload(null);
					unchokeMessage.send(luckyNeighborDownloadSocket);
					break;
				}
			}

			callingPeer.getNeighbors().get(randomNeighborIndex).getChockingState().compareAndSet(2, 0);
		}
		return new Object();
	}
}
