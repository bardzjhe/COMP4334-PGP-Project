package Alice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.util.Base64;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 7/11/2023
 * @Description:  save the keypair in the file. 
 *
 * Please refer to https://stackoverflow.com/questions/43960761/how-to-store-and-reuse-keypair-in-java
 */
public class Alice {

    PublicKey publicKey;
    PrivateKey privateKey;

    public Alice() {
        File privateKeyFile = new File("./src/main/java/Alice/AlicePrivate.key");
        File publicKeyFile = new File("./src/main/java/publickeys/AlicePublic.key");
        if(!privateKeyFile.exists() || !publicKeyFile.exists()){
            try{
                generateKeyPair();
            }catch (Exception ex){}
        }
        
    }

    public void generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        // Save public key in PEM format
        try (Writer writer = Files.newBufferedWriter(Paths.get("./src/main/java/publickeys/AlicePublic.pem"))) {
            writer.write("-----BEGIN PUBLIC KEY-----\n");
            writer.write(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
            writer.write("\n-----END PUBLIC KEY-----\n");
        }

        // Save private key in PEM format
        try (Writer writer = Files.newBufferedWriter(Paths.get("./src/main/java/Alice/AlicePrivate.pem"))) {
            writer.write("-----BEGIN PRIVATE KEY-----\n");
            writer.write(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
            writer.write("\n-----END PRIVATE KEY-----\n");
        }
    }

    public void sendMessage(String recipient, String message){
        
    }

    public static void main(String[] args) throws Exception {


    }
}
