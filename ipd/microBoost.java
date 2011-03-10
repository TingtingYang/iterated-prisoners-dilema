import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Class containing a boosting-like algorithm.
 * 
 * @author Tapioca pudding Tapioca pudding. Pudding is cake b/c it tastes better that way.
 *		   Adaptation of M.C. Semeniuk's and C.Wolas' implementations.
 *
 * NOTE: 0 = defect, 1 = cooperate	
 *
 * README: This class represents a strategy attempting to use "weak Classifiers" to predict the opponents's next move. It 
 * looks at a local history (previous 20 moves) that the opponent played and makes a prediction of what a good move would
 * be. This is not a true boosting algorithm, but borrows ideas from boosting to enhance the player's choice. The final
 * "strong classifier" is selected probabilistically based on the weighting from pattern recognition. But if one pattern is
 * more prevalent, that move is more likely to be selected. If not moves are looking decent the algorithm randomly chooses 
 * it's next move.
 * 
 * Each chromosome is evolved by the GA. The Chromosome's encoding is as follows: Each Chromosome is 5 genes 9 bits long.
 * 
 *  	BIT 0: 		Counter move -- This is the predicted move that the player should play.
 *  	BIT 1-4:	Non-encoding region -- This is used to prevent genetic drift.
 *  	BIT 5, 10:	represents 4 possible patterns that the player could search for in the previous history to make a prediction.
 *  	BIT 6-9:	Non-encoding Region -- Used to force more variation in the possible combinations of the 4 patterns.
 *  
 *  	BIT 6:		For relative case it will remember what the move was playing against: copying defect or co-op
 *  																					  opposite defect or co-op
 *  
 *  	Each GA's fitness is tested against a game play by the opponent: Always defect, Always co-op, All strategies, Random
 *  	The 5 best types are what's picked and the best move based upon the greatest weight wins.
 *  
 *  	This Strategy works best with Uniform Crossover.
 *
 */

//This represents the microBoost strategy
public class microBoost extends Strategy
{
	//storage of moves
	public ArrayList<Integer> opponentMoves;							//history of opponent's moves
    public ArrayList<Integer> playerMoves;								//history of the player's moves
    public static Random r = new Random();								//random move if no good move is determined
    
    //encoding indices
    private String chromosome;											//the chromosome of the player's move
    private int counterMoveIndex = 0;									//advocated move that is evolved: location index 1
    private int typeIndex = 5;											//type of the problem: Relative or Absolute
    private int subtypeIndex = 10;										//subtype: Relative: Copy || Opposite
    																	//		Absolute: Defect || Co-op
    //fake enums
    private int COPY = 0, OPPOSITE = 1;									//subtype
    private int ABSOLUTE = 0, RELATIVE = 1;  
    private int DEFECT = 0, COOP = 1;
    
    //used for selecting the winning move
    double randomNumber = r.nextDouble();
	double rWheel = 0.0;
    int indexSelected = 0;
	
    //This controls how far back in the play history we look
    //larger numbers will make the implementation slower for
    //longer plays. This number is super critical.
    private int NUM_STEPS_BACK = 20;									//how many steps back in play history
    private int numGenes = 5;   
    private int geneSize = 11; 
    
	//construct a the microBoost Strategy
	public microBoost() {
		
		name = "SmashCompetitionSpookHorses";							//announce the name of the strategy
		
		//make room for the game plays
		opponentMoves = new ArrayList<Integer>();						
		playerMoves = new ArrayList<Integer>();
		
	}
	
	//determine the next move the player will take
	//this technique is somewhat expensive to implement, but the code from other
	//parts of this program are poor.
	public int nextMove() {
	
		String instruction = "";										//hold current instruction 
		int counterMove = 0;											//current advocated defense
		int subType = 0;												//subtype problem
		int weightedSum= 0;												//used for selection
		int currentMove = r.nextInt(2);									//create random play if no strategy is decent
		int type = 0;													//co-op or rel.
		
		//decision making histories
		ArrayList<Double> weightedValues = new ArrayList<Double>();
		ArrayList<Integer> counterMoveHistory = new ArrayList<Integer>();

		//store previous movements of the local history Don't store too much for speed
		if (playerMoves.size() > NUM_STEPS_BACK + 5){
			playerMoves.clear();
			opponentMoves.clear();
		} else {		
			playerMoves.add(this.myLastMove);							//store the last movements from last game
			opponentMoves.add(this.opponentLastMove);					//store the opponents move from last game
		}
		
		//loop through the chromosome and collect data foreach instruction
		//in order to decide the next move
		for(int i = 0; i < numGenes; i++){
			
			//get instruction and counter move
			instruction = this.getInstruction(i);						
			counterMove = this.getEncoding(instruction, counterMoveIndex);
			
			//look at subtype and add counter move to the local history
			type = getEncoding(instruction, typeIndex);
			subType = this.getEncoding(instruction, subtypeIndex);
						
			//decode pattern matching sequence and build decision
			//making metrics
			switch(type) {
				
				//Absolute
				case 0: 
						//defect || co-op
						weightedValues.add(absolutePattern(subType));
						counterMoveHistory.add(counterMove);
						break;
						
				//Relative
				case 1:
						//copy || opposite
						weightedValues.add(relativePattern(subType));
						//counterMove = getRelativeCounterMove(subType);
						counterMoveHistory.add(counterMove);
						break;
						
				default: break;
			}			
		}	//END OF FOR LOOP
		
		
		/**************************
		 * Normalize Weights
		 * ************************
		 */
		
		//Sum weights
		for (int i = 0; i < weightedValues.size(); i ++) weightedSum += weightedValues.get(i);
				
		//check division by zero
		if (weightedSum != 0.0){
			for (int i = 0; i < weightedValues.size(); i ++)
				weightedValues.set(i, weightedValues.get(i) / weightedSum);
		}
		
		/****************************
		 *  Select Best move based on
		 *  weights.
		 * **************************
		 */
		// Sort Ascending
		Collections.sort(weightedValues);
		for(int i = 0; i < weightedValues.size(); i++){
			rWheel += weightedValues.get(i);
			if (randomNumber < rWheel) {
				indexSelected = i;
				break;
			}
		}
		
		if (counterMoveHistory.size() > 0)
			currentMove = counterMoveHistory.get(indexSelected);
			
		return currentMove;													//random move is selected if no move is decent
	}

	//set the object's move based upon the encoded representation
	public void setMoves(String chromosome){
		
		this.chromosome = "0011001111010101100111101101101001110101110000010101110";
		
	}

	/******************************************************************************
	 *  Private implementations for interpreting the chromosome
	 * ****************************************************************************
	 */
	//return the type
	private int getEncoding(String instruction, int i){
		return instruction.charAt(i)== '1' ? 1 : 0;
	}
		
	//return the instruction from within the chromosome
	private String getInstruction(int i) {
		
		return chromosome.substring(i*geneSize, (i+1)*geneSize);
		
	}
	
	//absolute pattern
	private double absolutePattern(int absType){
		
		double matchCount = 0;
		int movesCount = opponentMoves.size();
		
		//this enforces that we only look at the last 20 moves
		for (int i = ((movesCount - NUM_STEPS_BACK) <= 0)? 0 : movesCount -NUM_STEPS_BACK; i <  movesCount; i++)
			if (absType == opponentMoves.get(i))
				matchCount++;
				
		// How close was it to the pattern?
		return matchCount / movesCount;
	}
	
	//relative pattern
	private double relativePattern(int relType){
		
		double matchCount = 0;
		int movesCount = opponentMoves.size();
				
		//this enforces that we look at only the last 20. Just in case the array lists are big
		for (int i = ((movesCount - NUM_STEPS_BACK) <= 0)? 0 : movesCount -NUM_STEPS_BACK; i < movesCount; i++) {
				
			if (relType == COPY && playerMoves.get(i) == opponentMoves.get(i))
				matchCount++;
			else if (relType == OPPOSITE && playerMoves.get(i) != opponentMoves.get(i))
				matchCount++;
		}
		
		//return fraction of matching
		return matchCount / movesCount;
	}
} 
