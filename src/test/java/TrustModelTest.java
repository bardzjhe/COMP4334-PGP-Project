import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import trustmodel.TrustLevel;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 28/11/2023
 * @Description:
 */
public class TrustModelTest {

    Server server;
    @BeforeEach
    public void test(){
        server = new Server();

        // Alice fully trusts Bob
        server.addTrust("Alice", "Bob", TrustLevel.FULL);
        // Alice partially trusts Carmen and Jane
        server.addTrust("Alice", "Carmen", TrustLevel.PARTIAL);
        server.addTrust("Alice", "Jane", TrustLevel.PARTIAL);
        // Alice doesn't trust John
        server.addTrust("Alice", "John", TrustLevel.NONE);

        server.addTrust("Bob", "Cali", TrustLevel.FULL);
        server.addTrust("Carmen", "David", TrustLevel.FULL);
        server.addTrust("Jane", "David", TrustLevel.FULL);
        server.addTrust("Jane", "Eve", TrustLevel.FULL);

    }

    @Test
    void testEquals(){

        // Test case 1: Direct knowledge.
        assertEquals(TrustLevel.FULL, server.getTrustLevel("Alice", "Bob"));
        assertEquals(TrustLevel.PARTIAL, server.getTrustLevel("Alice", "Carmen"));
        assertEquals(TrustLevel.NONE, server.getTrustLevel("Alice", "John"));
        // Test case 2: one way test. Alice fully trusts Bob, but not visa versa.
        assertEquals(TrustLevel.NONE, server.getTrustLevel("Bob", "Alice"));

        // Test case 3: Indirect knowledge.
        // Alice fully trusts Bob, and Bob fully trusts Cali, then Alice fully trusts Cali
        assertEquals(TrustLevel.FULL, server.getTrustLevel("Alice", "Cali"));
        // Alice partially trusts Carmen and Jane, and these two fully trusts David.
        // the score is 0.5 + 0.5 = 1,
        // and therefore, Alice will fully trust David according to our design
        assertEquals(TrustLevel.FULL, server.getTrustLevel("Alice", "David"));
        // Alice partially trusts Jane, and Jane fully trusts Eve.
        // the score is 0.5,
        // and therefore, Alice will partially trust Eve
        assertEquals(TrustLevel.PARTIAL, server.getTrustLevel("Alice", "Eve"));
    }


}
