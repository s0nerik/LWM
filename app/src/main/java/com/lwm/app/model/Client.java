package com.lwm.app.model;

public class Client {

    private long ping = -1;
    private String ip;

    public Client(String ip) {
        this.ip = ip;
    }

    public String getIP() {
        return ip;
    }

    public long getPing() {
        return ping;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }

//    public void setPing(long ping) {
//        if(this.ping != -1) {
//            this.ping = Math.round((this.ping + ping) / 2f);
//        }else{
//            this.ping = ping;
//        }
//    }

    @Override
    public boolean equals(Object o) {
        return hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        int result = 17;
        return 37 * result + (ip == null ? 0 : ip.hashCode());
    }
}
