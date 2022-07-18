package io.github.sergkhram.ssh;

import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SSHService {
    CopyOnWriteArrayList<SSHClient> sshClients;

    @Value("${ssh.knownHosts.location}")
    private String knownHostsFileLocation;

    public SSHClient setupSshj(
        String remoteHost, Integer remotePort, String userName
    ) throws IOException {
        SSHClient client = new SSHClient();
        File knownHostsFile = new File(knownHostsFileLocation);
        if (knownHostsFile.exists()) {
            client.loadKnownHosts(new File(knownHostsFileLocation));
        } else {
            client.loadKnownHosts();
        }
        if (remotePort != null) {
            client.connect(remoteHost, remotePort);
        } else {
            client.connect(remoteHost);
        }
        client.authPublickey(userName);
//        client.authPassword(userName, password);
        sshClients.add(client);
        return client;
    }

    public void sendFileToSftp(SSHClient sshClient, File localFile, String remoteDir) throws IOException {
        SFTPClient sftpClient = sshClient.newSFTPClient();
        sftpClient.put(localFile.getAbsolutePath(), remoteDir);
        sftpClient.close();
        sshClient.disconnect();
    }

    @PreDestroy
    private void closeAllConnections() {
        sshClients.parallelStream().forEach(
            it -> {
                try {
                    if(it.isConnected()) {
                        log.info("Disconnecting client " + it.getRemoteHostname() + " started");
                        it.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        );
    }

}
