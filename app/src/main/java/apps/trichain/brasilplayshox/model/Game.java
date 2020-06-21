package apps.trichain.brasilplayshox.model;

import java.io.Serializable;

public class Game implements Serializable {
    private String title;
    private int numPlayers;
    private String server;
    private int pingAddress;
    private boolean isServerOnline;
    private boolean isNew;

    public Game() {
    }

    public Game(String title, int numPlayers, String server, int pingAddress, boolean isServerOnline, boolean isNew) {
        this.title = title;
        this.numPlayers = numPlayers;
        this.server = server;
        this.pingAddress = pingAddress;
        this.isServerOnline = isServerOnline;
        this.isNew = isNew;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPingAddress() {
        return pingAddress;
    }

    public void setPingAddress(int pingAddress) {
        this.pingAddress = pingAddress;
    }

    public boolean isServerOnline() {
        return isServerOnline;
    }

    public void setServerOnline(boolean serverOnline) {
        isServerOnline = serverOnline;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
