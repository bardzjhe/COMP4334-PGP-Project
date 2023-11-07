import java.security.*;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 7/11/2023
 * @Description:
 */
public class Alice {

    PublicKey publicKey;
    PrivateKey privateKey;

    public Alice() {
        try{
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            publicKey = pair.getPublic();
            privateKey = pair.getPrivate();
        }catch (NoSuchAlgorithmException ex) {

        }
    }

    public static void main(String[] args) {

    }
}
