package org.apache.zookeeper;


import org.apache.zookeeper.test.ClientBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/*
    This is a simple test extract from documentation
* From documentation any unicode character can be used in a path subject to the following constraints:
*
    The null character (\u0000) cannot be part of a path name. (This causes problems with the C binding.)
    The following characters can't be used because they don't display well, or render in confusing ways: \u0001 - \u001F and \u007F
    \u009F.
    The following characters are not allowed: \ud800 - uF8FF, \uFFF0 - uFFFF.
    The "." character can be used as part of another name, but "." and ".." cannot alone be used to indicate a node along a path,
    because ZooKeeper doesn't use relative paths. The following would be invalid: "/a/b/./c" or "/a/b/../c".
    The token "zookeeper" is reserved.

* */

//Using ClientBase class that provide an instance of TestableZooKeeper
@RunWith(value = Parameterized.class)
public class MyPathValidationTest extends ClientBase {

    private ZooKeeper zk;
    private String invalidPath;
    private String validPath;

    @Parameterized.Parameters
    public static Collection<?> getParameters(){

        return Arrays.asList(new Object[][] {
                {"/test/\u0000example","/test"},
                {"/test2/\u007F-example","/valid"},
                {"/test3/","/valid"},
                {"/test4/\ud800-example-","/test-valid-one"}, //part of possible not allowed char
                {"/a/b/./c","/test-valid-separator.dot"},
                {"/a/b/../c","/test-valid-separator..two-dot"}

        });
    }

    public MyPathValidationTest(String invalidPath, String validPath){
        this.invalidPath = invalidPath;
        this.validPath = validPath;
    }

    @Before
    public void setup() throws Exception {
        zk = createClient();
        zk.setData("/", "some".getBytes(), -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPath() throws InterruptedException, KeeperException {
        zk.create(invalidPath, "don't-care".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    public void testValidPath() throws InterruptedException, KeeperException {
        zk.create(validPath, "don't-care".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        List<String> children = zk.getChildren("/", false);
        assertTrue(children.contains(extractName(validPath)));

    }

    private String extractName(String pt){
        return pt.subSequence(1,pt.length()).toString();
    }

    @After
    public void close() {

        if (zk!=null){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
