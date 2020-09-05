import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	
	private UTXOPool mUtxoPool;
	
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	
    	mUtxoPool = new UTXOPool(utxoPool);
    }
    
    public UTXOPool getUTXOPool()
    {
    	return mUtxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	
        double totalIn = 0;
        double totalOut = 0;
        ArrayList<UTXO> usedUTXO = new ArrayList<>();

        for (int i=0; i<tx.numInputs(); i++) 
        {
            Transaction.Input input = tx.getInput(i);
            int outputIndex = input.outputIndex;
            byte[] prevTxHash = input.prevTxHash;
            byte[] signature = input.signature;

            UTXO utxo = new UTXO(prevTxHash, outputIndex);

            // check #1: all outputs in transaction are in current UTXO pool
            if (!mUtxoPool.contains(utxo)) return false;
            
            // check #2: the signatures are valid
            Transaction.Output output = mUtxoPool.getTxOutput(utxo);
            byte[] message = tx.getRawDataToSign(i);
            if (!Crypto.verifySignature(output.address,message,signature)) return false;
            
            //check #3: no UTXO is claimed multiple times
            if (usedUTXO.contains(utxo)) return false;
            
            usedUTXO.add(utxo);
            totalIn += output.value;
        }
        
        //check #4: all output values are non-negative
        for (int i=0; i<tx.numOutputs(); i++) 
        {
            Transaction.Output output = tx.getOutput(i);
            if (output.value < 0) return false;
            
            totalOut += output.value;
        }
        //check #5: the sum of input values is greater than or equal to the sum of its output values
        if (totalIn < totalOut) return false;
       
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	
        ArrayList<Transaction> validTxs = new ArrayList<>();
        
        for (Transaction t : possibleTxs) 
        {
            if (isValidTx(t)) 
            {
                validTxs.add(t);

                for (Transaction.Input input : t.getInputs()) 
                {
                    int outputIndex = input.outputIndex;
                    byte[] prevTxHash = input.prevTxHash;
                    UTXO utxo = new UTXO(prevTxHash, outputIndex);
                    mUtxoPool.removeUTXO(utxo);
                }

                byte[] hash = t.getHash();
                for (int i=0;i<t.numOutputs();i++) 
                {
                    UTXO utxo = new UTXO(hash, i);
                    mUtxoPool.addUTXO(utxo, t.getOutput(i));
                }
            }
        }
        
        Transaction[] validTxsArr = new Transaction[validTxs.size()];
        validTxsArr = validTxs.toArray(validTxsArr);
        
        return validTxsArr;
    }

}
