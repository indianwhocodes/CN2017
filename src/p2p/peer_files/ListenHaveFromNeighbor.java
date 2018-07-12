package p2p.peer_files;

import java.util.concurrent.Callable;
import java.net.Socket;

import p2p.ByteManipulation;
import p2p.logging.LogFile;
import p2p.messages.Bitfield;
import p2p.messages.Message;

public class ListenHaveFromNeighbor implements Callable<Object> {

    private Neighbor neighbor;
	private LogFile log;
	private Peer currentPeer;

    public ListenHaveFromNeighbor(Neighbor neighbor, LogFile log, Peer currentPeer) {
        this.neighbor = neighbor;
		this.log = log;
		this.currentPeer = currentPeer;
    }

    @Override
    public Object call() throws Exception {
		Message message = new Message();
		Socket haveSocket = neighbor.getHaveSocket();

		while (true) {
			message.receive(haveSocket);
			if (message.getMessageType() == Message.STOP)
				break;

			if (message.getMessageType() == Message.HAVE) {
				byte[] payload = message.getPayload();
				//HAVE message contains the pieceIndex that the neighbor has just received
				int receivedPieceIndex = ByteManipulation.bytes2int(payload);
				Bitfield bitfield = neighbor.getBitfield();
				bitfield.setSingleBit(receivedPieceIndex);
				neighbor.setBitfield(bitfield);
				log.receiveHaveMessage(neighbor.getPeerId(), receivedPieceIndex);

				int missingPieceIndex = currentPeer.getBitfield().getMissingIndex(neighbor.getBitfield());
				if (missingPieceIndex != -1) {
					message.setMessageType(Message.REQUEST);
					message.setPayload(ByteManipulation.int2bytes(missingPieceIndex));
					message.send(neighbor.getUploadSocket());
				}

			}
		}
		return new Object();
    }
}
