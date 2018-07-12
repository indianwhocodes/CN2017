package p2p.config_files;

import p2p.peer_files.Peer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

// class to read the properties of PeerInfo.cfg

public class PeerInfoConfigReader {
    private ArrayList<Integer> ids = new ArrayList<Integer>();
    private ArrayList<String> hostnames = new ArrayList<String>();
    private ArrayList<Integer> ports = new ArrayList<Integer>();
    private ArrayList<Boolean> hasFileStatus = new ArrayList<Boolean>();
    private int numPeers = 0;
    //value 1 if it has file ; 0 if it doesn't have file
    private ArrayList<Integer> hasFile = new ArrayList<Integer>();

   // private final ArrayList<Integer> uploadingPorts = new ArrayList<Integer>();
    //private final ArrayList<Integer> hasPorts = new ArrayList<Integer>();

    //initializing variables
    public PeerInfoConfigReader(String peerInfoFile) throws FileNotFoundException {

        Scanner scan = new Scanner(new FileReader(peerInfoFile));

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            line = line.trim();
            String[] values = line.split(" ");
            ids.add(Integer.parseInt(values[0]));
            boolean hasFile = Integer.parseInt(values[3]) == 1 ? true : false;
            hasFileStatus.add(hasFile);
            hostnames.add(values[1]);
            ports.add(Integer.parseInt(values[2]));
            numPeers++;
        }
        scan.close();

    }

//    public ArrayList<Integer> getUploadingPorts() {
//        return uploadingPorts;
//    }
//
//    public ArrayList<Integer> getHasPorts() {
//        return hasPorts;
//    }

    public ArrayList<Integer> getIds() {
        return ids;
    }

    public void setIds(ArrayList<Integer> ids) {
        this.ids = ids;
    }

    public ArrayList<String> getHostnames() {
        return hostnames;
    }

    public void setHostnames(ArrayList<String> hostnames) {
        this.hostnames = hostnames;
    }

    public ArrayList<Integer> getPorts() {
        return ports;
    }

    public void setPorts(ArrayList<Integer> ports) {
        this.ports = ports;
    }

    public ArrayList<Integer> getHasFile() {
        return hasFile;
    }

    public void setHasFile(ArrayList<Integer> hasFile) {
        this.hasFile = hasFile;
    }

    public int getNumPeers() {
        return numPeers;
    }

    public ArrayList<Boolean> getHasFileStatus() {
        return hasFileStatus;
    }
}
