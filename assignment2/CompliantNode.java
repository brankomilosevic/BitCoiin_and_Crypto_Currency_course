import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private double mGraph, mMalicious, mtxDistribution;
    private boolean[] mFollowees;
    private boolean[] mBlackLists;
    private Set<Transaction> mConsensusTrans = new HashSet<>();
    private Set<Transaction> mPendingTrans = new HashSet<>();
    private int mNumRounds;
    private int mCurrentRound, mPreviousRound = 0;
    
    
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) 
    {
        // IMPLEMENT THIS
    	
    	mGraph = p_graph;
    	mMalicious = p_malicious;
    	mtxDistribution = p_txDistribution;
    	mNumRounds = numRounds;	
    }

    public void setFollowees(boolean[] followees) 
    {
        // IMPLEMENT THIS
    	
    	mFollowees = followees;
    	mBlackLists = new boolean[followees.length];
    	Arrays.fill(mBlackLists, Boolean.FALSE); 	
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) 
    {
        // IMPLEMENT THIS
    	mPendingTrans = pendingTransactions;
    	mConsensusTrans = pendingTransactions;
    	
    }

    public Set<Transaction> sendToFollowers() 
    {
        // IMPLEMENT THIS
    	
        Set<Transaction> Txs = new HashSet<>();
        if (mCurrentRound == mNumRounds) 
        {
            Txs = mConsensusTrans;
        } 
        else if (mCurrentRound < mNumRounds) 
        {
            Txs.addAll(mPendingTrans);
            mPreviousRound = mCurrentRound;
        }
        
        return Txs;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) 
    {
        // IMPLEMENT THIS
    	
        mCurrentRound++;
        
        if (mCurrentRound >= mNumRounds-1) return;
        
        if (mPreviousRound > 0 && mCurrentRound > mPreviousRound) mPendingTrans.clear();
        
        checkMaliciousCandidates(candidates);
        
        for (Candidate c : candidates) 
        {
            if (!mConsensusTrans.contains(c.tx) && !mBlackLists[c.sender]) 
            {
                mConsensusTrans.add(c.tx);
                mPendingTrans.add(c.tx);
            }
        }
    }
    
    private void checkMaliciousCandidates(Set<Candidate> candidates) 
    {
        Set<Integer> senders = candidates.stream().map(c -> c.sender).collect(toSet());
        
        for (int i = 0; i < mFollowees.length; i++) 
            if (mFollowees[i] && !senders.contains(i)) mBlackLists[i] = true;
            
    }   
    
}
