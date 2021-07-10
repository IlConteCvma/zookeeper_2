package org.apache.zookeeper;

import org.apache.zookeeper.test.ClientBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(value = Parameterized.class)
public class MyDeleteAsyncTest extends ClientBase {

    private ZooKeeper zk;
    private int returnCode;
    private String returnPath;
    private String path;
    private int version;

    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {
                {"/test/sub",-1}, //normal deletion
                {"/test/sub",1}, //KeeperException with error code KeeperException.BadVersion
                {"/fake",-1},   //KeeperException with error code KeeperException.NoNode
                {"/test",-1},   // KeeperException with error code KeeperException.NotEmpty
                // add after coverage analysis
                {"/",-1}
        });
    }

    public MyDeleteAsyncTest(String path , int version){
        this.path = path;
        this.version = version;
    }

    @Before
    public void setup() throws Exception {
        zk = createClient();
        zk.setData("/", "some".getBytes(), -1);
        zk.create("/test", "don't-care".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create("/test/sub","don't-care".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    public void test() throws InterruptedException {
        AtomicBoolean ctx = new AtomicBoolean(false);
        AsyncCallback.VoidCallback cb = new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx) {
                synchronized (ctx){
                    returnCode = rc;
                    returnPath = path;
                    ((AtomicBoolean)ctx).set(true);
                    ctx.notify();
                }
            }
        };

        zk.delete(path,version,cb,ctx);
        //wait response
        synchronized (ctx){
            while (ctx.get() == false){
                ctx.wait();
            }
        }

        //assert
        if (returnCode == 0){
            assertEquals(path,returnPath);
        }
        else if (returnCode == KeeperException.Code.BADVERSION.intValue()){
            assertEquals(path,returnPath);
        }
        else if (returnCode == KeeperException.Code.NONODE.intValue()){
            assertEquals(path,returnPath);
        }
        else if(returnCode == KeeperException.Code.NOTEMPTY.intValue()){
            assertEquals(path,returnPath);
        }else {
            //not expected error
            fail("Unexpected error: " + returnCode);
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
