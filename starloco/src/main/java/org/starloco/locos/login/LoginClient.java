package org.starloco.locos.login;

import org.starloco.locos.kernel.Config;
import org.starloco.locos.login.packet.PacketHandler;
import org.starloco.locos.object.Account;
import org.apache.mina.core.session.IoSession;

public class LoginClient {

    private final static String POLICY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<cross-domain-policy>"+
                "<site-control permitted-cross-domain-policies=\"all\"/>\n" +
                "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\"/>\n" +
                "<allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>"+
            "</cross-domain-policy>";

    private final IoSession ioSession;
    private final String key;
    private Status status;
    private Account account;
    private byte maitain = 0;

    public LoginClient(IoSession ioSession, String key) {
        this.ioSession = ioSession;
        this.key = key;

        this.send(POLICY);
        this.send("HC" + this.getKey());
        this.setStatus(Status.WAIT_VERSION);
    }

    public void send(Object object) {
        this.ioSession.write(object);
    }

    public void parser(String packet) {
        PacketHandler.parser(this, packet);
    }

    public void kick() {
        if (Config.loginServer.clients.containsKey(this.getAccount().getName()))
            Config.loginServer.clients.remove(this.getAccount().getName());
        this.ioSession.close(true);
    }

    public IoSession getIoSession() {
        return ioSession;
    }

    public String getKey() {
        return key;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public byte getMaintain() {
        return maitain;
    }

    public void setMaintain() {
        this.maitain = 1;
    }

    public enum Status {
        WAIT_VERSION, WAIT_PASSWORD, WAIT_ACCOUNT, WAIT_NICKNAME, SERVER
    }
}
