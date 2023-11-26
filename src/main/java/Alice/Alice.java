package Alice;

import model.Client;

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

    public static void main(String[] args) throws Exception {
        new Client("Alice");
    }
}
