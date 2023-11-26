package Bob;

import model.Client;

import java.io.File;
import java.io.FileOutputStream;
import java.security.*;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 7/11/2023
 * @Description:
 */
public class Bob {
    public Bob() {
        File privateKeyFile = new File("./src/main/java/Bob/BobPrivate.key");
        File publicKeyFile = new File("./src/main/java/publickeys/BobPublic.key");
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

        try(FileOutputStream fos = new FileOutputStream("./src/main/java/publickeys/BobPublic.key")){
            fos.write(pair.getPublic().getEncoded());
        }

        try(FileOutputStream fos = new FileOutputStream("./src/main/java/Bob/BobPrivate.key")){
            fos.write(pair.getPrivate().getEncoded());
        }
    }

    public static void main(String[] args) {
        new Client("Bob");
    }
}
