import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Class containing the random strategy.
 * @author	Michael C. Semeniuk
 */

public class StrategyProbabilisticII extends Strategy
{
	/**
	 * Encoding for a strategy.
	 */
	// 0 = defect, 1 = cooperate
  
	public StrategyProbabilisticII()
	{
		name = "Probabilistic_II";

		opponentMoves = new ArrayList<Integer>();
		playerMoves = new ArrayList<Integer>();

	} 

   public void setMoves(String moveData)
   {
      playerMoveData = moveData;
   }


    public ArrayList<Integer> opponentMoves;
    public ArrayList<Integer> playerMoves;
    public String playerMoveData;
	
	public static Random r = new Random();
	
	public static int FIND_PATTERN_INSTRUCTION = 0, RETURN_INSTRUCTION = 1;
	public static int RELATIVE = 0, ABSOLUTE = 1;
	public static int COOPERATE = 1, DEFECT = 0;
	public static int COPY = 1, OPPOSITE = 0;

	// OPCODE + WEIGHT + REPEAT + INSTRUCTION_TYPE + INSTRUCTION_SUBTYPE + NUM_STEPS_BACK
	public int INSTRUCTION_LENGTH = 9; //1 + 4 + 1 + 1 + 1 + 1;
	public int INSTRUCTION_COUNT = 5;
	
	
	
	public int nextMove()
	{
		// Store all moves
		playerMoves.add(this.myLastMove);
		opponentMoves.add(this.opponentLastMove);
		
		// Select Randomly 0 and 1
		int currentMove = r.nextInt(2);

		//////////////////////////////////////
		// STEP 1: Read in each instruction //
		//////////////////////////////////////

		String instruction ="";
		int opCode = 0;
		double weight = 0.0;
		//boolean repeat = false;
		int instructionType = 0;
		int relativeType = 0;
		int absoluteType = 0;
		int numStepsBack = 0;
		int counterMove  = 0;

		double probability = 0.0;

		// We need to save the weighted probabilities and the move that should be made for each instruct. type.
		ArrayList<Double> weightedProbabilities = new ArrayList<Double>();
		ArrayList<Integer> probabilisticMoves = new ArrayList<Integer>();
		
		for(int i = 0; i < INSTRUCTION_COUNT; i++)
		{
			// i) Fetch next instruction
			instruction = getInstruction(i);
			
			
			// Fetch opcode of instruction
			opCode = 0;//readOpCode(instruction);
			
			if (opCode == FIND_PATTERN_INSTRUCTION)
			{
				weight = readWeight(instruction);
				//repeat = readRepeat(instruction);
				
				instructionType = getInstructionType(instruction);
				
				if (instructionType == ABSOLUTE)
				{
					absoluteType = readAbsoluteType(instruction);
				
					probability = matchAbsolutePattern(absoluteType);
					
					// "STRATEGY FOR ABSOLUTE TYPES" 
					// Always defect, you will get the most points
					probabilisticMoves.add(DEFECT);
				}
				else //if (instructionType == RELATIVE)
				{
					relativeType = readRelativeType(instruction);
					numStepsBack = readNumStepsBack(instruction);
					
					probability = matchRelativePattern(relativeType,numStepsBack);
					
					counterMove = getRelativeCounterMove(relativeType,numStepsBack);
					
					probabilisticMoves.add(counterMove);
				}
				
				
			}
			else //if (opCode == RETURN_INSTRUCTION)
			{
				break;
			}
		

			//////////////////////////////////
			// STEP 2: Save Instructions    //
			//////////////////////////////////
			weightedProbabilities.add(weight * probability);
		}	
		
		
		/////////////////////////////////////////
		// STEP 3: Compute Most Likely Pattern //
		/////////////////////////////////////////
		
		double weightedSum = 0.0;
		
		// Sum weights
		for (int i = 0; i <weightedProbabilities.size(); i ++)
		{
			weightedSum += weightedProbabilities.get(i);
		}
		
		
		// An angel loses its wings every time you divide by 0.
		if (weightedSum != 0.0)
		{
			// Normalize
			for (int i = 0; i <weightedProbabilities.size(); i ++)
			{
				double normalizedWeight = weightedProbabilities.get(i) / weightedSum;
				
				weightedProbabilities.set(i,normalizedWeight);
			}
		}
		
		// Sort Ascending
		Collections.sort(weightedProbabilities);
		
		int indexSelected = 0;
		
		
		// Select instruction from list based on probability
		// Roulette Wheel
		double randomNumber = r.nextDouble();
		double rWheel = 0.0;
		
		for (int i = 0; i <weightedProbabilities.size(); i ++)
		{
			rWheel = rWheel + weightedProbabilities.get(i);
			if (randomNumber < rWheel)
			{
				indexSelected = i;
				break;
			}
		}
		
		/////////////////////////////////////////
		// STEP 4: Execute Selected Move       //
		/////////////////////////////////////////
		if (probabilisticMoves.size() > 0)
		{
			currentMove = probabilisticMoves.get(indexSelected);
		}
		else {} // Our current move will be random 
		
		return currentMove;
	}



	private String getInstruction(int i) 
	{
		int start =   i   * INSTRUCTION_LENGTH;
		int end   = (i+1) * INSTRUCTION_LENGTH;
		
		String value = playerMoveData.substring(start, end);
		
		return value;
	}

	private int getRelativeCounterMove(int relativeType, int numStepsBack)
	{
		// Make sure to provide support so that it doesn't go out of index bounds
		int index = Math.max(0, playerMoves.size()-1-numStepsBack);
		
		int move = playerMoves.get(index);
		
		if (relativeType == OPPOSITE)
		{
			if (move == COOPERATE)
			{
				// will play opposite (DEFECT)- counter with DEFECT for most points
				return DEFECT;
			}
			else //if (move == DEFECT)
			{
				// will play opposite (COOPERATE)- play randomly
				// it is advantageous in the short run to play defect
				// but harmful in the longrun.
				return r.nextInt(2); // RANDOM
			}
			
		}
		else // if (relativeType == COPY)
		{
			if (move == COOPERATE)
			{
				// will play cooperate, this is tit-for-tat so let's maximize points
				return COOPERATE;
			}
			else //if (move == DEFECT)
			{
				// will play defect, we can attempt to break the tit for tat by playing a random
				return  r.nextInt(2); // RANDOM
			}			
		}			
	}
	
	private double matchRelativePattern(int relativeType, int numStepsBack)
	{
		double matchCount = 0;
		double movesCount = opponentMoves.size();
		
		
		// We want to start at index 1 or two so we can check
		// the opponents moves based on the players moves
		// Ex. Tit-for-tat strategies
		for (int i = numStepsBack; i < movesCount; i++)
		{
			if (relativeType == COPY && playerMoves.get(i-numStepsBack) == opponentMoves.get(i))
			{
				matchCount++;
			}
			else if (relativeType == OPPOSITE && playerMoves.get(i-numStepsBack) != opponentMoves.get(i))
			{
				matchCount++;
			}
		}

		// How close was it to the pattern?
		return matchCount / movesCount;
	}

	private double matchAbsolutePattern(int absoluteType)
	{
		double matchCount = 0;
		double movesCount = opponentMoves.size();
		
		for (int i = 0; i <  movesCount; i++)
		{
			if (absoluteType == opponentMoves.get(i))
			{
				matchCount++;
			}
		}
		
		// How close was it to the pattern?
		return matchCount / movesCount;
	}


	////////////////////////////////////////////////////////////////////
	// OPCODE FORMAT                                                  //
	////////////////////////////////////////////////////////////////////
	//       | OPCODE | WEIGHT | TYPE | SUBTYPE | #STEPSBACK | C. MOVE//
	// LEN:  |   1    |   4    |   1  |    1    |    1       |   1    //
	// INDEX:|   0    | 1  - 4 |   5  |    6    |    7       |   8    //
	////////////////////////////////////////////////////////////////////
	//           ^
	//           |
	//           | taken out for now
	
	public static int INDEX_OPCODE = 0;
	public static int INDEX_WEIGHT_BEGIN = 1, INDEX_WEIGHT_END = 4;
	//public static int INDEX_REPEAT = 5;
	public static int INDEX_INSTRUCTION_TYPE = 5;
	public static int INDEX_INSTRUCTION_SUBTYPE = 6;
	public static int INDEX_NUM_STEPS_BACK = 7;
	//public static int INDEX_COUNTER_MOVE = 8;

	// Index 0
	private int readOpCode(String instruction)
	{
		return instruction.charAt(INDEX_OPCODE)== '1' ? 1 : 0; 
	}

	// Index 1 - 4
	private double readWeight(String instruction)
	{
		String weightString = instruction.substring(INDEX_WEIGHT_BEGIN, INDEX_WEIGHT_END+1);
		
		// Unsigned number
		double value = Integer.valueOf(weightString, 2);
		
		// 2 ^ 4 = 16 which can represent from 0 - 15. We want are probability to represent from 0 to 1
		// Therefore we should divide by 15
		
		// Return Inverse 
		return value / 15.0;
	}
	
	// Index obsolete
	/*
	private boolean readRepeat(String instruction)
	{
		return instruction.charAt(INDEX_REPEAT)== '1' ? true : false; 
	}	
	*/
	
	// Index 5
	private int getInstructionType(String instruction) 
	{
		return instruction.charAt(INDEX_INSTRUCTION_TYPE)== '1' ? 1 : 0; 
	}


	// Index 6
	private int readAbsoluteType(String instruction)
	{
		return instruction.charAt(INDEX_INSTRUCTION_SUBTYPE)== '1' ? 1 : 0; 
	}
	
	// Index 6
	private int readRelativeType(String instruction)
	{
		return instruction.charAt(INDEX_INSTRUCTION_SUBTYPE)== '1' ? 1 : 0; 
	}

	// Index 7
	private int readNumStepsBack(String instruction) 
	{
		// Read either 1 or two steps back:
		// Either tit for tat or tit for double tat
		return instruction.charAt(INDEX_NUM_STEPS_BACK)== '1' ? 2 : 1; 
	}

	// Index 8
	/*
	private int readCounterMove(String instruction) 
	{
		return instruction.charAt(INDEX_COUNTER_MOVE)== '1' ? 1 : 0; 
	}
	*/
} 