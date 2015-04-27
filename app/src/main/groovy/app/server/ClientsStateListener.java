package app.server;

public interface ClientsStateListener {
    public void onClientsReady();
    public void onWaitClients();
}
