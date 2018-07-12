package p2p.peer_files;


import p2p.ByteManipulation;
import p2p.file_handling.FileManager;
import p2p.logging.LogFile;
import p2p.messages.Message;
import p2p.messages.Piece;

import java.net.Socket;
import java.util.concurrent.Callable;

class DownloadFromNeighbor implements Callable {

	private Neighbor neighbor;
	private Socket neighborDownloadSocket;
	private Peer callingPeer;
	private FileManager fileManager;
	private LogFile log;
	private Socket neighborUploadSocket;


	public DownloadFromNeighbor(Neighbor neighbor, Peer callingPeer, LogFile log, FileManager fileManager) {
		this.neighbor = neighbor;
		this.neighborDownloadSocket = neighbor.getDownloadSocket();
		this.callingPeer = callingPeer;
		this.log = log;
		this.fileManager = fileManager;
		this.neighborUploadSocket = neighbor.getUploadSocket();

	}

	@Override
	public Object call() throws Exception {
		Message message = new Message();

		while(!callingPeer.getBitfield().isFinishedDownloading()) {
			message.receive(neighborUploadSocket);
			System.out.println("DownloadFromNeighbor: received message from "+neighbor.getPeerId() + " ; "+message.getMessageType());
			if (message.getMessageType() == Message.STOP) {
				for (Neighbor neighbor : callingPeer.getNeighbors()) {
					message.send(neighbor.getHaveSocket());
				}
				break;
			}
			else if (message.getMessageType() == Message.UNCHOKE) {
				System.out.println("DownloadFromNeighbor: received unchoking from "+neighbor.getPeerId() + " ; "+message.getMessageType());
				log.unchoking(neighbor.getPeerId());
				while (true) {
					int missingPieceIndex = callingPeer.getBitfield().getMissingIndex(neighbor.getBitfield());
					if (missingPieceIndex == -1) {
						message.setMessageType(Message.NOTINTERESTED);
						message.setPayload(null);
						message.send(neighborUploadSocket);
						System.out.println("DownloadFromNeighbor: sent not interested message to "+neighbor.getPeerId()+ " ; "+message.getMessageType());
						break;
					}
					else {
						message.setMessageType(Message.REQUEST);
						message.setPayload(ByteManipulation.int2bytes(missingPieceIndex));
						message.send(neighborDownloadSocket);
						System.out.println("DownloadFromNeighbor: sent request message to "+neighbor.getPeerId()+ " ; "+message.getMessageType());
						//Thread.sleep(500);

						message.receive(neighborUploadSocket);
						System.out.println("DownloadFromNeighbor: received response message from "+neighbor.getPeerId()+ " ; "+message.getMessageType());

						// if choked by neighbor
						if (message.getMessageType() == Message.CHOKE) {
							System.out.println("DownloadFromNeighbor : Downloaded pieces when choked : "+ neighbor.getNumDownloadedPieces());
							log.choking(neighbor.getPeerId());
							break;
						}

						System.out.println("DownloadFromNeighbor : Downloaded pieces after choked : "+ neighbor.getNumDownloadedPieces());
						if (message.getMessageType() != Message.CHOKE) {
							Piece missingPiece = new Piece(missingPieceIndex, message.getPayload());
							fileManager.writeChunk(missingPiece);
							callingPeer.getBitfield().setSingleBit(missingPieceIndex);
							neighbor.newPieceDownloaded();
							log.downloadPiece(neighbor.getPeerId(), missingPieceIndex);

							if (callingPeer.getBitfield().isFinishedDownloading()) {
								log.downloadCompletion();
							}

							//send HAVE message to all neighbors so that they can update the
							// info about this peer that it has this Piece
							message.setMessageType(Message.HAVE);
							message.setPayload(ByteManipulation.int2bytes(missingPieceIndex));
							for (Neighbor neighbor : callingPeer.getNeighbors()) {
								message.send(neighbor.getHaveSocket());
							}

						}

					}

				}
			}
		}
		return new Object();
	}
}