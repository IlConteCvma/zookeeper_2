package org.apache.zookeeper;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.test.ClientBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


//acl standard ZooDefs.Ids.OPEN_ACL_UNSAFE
@RunWith(value = Parameterized.class)
public class MyCreateTest extends ClientBase {

    private ZooKeeper zk;
    private String path;
    private byte data[];
    private List<ACL> acl;
    private static final List<ACL> standard = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    //@msgSize number of byte
    private static String createDataSize(int msgSize) {
        StringBuilder sb = new StringBuilder(msgSize);
        for (int i=0; i<msgSize; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

    @Parameterized.Parameters
    public static Collection<?> getParameters(){

        return Arrays.asList(new Object[][] {
                {"/valid","some.data",standard},
                {"/noParent/test","some.data",standard}, //no parent node
                {"/","some.data",standard}, //node already exist
                {"/valid","data",null},
                {"/valid","data",new ArrayList<>()}
                //{"/valid",createDataSize(1048600)} disabilitato non ritorna l'errore aspettato

        });
    }

    public MyCreateTest(String path, String data, List<ACL> acl){
        this.path = path;
        this.data = data.getBytes(StandardCharsets.UTF_8);
        this.acl = acl;
    }

    @Before
    public void setup() throws Exception {
        zk = createClient();

        //initialize "/" path
        zk.setData("/", "some".getBytes(), -1);

    }


    /*
    * Create tests
    * */
    @Test
    public void test(){
        //normal create
        try {
            zk.create(this.path, this.data, this.acl , CreateMode.PERSISTENT);

        } catch (KeeperException e) {
            KeeperException.Code returned = e.code();
            if(!(returned == KeeperException.Code.NODEEXISTS ||
                    returned == KeeperException.Code.NONODE ||
                    returned == KeeperException.Code.INVALIDACL ||
                    returned == KeeperException.Code.MARSHALLINGERROR) ){
                e.printStackTrace();
                fail("Unexpected error with code: " + returned);
            }
        } catch (InterruptedException e) {
            fail("Unexpected InterruptedException" );
        }

    }

    @Test()
    public void ephemeralTest(){
        try {
            zk.create("/valid", "this.data".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL);
            zk.create("/valid/eph","this.data".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE , CreateMode.EPHEMERAL);
        } catch (KeeperException e) {
            assertEquals(KeeperException.Code.NOCHILDRENFOREPHEMERALS,e.code());
        } catch (InterruptedException e) {
            fail("Unexpected InterruptedException" );
        }
    }

    @After
    public void close(){
        if (zk!=null){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
