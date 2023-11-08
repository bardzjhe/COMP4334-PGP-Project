package Alice;

import java.security.*;

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

    }

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }

    public static void main(String[] args) {

    }
}
