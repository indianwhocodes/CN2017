package p2p.peer_files;

import java.net.Socket;
import java.util.concurrent.Callable;

import p2p.ByteManipulation;
import p2p.file_handling.FileManager;
import p2p.logging.LogFile;
import p2p.messages.Message;
import p2p.messages.Piece;

public class UnchokePeer implements Callable<Object> {
	
	private Neighbor neighbor;
	private Socket neighborUploadSocket;
	private FileManager fileManager;
	private LogFile logFile;
	private int unchokeInterval;
	private Socket neighborDownloadSocket;

	
	public UnchokePeer(Neighbor neighbor, int unchokeInterval, FileManager fileManager, LogFile logFile) {
		this.neighborUploadSocket = neighbor.getUploadSocket();
		this.neighbor = neighbor;
		this.fileManager = fileManager;
		this.logFile = logFile;
		this.unchokeInterval = unchokeInterval;
		this.neighborDownloadSocket = neighbor.getDownloadSocket();
	}

    @Override
    public Object call() throws Exception {		

		Message message = new Message(Message.UNCHOKE, null);
		message.send(neighborDownloadSocket);
		System.out.println("Unchoke Peer : unchoke message sent to "+neighbor.getPeerId());
		long startTime = System.currentTimeMillis();

		while (true) {
			message.receive(neighborUploadSocket);
			System.out.println("Unchoke Peer : received response from "+neighbor.getPeerId() + " ; "+ message.getMessageType());
			if (message.getMessageType() == Message.NOTINTERESTED) {
				logFile.receiveNotInterested(neighbor.getPeerId());
				break;
			}

			//neighbor sends me REQUEST message(containing missingPieceIndex) if the neighbor is interested
			if (message.getMessageType() == Message.REQUEST) {
				int missingPieceIndex = ByteManipulation.bytes2int(message.getPayload());
				System.out.println("Unchoke Peer : "+neighbor.getPeerId() + " wants piece : "+missingPieceIndex);
				Piece missingPiece = fileManager.readChunk(missingPieceIndex);
				message.setMessageType(Message.PIECE);
				message.setPayload(missingPiece.getPayload());
				message.send(neighborDownloadSocket);
			}

			//choke the neighbor after unchokeInterval
			if ((System.currentTimeMillis() - startTime) > unchokeInterval*1000) {
				neighbor.newPieceDownloaded();
				message.setMessageType(Message.CHOKE);
				message.setPayload(null);
				message.send(neighborDownloadSocket);
				break;
			}
		}
		return new Object();
    }
}
