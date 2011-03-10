/******************************************************************************
*  A Teaching GA					  Developed by Hal Stringer & Annie Wu, UCF
*  Version 2, January 18, 2004
*******************************************************************************/

import java.io.*;
import java.util.*;
import java.text.*;

public class FitnessPD extends FitnessFunction{

	public Strategy myStrategy;
	public Strategy theirStrategy;
		
	public int counter = 0;
	public static int PROBABILISTIC = 0, PROBABILISTIC_II = 1, microBoost =2;
	
	//encoding indices
    private int counterMoveIndex = 0;									//advocated move that is evolved: location index 1
    private int typeIndex = 5;											//type of the problem: Relative or Absolute
    private int subtypeIndex = 10;										//subtype: Relative: Copy || Opposite
    																	//		Absolute: Defect || Co-op
    //fake enums
    private int COPY = 0, OPPOSITE = 1;									//subtype
		
	//constructor
	//2 types: Probabilistic or ProbabilisticII both inherit from Strategy
	public FitnessPD () throws java.io.IOException
	{
		//Create instance of the player problem type
		if (Parameters.myStrategy == PROBABILISTIC)
			myStrategy = new StrategyProbabilistic();
		else if (Parameters.myStrategy == PROBABILISTIC_II)
			myStrategy = new StrategyProbabilisticII();
		else if (Parameters.myStrategy == microBoost)
			myStrategy = new microBoost();			
	}

	//raw fitness value based upon the fitness of how the strategy plays.
	public void doRawFitness(Chromo X)
	{
		
		//Translate the "players" chromosome into a strategy
		if (myStrategy instanceof StrategyProbabilistic)
			((StrategyProbabilistic)myStrategy).setMoves(X.chromo);
		else if (myStrategy instanceof StrategyProbabilisticII)			
			((StrategyProbabilisticII)myStrategy).setMoves(X.chromo);
		else if (myStrategy instanceof microBoost)
			((microBoost)myStrategy).setMoves(X.chromo);
		
		//Translate "opposing players" strategy
		switch (Parameters.theirStrategy)
		{
			case 0: // Always Cooperate 
				theirStrategy = new StrategyAlwaysCooperate();
				break;
				
			case 1: // Always Defect 
				theirStrategy = new StrategyAlwaysDefect();
				break;
				
			case 2: // Random
				theirStrategy = new StrategyRandom();
				break;
				
			case 3: // Tit For Tat
				theirStrategy = new StrategyTitForTat();
				break;
				
			case 4: // Tit For Two Tats
				theirStrategy = new StrategyTitForTwoTats();
				break;
			
			case 5: // All Strategies
			
				//calculate an avg score
				int overallFitness = 0;		
				
				//consider all strategies
				Strategy[] theirStrategies = new Strategy[]{ new StrategyAlwaysCooperate(),
						new StrategyAlwaysDefect(),	new StrategyRandom(), new StrategyTitForTat(),
						new StrategyTitForTwoTats()};

				//calculate the overall
				for(int i = 0; i < theirStrategies.length; i++)
					overallFitness += fitnessFunc(X, myStrategy, theirStrategies[i]);
			
				//average raw fitness
				X.rawFitness = overallFitness / theirStrategies.length;
				return;				
		}
		
		//calculate the fitness for 1-4. 5 is above.
		X.rawFitness = fitnessFunc(X, myStrategy, theirStrategy);
	}	

	//print out the gene to output file
	public void doPrintGenes(Chromo X, FileWriter output) throws java.io.IOException
	{
		output.write("Bitcode Data : " + X.chromo + "\n");
		
		//iterate through total number of instructions
		for (int i = 0; i < Parameters.numGenes; i++)
		{
			String instruction = X.getGeneAlpha(i);									//get instruction
			output.write("Strategy "+ i + "\n");									//which no. strategy

			//instruction type
			String instructionType = getType(instruction);							//type: Abs. or Relative
			output.write("Instruction Type: " + instructionType + "\n");
				
			if (instructionType == "ABSOLUTE")
			{	
				output.write("Instruction Subtype: " + readAbsoluteType(instruction) + "\n");
				output.write("Counter Move: " + readCounterMove(instruction) + "\n");					
				
			}
			else // if (instructionType == "RELATIVE")
			{
				String relType = readRelativeType(instruction);
				output.write("Instruction Subtype: " + relType + "\n");
				output.write("Counter Move: " + getRelCounterMove(relType) + "\n");	
				output.write("Counter Move played: " + readCounterMove(instruction) + "\n");
			}
			output.write("\n");
		}
	}	
		
	/****************************************************
	 *  Private methods: For decoding interpretation
	 * **************************************************
	 */
	
	//instruction type
	private String getType(String instruction) 
	{
		return instruction.charAt(typeIndex)== '0' ? "ABSOLUTE" : "RELATIVE"; 
	}

	//subtype for absolute
	private String readAbsoluteType(String instruction)
	{
		return instruction.charAt(subtypeIndex)== '1' ? "COOPERATE" : "DEFECT"; 
	}
	
	//subtype for relative
	private String readRelativeType(String instruction)
	{
		return instruction.charAt(subtypeIndex) == '0' ? "COPY" : "OPPOSITE"; 
	}

	//reads the counter move
	private String readCounterMove(String instruction) 
	{
		return instruction.charAt(counterMoveIndex)== '1' ? "COOPERATE" : "DEFECT"; 
	}
		
	//reads counter move for the relatvie case
	private String getRelCounterMove(String relType){
		
		if (relType == "OPPOSITE")
		{
			return "DEFECT IF RELATIVE MOVE IS COOPERATE / RANDOM IF MOVE IS DEFECT";
		}
		else // if (relativeType == "COPY")
		{
			return "COOPERATE IF RELATIVE MOVE IS COOPERATE / RANDOM IF MOVE IS DEFECT";
		}			
	}
	
	//calculate fitness
	private double fitnessFunc(Chromo X, Strategy myStrategy, Strategy theirStrategy) {
		
		IteratedPD ipd = new IteratedPD(myStrategy, theirStrategy);
		
		//run the IPD for numsteps
		ipd.runSteps(Parameters.numsteps);

		if (Parameters.myStrategy == PROBABILISTIC)
		{
			StrategyProbabilistic stratProb = (StrategyProbabilistic)myStrategy;

			stratProb.setMoves(X.chromo);		
			stratProb.playerMoves.clear();
			stratProb.opponentMoves.clear();
			
		}
		else if (Parameters.myStrategy == PROBABILISTIC_II)
		{
			StrategyProbabilisticII stratProbII = (StrategyProbabilisticII)myStrategy;

			stratProbII.setMoves(X.chromo);
			stratProbII.playerMoves.clear();
			stratProbII.opponentMoves.clear();

		} else if (Parameters.myStrategy == microBoost){
			
			microBoost microProb = (microBoost)myStrategy;

			microProb.setMoves(X.chromo);
			microProb.playerMoves.clear();
			microProb.opponentMoves.clear();
						
		}
		
		return ipd.p1Score;
	}
} 