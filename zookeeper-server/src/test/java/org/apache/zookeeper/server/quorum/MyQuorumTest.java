package org.apache.zookeeper.server.quorum;

import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.security.sasl.SaslException;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(value = Parameterized.class)
public class MyQuorumTest  {

    private QuorumPeer peer;
    private boolean isReconfigurable;
    private long mockQuorumVer;
    private Long suggestedLeaderId;
    private Long zxid;
    private boolean restartLE;

    @Parameterized.Parameters
    public static Collection<?> getParameters(){

        return Arrays.asList(new Object[][] {
                /*
                * Leader Election boolean always false in this test because there isn't a real election phase
                * */
                {false,0L,0L,0L,false},
                {true,0L,0L,0L,false},
                {true,1L,0L,0L,false},
                {true,1L,0L,1L,false},
                {true,1L,1L,1L,false},
                {true,1L,-1L,-1L,false},
                {true,1L,null,null,false}

        });
    }

    public MyQuorumTest(boolean isReconfigurable, long mockQuorumVer, Long suggestedLeaderId, Long zxid,boolean restartLE){
        this.isReconfigurable = isReconfigurable;
        this.mockQuorumVer = mockQuorumVer;
        this.suggestedLeaderId= suggestedLeaderId;
        this.zxid = zxid;
        this.restartLE = restartLE;
    }

    @Before
    public void setUp(){
        try {
            QuorumPeerConfig.setReconfigEnabled(isReconfigurable);
            peer = new QuorumPeer();
            QuorumVerifier mockQuorumVr = mock(QuorumVerifier.class);
            when(mockQuorumVr.getVersion()).thenReturn(0L);

            peer.setQuorumVerifier(mockQuorumVr,false);
            peer.setCurrentVote(new Vote(0L,0L));
            /*
            if(restartLE){
                ZKDatabase mockDB = mock(ZKDatabase.class);
                mockDB.setlastProcessedZxid(0L);
                when(mockDB.isInitialized()).thenReturn(true);
                peer.setZKDatabase(mockDB);
                peer.startLeaderElection();
            }
            */

        } catch (SaslException e) {
            fail("Get SaslException: "+ e.getMessage());
        }
    }


    @Test
    public void test(){
        boolean ret;
        QuorumVerifier mockQuorumVr = mock(QuorumVerifier.class);
        when(mockQuorumVr.getVersion()).thenReturn(this.mockQuorumVer);

        ret = peer.processReconfig(mockQuorumVr,this.suggestedLeaderId,this.zxid,this.restartLE);
        if(!this.isReconfigurable){
            assertFalse(ret);
        }else {
            //if suggestedLeaderId equal to 0L the same Vote set in before method expected false
            if (this.suggestedLeaderId != null){
                if (this.suggestedLeaderId == 0L)
                    assertFalse(ret);
                else
                    assertTrue(ret);
            }else {
                //can be null default config
                assertTrue(ret);
            }

        }
    }


}
