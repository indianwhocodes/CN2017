package p2p.peer_files;

import p2p.messages.Bitfield;
import p2p.messages.Handshake;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Neighbor {

    private int peerId;
    private Socket uploadSocket;
    private Socket downloadSocket;
    private Socket haveSocket;
    private Bitfield bitfield;
    private AtomicInteger chockingState; // 0 : chocked, 1: unchoked, 2: optimistically unchoked
    private AtomicInteger numDownloadedPieces;

    public Neighbor(Socket uploadSocket, Socket downloadSocket, Socket haveSocket, int peerId) {
        this.uploadSocket = uploadSocket;
        this.downloadSocket = downloadSocket;
        this.haveSocket = haveSocket;
        this.peerId = peerId;
        this.bitfield = null;
        this.numDownloadedPieces = new AtomicInteger(-1);
        this.chockingState = new AtomicInteger(0);

    }

    public int getPeerId() {
        return peerId;
    }

    public Socket getUploadSocket() {
        return uploadSocket;
    }

    public Socket getDownloadSocket() {
        return downloadSocket;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public void setUploadSocket(Socket uploadSocket) {
        this.uploadSocket = uploadSocket;
    }

    public void setDownloadSocket(Socket downloadSocket) {
        this.downloadSocket = downloadSocket;
    }

    public void setHaveSocket(Socket haveSocket) {
        this.haveSocket = haveSocket;
    }

    public Bitfield getBitfield() {
        return bitfield;
    }

    public void setBitfield(Bitfield bitfield) {
        this.bitfield = bitfield;
    }

    public Socket getHaveSocket() {
        return haveSocket;
    }

    public AtomicInteger getChockingState() {
        return chockingState;
    }

    public void setChockingState(AtomicInteger chockingState) {
        this.chockingState = chockingState;
    }

    public int getNumDownloadedPieces() {
        return numDownloadedPieces.get();
    }

    public void newPieceDownloaded() {
        numDownloadedPieces.getAndIncrement();
    }

    public void setNumDownloadedPieces(int numDownloadedPieces) {
        this.numDownloadedPieces.set(numDownloadedPieces);
    }
}
