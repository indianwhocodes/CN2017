package p2p.peer_files;

import p2p.file_handling.FileManager;
import p2p.logging.LogFile;
import p2p.messages.Message;
import p2p.messages.Stop;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PeerConnectionsHandler implements Runnable {

    private Config config;
    private Peer thisPeer;
    private FileManager fileManager;
    private LogFile logFile;

    public PeerConnectionsHandler(int pId, Config config) throws IOException {
        this.config = config;
        for (int i=0; i<config.getPeers().size(); i++) {
            if (config.getPeers().get(i).getPeerId() == pId) {
                this.thisPeer = config.getPeers().get(i);
            }
        }
        this.fileManager = new FileManager(thisPeer.getPeerId(), config.getCommonConfig());
        this.logFile = new LogFile(thisPeer.getPeerId());
    }

    Socket neighborUploadSocket = null,
            neighborDownloadSocket = null,
            neighborHaveSocket = null;

    ServerSocket downloadServer = null;
    ServerSocket uploadServer = null;
    ServerSocket haveServer = null;

    int thisPeerIndex = -1;

    @Override
    public void run() {

        // If current peer has the file, set all its bitfield bits to true
        if(thisPeer.hasFile()) {
            thisPeer.getBitfield().setAllTrue();
        }


        try {

            actAsClient();

            actAsServer(thisPeerIndex);

            performMessageTransferOperations();

            //close all sockets
            if(thisPeerIndex != config.getNumPeers()-1) {
                downloadServer.close();
                haveServer.close();
                uploadServer.close();
            }

            for (int i=0; i<thisPeer.getNeighbors().size(); i++) {
                neighborDownloadSocket.close();
                neighborUploadSocket.close();
                neighborHaveSocket.close();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    /*
        This peer acts as a client for peers that started before it.
     */
    public int actAsClient() throws Exception{

        //      int thisPeerIndex = -1;
        for (int i=0; i<config.getNumPeers(); i++) {
            thisPeerIndex++;
            if (config.getPeers().get(i) == thisPeer) {
                //thisPeerIndex = i;
                break;
            }

            int neighborIndex = i;

            neighborUploadSocket = new Socket(config.getPeers().get(i).getHostName(),
                    config.getPeers().get(i).getUploadingPort());
            neighborDownloadSocket = new Socket(config.getPeers().get(i).getHostName(),
                    config.getPeers().get(i).getDownloadingPort());
            neighborHaveSocket = new Socket(config.getPeers().get(i).getHostName(),
                    config.getPeers().get(i).getHavePort());

            int neighborPeerId = config.getPeers().get(neighborIndex).getPeerId();

            Neighbor neighbor = new Neighbor(neighborUploadSocket,
                    neighborDownloadSocket,
                    neighborHaveSocket,
                    neighborPeerId);

            logFile.makeConnectionToPeer(neighbor.getPeerId());

            thisPeer.addNeighbor(neighbor);

            thisPeer.initHandshakeAsClient(neighbor, neighborPeerId, logFile);
            thisPeer.initBitfieldAsClient(neighborUploadSocket, thisPeer.getNeighbors().get(neighborIndex), logFile);
            thisPeer.initInterestAsClient(neighbor, neighbor.getBitfield(), logFile);

        }

        return thisPeerIndex;
    }


    /*
        After making connection with peers that started before it,
        current peer acts as server for peers that start after it and establish
        connections with them through downloadSocket.
     */
    public void actAsServer(int thisPeerIndex) throws Exception{

        if(thisPeerIndex != config.getNumPeers()-1) {
            downloadServer = new ServerSocket(thisPeer.getDownloadingPort());
            uploadServer = new ServerSocket(thisPeer.getUploadingPort());
            haveServer = new ServerSocket(thisPeer.getHavePort());

            for (int i=thisPeerIndex; i<config.getNumPeers()-1; i++) {
                Socket downloadSocket = downloadServer.accept();
                Socket uploadSocket = uploadServer.accept();
                Socket haveSocket = haveServer.accept();

                // Current peer while acting as server receives handshake from a neighbor, adds that neighbor
                // to its neighbor list.
                int neighborId = thisPeer.initHandshakeAsServer(downloadSocket, uploadSocket, haveSocket, logFile);
                if (neighborId != thisPeer.getPeerId()) {
                    thisPeer.addNeighbor(new Neighbor(downloadSocket, uploadSocket, haveSocket, neighborId));
                }
                thisPeer.initBitfieldAsServer(downloadSocket, uploadSocket, thisPeer.getNeighbors().get(i), logFile);
                thisPeer.initInterestAsServer(neighborId, downloadSocket, uploadSocket, thisPeer.getNeighbors().get(i), logFile);
            }
        }
    }


    /*
        Initiate Message transfer operations like downloading messages,
        listening have messages from neighbors, unchoke peers etc
     */
    public void performMessageTransferOperations() throws Exception {
        System.out.println(thisPeer.getNeighbors().size());

        ExecutorService downloadThreadpool = Executors.newFixedThreadPool(thisPeer.getNeighbors().size());
        ArrayList<Future> downloadResultFutures = new ArrayList<>();

        ExecutorService findPeerToOptUnchoke = Executors.newSingleThreadExecutor();
        Future<Object> findPeerToOptUnchokeFuture;

        ExecutorService haveThreadPool = Executors.newFixedThreadPool(thisPeer.getNeighbors().size());
        ArrayList<Future> haveResultsFutures = new ArrayList<>();


        // Initiate tasks for downloading pieces and listening have messages for all the neighbors of current peer
        for (int i=0; i<thisPeer.getNeighbors().size(); i++) {

            Neighbor neighbor = thisPeer.getNeighbors().get(i);

            // download from neighbor
            Future<Object> downloadFromNeighborFuture = downloadThreadpool.submit(
                    new DownloadFromNeighbor(thisPeer.getNeighbors().get(i), thisPeer, logFile, fileManager));
            downloadResultFutures.add(downloadFromNeighborFuture);

            //listen have from neighbor
            Future<Object> listenHaveFromNeighborFuture = haveThreadPool.submit(
                    new ListenHaveFromNeighbor(thisPeer.getNeighbors().get(i), logFile, thisPeer));

            haveResultsFutures.add(listenHaveFromNeighborFuture);
        }

        // unchoke peers
        uploadToNeighbors();

        // optimistically unchoke peers
        findPeerToOptUnchokeFuture = findPeerToOptUnchoke.submit(new OptimisticallyUnchokePeer(
                thisPeer, logFile, fileManager, config.getCommonConfig().getOptimisticUnchokingInterval()));

        for (int i=0; i<thisPeer.getNeighbors().size(); i++) {
            downloadResultFutures.get(i).get();
            haveResultsFutures.get(i).get();
        }

        findPeerToOptUnchokeFuture.get();
    }

    public void uploadToNeighbors() throws Exception {

        ArrayList<Neighbor> interestedNeighborsPriorityList = new ArrayList<>();

        ExecutorService uploadThreadPool = Executors.newFixedThreadPool(config.getCommonConfig().getNoOfPreferredNeighbors());
        ArrayList<Future> uploadTasksFutures = new ArrayList<>();

        while(true) {
            int peersDoneDownloading = 0;

            // For all neighbors, check if they have finished downloading.
            for (int i=0; i<thisPeer.getNeighbors().size(); i++) {

                Neighbor neighbor = thisPeer.getNeighbors().get(i);
                //Choke all unchoked neighbors
                neighbor.getChockingState().compareAndSet(1, 0);

                if(neighbor.getBitfield().isFinishedDownloading()) {
                    peersDoneDownloading++;
                }
            }

            // If all neighbors have finished downloading, broadcast stop message to them.
            if(peersDoneDownloading == thisPeer.getNeighbors().size()) {
                Message stopMessage = new Stop();

                // broadcast stop message to all neighbors
                for (int i=0; i<thisPeer.getNeighbors().size(); i++) {
                    stopMessage.send(thisPeer.getNeighbors().get(i).getHaveSocket());
                    stopMessage.send(thisPeer.getNeighbors().get(i).getUploadSocket());
                }
                logFile.stop();
                break;
            }

            // make decreasing order list of neighbors prioritized on the basis of download count
            for (int i=0; i<thisPeer.getNeighbors().size(); i++) {
                Neighbor neighbor = thisPeer.getNeighbors().get(i);

                // if current peer has a piece that neighbor doesn't
                if(neighbor.getBitfield().getMissingIndex(thisPeer.getBitfield()) != -1) {
                    interestedNeighborsPriorityList.add(neighbor);
                }

                if(interestedNeighborsPriorityList.size() > 1) {
                    int prev = i - 1;
                    int current = i;

                    //if current neighbor has better #download count than the previous one,
                    // move the current one ahead and check again
                    while (prev >= 0 &&
                            interestedNeighborsPriorityList.get(current).getNumDownloadedPieces() >
                                    interestedNeighborsPriorityList.get(prev).getNumDownloadedPieces()) {
                        Neighbor chosenNeighbor = interestedNeighborsPriorityList.get(current);
                        interestedNeighborsPriorityList.set(current, interestedNeighborsPriorityList.get(prev));
                        interestedNeighborsPriorityList.set(prev, chosenNeighbor);
                        current = prev;
                        prev--;
                    }
                }
            }

            // if no interested neighbor is found, sleep and startover again
            if(interestedNeighborsPriorityList.size() == 0) {
                Thread.sleep(1000);
                continue;
            }

            //now that preferred neighbor has been decided, clear all neighbors' download count
            for (Neighbor neighbor : thisPeer.getNeighbors()){
                neighbor.setNumDownloadedPieces(0);
            }

            ArrayList<Integer> prefNeighborsIds = new ArrayList<Integer>(); // to keep ids of preferred neighbors

            // For all interested neighbors, submit unchoke tasks
            for (int i=0; i<interestedNeighborsPriorityList.size(); i++) {
                Neighbor neighbor = interestedNeighborsPriorityList.get(i);
                if(i < config.getCommonConfig().getNoOfPreferredNeighbors()) {
                    neighbor.getChockingState().compareAndSet(0, 1);
                    Future<Object> unchokePeerFuture = uploadThreadPool.submit(new UnchokePeer(
                            neighbor, config.getCommonConfig().getUnchokingInterval(), fileManager, logFile));
                    uploadTasksFutures.add(unchokePeerFuture);
                    prefNeighborsIds.add(neighbor.getPeerId());
                }
            }
//
//            if(interestedNeighborsPriorityList.size() > 0){
//                logFile.changeOfPreferredNeighbors(prefNeighborsIds);
//            }

            for (Future uploadTask : uploadTasksFutures) {
                uploadTask.get();
            }
        }
    }
}
