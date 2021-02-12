package net.limework.rediskript.data;

import org.bukkit.configuration.Configuration;
import org.cryptomator.siv.SivMode;
import org.cryptomator.siv.UnauthenticCiphertextException;

import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;

public class Encryption {

    private boolean encryptionEnabled;
    private String encryptionKey;
    private String macKey;
    private final SivMode AES_SIV = new SivMode();

    public Encryption(Configuration config){
        encryptionEnabled = config.getBoolean("Redis.EncryptMessages");
        if (encryptionEnabled) {
            // AES encryption
            encryptionKey = config.getString("Redis.EncryptionKey");
            macKey = config.getString("Redis.MacKey");
        }
    }

    public boolean isEncryptionEnabled() { return encryptionEnabled; }

    public String decrypt(byte[] message) throws UnauthenticCiphertextException, IllegalBlockSizeException {
        return new String(AES_SIV.decrypt(encryptionKey.getBytes(StandardCharsets.UTF_8), macKey.getBytes(StandardCharsets.UTF_8), message), StandardCharsets.UTF_8);
    }

    public byte[] encrypt(String message) {
        return AES_SIV.encrypt(encryptionKey.getBytes(StandardCharsets.UTF_8), macKey.getBytes(StandardCharsets.UTF_8), message.getBytes(StandardCharsets.UTF_8));
    }


}
