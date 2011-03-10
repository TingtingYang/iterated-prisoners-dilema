/******************************************************************************
*  A Teaching GA					  Developed by Hal Stringer & Annie Wu, UCF
*  Version 2, January 18, 2004
*******************************************************************************/

import java.io.*;
import java.util.*;
import java.text.*;

public class Search {

	public static FitnessFunction problem;

	public static Chromo[] member;
	public static Chromo[] child;

	public static Chromo bestOfGenChromo;
	public static int bestOfGenR;
	public static int bestOfGenG;
	public static Chromo bestOfRunChromo;
	public static int bestOfRunR;
	public static int bestOfRunG;
	public static Chromo bestOverAllChromo;
	public static int bestOverAllR;
	public static int bestOverAllG;

	public static double sumRawFitness;
	public static double sumRawFitness2;	// sum of squares of fitness
	public static double sumSclFitness;
	public static double sumProFitness;
	public static double defaultBest;
	public static double defaultWorst;

	public static double averageRawFitness;
	public static double stdevRawFitness;

	public static int G;
	public static int R;
	public static Random r = new Random();
	private static double randnum;

	private static int memberIndex[];
	private static double memberFitness[];
	private static int TmemberIndex;
	private static double TmemberFitness;

	private static double fitnessStats[][];  // 0=Avg, 1=Best

	// Added to find best in all runs
	public static double bestOverAllFitness = 999999999999999999999.0;;
	public static int bestOverAllFitnessRun = 1;
	public static int bestOverAllFitnessGen = 0;
	// SD of average best fitnesses per generation
	public static double averageBestFitnessSD = 0;
	public static double sumAveBestFitness = 0;
	public static double sumAveBestFitness2 = 0;
	public static double averageBestFitness[][];
	public static double sdAveBestList[];
	// SD of average average fitnesses per generation
	public static double averageAveFitnessSD = 0;
	public static double sumAveAveFitness = 0;
	public static double sumAveAveFitness2 = 0;
	public static double averageAveFitness[][];
	public static double sdAveAveList[];
	// Used to find the average and SD of the best fitnesses per run
	public static double bestRunFitnessSD = 0;
	public static double sumBestRunFitness = 0;
	public static double sumBestRunFitness2 = 0;
	public static int bestOfRunFitness[];
	// Used to find average generation of optimum
	public static double aveOptimum = 0;
	public static boolean optimumCount = false;

	//main method for the Search function
	public static void main(String[] args) throws java.io.IOException{

		Calendar dateAndTime = Calendar.getInstance(); 
		Date startTime = dateAndTime.getTime();

	//  Read Parameter File
		System.out.println("\nParameter File Name is: " + args[0] + "\n");
		Parameters parmValues = new Parameters(args[0]);

	//  Write Parameters To Summary Output File
		String summaryFileName = Parameters.expID + "_summary.txt";
		FileWriter summaryOutput = new FileWriter(summaryFileName);
		parmValues.outputParameters(summaryOutput);
		// Files in a format gnuPlot can use
		String summaryFileName2 = "gnuPlot1a.txt";
		FileWriter gnuPlot1a = new FileWriter(summaryFileName2);
		String summaryFileName3 = "gnuPlot1b.txt";
		FileWriter gnuPlot1b = new FileWriter(summaryFileName3);
		String summaryFileName4 = "gnuPlot1ab.txt";
		FileWriter gnuPlot1ab = new FileWriter(summaryFileName4);

	//	Set up Fitness Statistics matrix
		fitnessStats = new double[2][Parameters.generations];
		for (int i=0; i<Parameters.generations; i++){
			fitnessStats[0][i] = 0;
			fitnessStats[1][i] = 0;
		}

		// Generate arrays for information storage
		averageAveFitness = new double[Parameters.numRuns][Parameters.generations];
		averageBestFitness = new double[Parameters.numRuns][Parameters.generations];
		sdAveBestList = new double[Parameters.generations];
		sdAveAveList = new double[Parameters.generations];
		
		// PD Representation
		if (Parameters.problemType.equals("PD")){
			
				//problem representation
				problem = new FitnessPD();
			
				member = new ChromoPD[Parameters.popSize];
				child = new ChromoPD[Parameters.popSize];
				bestOfGenChromo = new ChromoPD();
				bestOfRunChromo = new ChromoPD();
				bestOverAllChromo = new ChromoPD();				
		
		} else System.out.println("Invalid Problem Type");

		System.out.println(problem.name);

		//	Initialize RNG, array sizes and other objects
		r.setSeed(Parameters.seed);
		memberIndex = new int[Parameters.popSize];
		memberFitness = new double[Parameters.popSize];
		
		if (Parameters.minORmax.equals("max")){
			defaultBest = 0;
			defaultWorst = 999999999999999999999.0;
		}
		else{
			defaultBest = 999999999999999999999.0;
			defaultWorst = 0;
		}

		bestOverAllChromo.rawFitness = defaultBest;
		bestOfRunFitness = new int[Parameters.numRuns];

		//  Start program for multiple runs
		for (R = 1; R <= Parameters.numRuns; R++){

			// Initialized to find the optimum
			optimumCount = false;
			bestOfRunChromo.rawFitness = defaultBest;
			System.out.println();

			//	Initialize First Generation
			for (int i=0; i<Parameters.popSize; i++)
			{
				// Prisoners Dilemma Representation
				if (Parameters.problemType.equals("PD"))
				{			
					member[i] = new ChromoPD();
					child[i] = new ChromoPD();
				}
			}

			//	Begin Each Run
			for (G=0; G<Parameters.generations; G++){

				sumProFitness = 0;
				sumSclFitness = 0;
				sumRawFitness = 0;
				sumRawFitness2 = 0;
				bestOfGenChromo.rawFitness = defaultBest;

				//	Test Fitness of Each Member
				for (int i=0; i<Parameters.popSize; i++){

					member[i].rawFitness = 0;
					member[i].sclFitness = 0;
					member[i].proFitness = 0;

					problem.doRawFitness(member[i]);
									
					sumRawFitness = sumRawFitness + member[i].rawFitness;
					sumRawFitness2 = sumRawFitness2 +
						member[i].rawFitness * member[i].rawFitness;

					if (Parameters.minORmax.equals("max")){
						if (member[i].rawFitness > bestOfGenChromo.rawFitness){
							member[i].copyTo(bestOfGenChromo);
							bestOfGenR = R;
							bestOfGenG = G;
						}
						if (member[i].rawFitness > bestOfRunChromo.rawFitness){
							member[i].copyTo(bestOfRunChromo);
							bestOfRunR = R;
							bestOfRunG = G;
						}
						if (member[i].rawFitness > bestOverAllChromo.rawFitness){
							member[i].copyTo(bestOverAllChromo);
							bestOverAllR = R;
							bestOverAllG = G;
						}
					}
					else {
						if (member[i].rawFitness < bestOfGenChromo.rawFitness){
							member[i].copyTo(bestOfGenChromo);
							bestOfGenR = R;
							bestOfGenG = G;
						}
						if (member[i].rawFitness < bestOfRunChromo.rawFitness){
							member[i].copyTo(bestOfRunChromo);
							bestOfRunR = R;
							bestOfRunG = G;
						}
						if (member[i].rawFitness < bestOverAllChromo.rawFitness){
							member[i].copyTo(bestOverAllChromo);
							bestOverAllR = R;
							bestOverAllG = G;
						}
					}
				}

				// Accumulate fitness statistics
				fitnessStats[0][G] += sumRawFitness / Parameters.popSize;
				fitnessStats[1][G] += bestOfGenChromo.rawFitness;

				averageRawFitness = sumRawFitness / Parameters.popSize;
				stdevRawFitness = Math.sqrt(
							Math.abs(sumRawFitness2 - 
							sumRawFitness*sumRawFitness/Parameters.popSize)
							/
							(Parameters.popSize-1)
							);

				// Used to find the best fitness
				for(int i = 0; i < Parameters.popSize; i++)
				{
					if(bestOfGenChromo.rawFitness < bestOverAllFitness)
					{
						// Record current best fitness ever found
						bestOverAllFitness = bestOfGenChromo.rawFitness;
						// Record the run
						bestOverAllFitnessRun = R;
						// Record the generation
						bestOverAllFitnessGen = G;
					}
				}

				if(bestOfGenChromo.rawFitness == 0 && optimumCount == false)
				{
					aveOptimum += bestOverAllFitnessGen;
					optimumCount = true;
				}

				// Record average best and average average fitness to later calculate SDs
				averageBestFitness[R-1][G] = bestOfGenChromo.rawFitness;
				averageAveFitness[R-1][G] = averageRawFitness;


				// Output generation statistics to screen
				System.out.println(R + "\t" + G +  "\t" + (int)bestOfGenChromo.rawFitness + "\t" + averageRawFitness + "\t" + stdevRawFitness);

				// Output generation statistics to summary file
				summaryOutput.write(" R ");
				Hwrite.right(R, 3, summaryOutput);
				summaryOutput.write(" G ");
				Hwrite.right(G, 3, summaryOutput);
				summaryOutput.write(" BF ");
				Hwrite.right((int)bestOfGenChromo.rawFitness, 11, summaryOutput);
				summaryOutput.write(" AF ");
				Hwrite.right(averageRawFitness, 20, 3, summaryOutput);
				summaryOutput.write(" SD ");
				Hwrite.right(stdevRawFitness, 20, 3, summaryOutput);
				summaryOutput.write("\n");
				
		// *********************************************************************
		// **************** SCALE FITNESS OF EACH MEMBER AND SUM ***************
		// *********************************************************************

				switch(Parameters.scaleType){

				case 0:     // No change to raw fitness
					for (int i=0; i<Parameters.popSize; i++){
						member[i].sclFitness = member[i].rawFitness + .000001;
						sumSclFitness += member[i].sclFitness;
					}
					break;

				case 1:     // Fitness not scaled.  Only inverted.
					for (int i=0; i<Parameters.popSize; i++){
						member[i].sclFitness = 1/(member[i].rawFitness + .000001);
						sumSclFitness += member[i].sclFitness;
					}
					break;

				case 2:     // Fitness scaled by Rank (Maximizing fitness)

					//  Copy genetic data to temp array
					for (int i=0; i<Parameters.popSize; i++){
						memberIndex[i] = i;
						memberFitness[i] = member[i].rawFitness;
					}
					//  Bubble Sort the array by floating point number
					for (int i=Parameters.popSize-1; i>0; i--){
						for (int j=0; j<i; j++){
							if (memberFitness[j] > memberFitness[j+1]){
								TmemberIndex = memberIndex[j];
								TmemberFitness = memberFitness[j];
								memberIndex[j] = memberIndex[j+1];
								memberFitness[j] = memberFitness[j+1];
								memberIndex[j+1] = TmemberIndex;
								memberFitness[j+1] = TmemberFitness;
							}
						}
					}
					//  Copy ordered array to scale fitness fields
					for (int i=0; i<Parameters.popSize; i++){
						member[memberIndex[i]].sclFitness = i;
						sumSclFitness += member[memberIndex[i]].sclFitness;
					}

					break;

				case 3:     // Fitness scaled by Rank (minimizing fitness)

					//  Copy genetic data to temp array
					for (int i=0; i<Parameters.popSize; i++){
						memberIndex[i] = i;
						memberFitness[i] = member[i].rawFitness;
					}
					//  Bubble Sort the array by floating point number
					for (int i=1; i<Parameters.popSize; i++){
						for (int j=(Parameters.popSize - 1); j>=i; j--){
							if (memberFitness[j-i] < memberFitness[j]){
								TmemberIndex = memberIndex[j-1];
								TmemberFitness = memberFitness[j-1];
								memberIndex[j-1] = memberIndex[j];
								memberFitness[j-1] = memberFitness[j];
								memberIndex[j] = TmemberIndex;
								memberFitness[j] = TmemberFitness;
							}
						}
					}
					//  Copy array order to scale fitness fields
					for (int i=0; i<Parameters.popSize; i++){
						member[memberIndex[i]].sclFitness = i;
						sumSclFitness += member[memberIndex[i]].sclFitness;
					}

					break;

				default:
					System.out.println("ERROR - No scaling method selected");
				}

		// *********************************************************************
		// ****** PROPORTIONALIZE SCALED FITNESS FOR EACH MEMBER AND SUM *******
		// *********************************************************************

				for (int i=0; i<Parameters.popSize; i++){
					member[i].proFitness = member[i].sclFitness/sumSclFitness;
					sumProFitness = sumProFitness + member[i].proFitness;
				}
				
		// *********************************************************************
		// ************ CROSSOVER AND CREATE NEXT GENERATION *******************
		// *********************************************************************

				int parent1 = -1;
				int parent2 = -1;

				//  Assumes always two offspring per mating
				for (int i=0; i<Parameters.popSize; i=i+2){

					//	Select Two Parents
					parent1 = Chromo.selectParent();
					parent2 = parent1;
					while (parent2 == parent1){
						parent2 = Chromo.selectParent();
					}


					//	Crossover Two Parents to Create Two Children
					randnum = r.nextDouble();
					if (randnum < Parameters.xoverRate)
					{
						member[parent1].mateWith(member[parent2], child[i], child[i+1]);
					}
					else 
					{
						member[parent1].mate(child[i]);
						member[parent2].mate(child[i+1]);
					}				
					
				} // End Crossover

				//	Mutate Children
				for (int i=0; i<Parameters.popSize; i++){
					child[i].doMutation();
				}

				//	Swap Children with Last Generation
				for (int i=0; i<Parameters.popSize; i++)
				{
					child[i].copyTo(member[i]);
					
				}

			} //  Repeat the above loop for each generation

			summaryOutput.write("\n");
			
			Hwrite.right("Best Run : " + bestOfRunR, 20, summaryOutput);
			Hwrite.right("Best Gen : " + bestOfRunG, 20, summaryOutput);

			summaryOutput.write("\n");

			
			problem.doPrintGenes(bestOfRunChromo, summaryOutput);

			System.out.println(R + "\t" + "B" + "\t"+ (int)bestOfRunChromo.rawFitness);

			// Record each best of run fitness
			bestOfRunFitness[R-1] = (int)bestOfRunChromo.rawFitness;

		} //End of a Run

		summaryOutput.write("\n");
		Hwrite.left("Best", 8, summaryOutput);
		summaryOutput.write("\n");

		problem.doPrintGenes(bestOverAllChromo, summaryOutput);

		// Find the SD for average and best values
		for(int i = 0; i < Parameters.generations; i++)
		{
			sumAveAveFitness = 0;
			sumAveAveFitness2 = 0;
			sumAveBestFitness = 0;
			sumAveBestFitness2 = 0;
			for(int j = 0; j < Parameters.numRuns; j++)
			{
				sumAveAveFitness += averageAveFitness[j][i];
				sumAveAveFitness2 += averageAveFitness[j][i]*averageAveFitness[j][i];

				sumAveBestFitness += averageBestFitness[j][i];
				sumAveBestFitness2 += averageBestFitness[j][i]*averageBestFitness[j][i];
			}
			// Calculate and record the SD
			sdAveBestList[i] = Math.sqrt(Math.abs(sumAveBestFitness2 - sumAveBestFitness*sumAveBestFitness/Parameters.numRuns)
							/(Parameters.numRuns-1));
			sdAveAveList[i] = Math.sqrt(Math.abs(sumAveAveFitness2 - sumAveAveFitness*sumAveAveFitness/Parameters.numRuns)
							/(Parameters.numRuns-1));
		}

		//	Output Fitness Statistics matrix
		summaryOutput.write("Gen           AvgFit          AvgBestFit          AvgStdDev          BestStdDev \n");
		for (int i=0; i<Parameters.generations; i++){
			Hwrite.left(i, 15, summaryOutput);
			Hwrite.left(fitnessStats[0][i]/Parameters.numRuns, 20, 2, summaryOutput);
			Hwrite.left(fitnessStats[1][i]/Parameters.numRuns, 20, 2, summaryOutput);
			Hwrite.left(sdAveAveList[i], 20, 2, summaryOutput);
			Hwrite.left(sdAveBestList[i], 20, 2, summaryOutput);
			summaryOutput.write("\n");
		}

		gnuPlot1a.write("Gen           AvgFit          AvgStdDev \n");
		for(int i = 0; i < Parameters.generations; i++)
		{
			Hwrite.left(i, 15, gnuPlot1a);
			Hwrite.left(fitnessStats[0][i]/Parameters.numRuns, 20, 2, gnuPlot1a);
			Hwrite.left(sdAveAveList[i], 20, 2, gnuPlot1a);
			gnuPlot1a.write("\n");
		}
		gnuPlot1a.close();

		gnuPlot1b.write("Gen           AvgBestFit          BestStdDev \n");
		for(int i = 0; i < Parameters.generations; i++)
		{
			Hwrite.left(i, 15, gnuPlot1b);
			Hwrite.left(fitnessStats[1][i]/Parameters.numRuns, 20, 2, gnuPlot1b);
			Hwrite.left(sdAveBestList[i], 20, 2, gnuPlot1b);
			gnuPlot1b.write("\n");
		}
		gnuPlot1b.close();

		gnuPlot1ab.write("Gen           AvgFit          AvgBestFit          AvgStdDev          BestStdDev \n");
		for(int i = 0; i < Parameters.generations; i++)
		{
			Hwrite.left(i, 15, gnuPlot1ab);
			Hwrite.left(fitnessStats[0][i]/Parameters.numRuns, 20, 2, gnuPlot1ab);
			Hwrite.left(fitnessStats[1][i]/Parameters.numRuns, 20, 2, gnuPlot1ab);
			Hwrite.left(sdAveAveList[i], 20, 2, gnuPlot1ab);
			Hwrite.left(sdAveBestList[i], 20, 2, gnuPlot1ab);
			gnuPlot1ab.write("\n");
		}
		gnuPlot1ab.close();

		summaryOutput.write("\n");
		summaryOutput.write("Run          BestFitofRun \n");
		for(int i=0; i<Parameters.numRuns; i++)
		{
			Hwrite.left(i+1, 15, summaryOutput);
			Hwrite.left(bestOfRunFitness[i], 20, 2, summaryOutput);
			summaryOutput.write("\n");

			//sumBestRunFitness
			sumBestRunFitness += bestOfRunFitness[i];
			sumBestRunFitness2 += bestOfRunFitness[i]*bestOfRunFitness[i];
		}

		// BestRunFitness average and fitness
		bestRunFitnessSD = Math.sqrt(Math.abs(sumBestRunFitness2 - sumBestRunFitness*sumBestRunFitness/Parameters.numRuns)
							/(Parameters.numRuns-1));
		summaryOutput.write("Best Fitness of each Run:   Ave:  ");
		Hwrite.left(sumBestRunFitness/Parameters.numRuns, 20, 3, summaryOutput);
		summaryOutput.write("  SD:  ");
		Hwrite.left(bestRunFitnessSD, 20, 3, summaryOutput);
		summaryOutput.write("\n");
		summaryOutput.write("\n");

		// Print out best overall fitness and when it occurred
		if(bestOverAllFitness == 0)
		{
			summaryOutput.write("Optimum Found! ");
			summaryOutput.write("\n");
		}
		else
		{
			summaryOutput.write("Optimum Not Found ");
			summaryOutput.write("\n");
		}
		
		summaryOutput.write("BestOverallFitness:   ");
		Hwrite.left(bestOverAllFitness, 20, 3, summaryOutput);
		summaryOutput.write("   Found in Run:   ");
		Hwrite.right(bestOverAllFitnessRun, 3, summaryOutput);
		summaryOutput.write("   Found in Gen:   ");
		Hwrite.right(bestOverAllFitnessGen, 3, summaryOutput);
		summaryOutput.write("   Ave Gen:   ");
		Hwrite.right(aveOptimum/Parameters.numRuns, 20, 3, summaryOutput);
		summaryOutput.write("\n");
		summaryOutput.write("\n");
	
		
		summaryOutput.write("\n");
		summaryOutput.close();

		System.out.println();
		System.out.println("Start:  " + startTime);
		dateAndTime = Calendar.getInstance(); 
		Date endTime = dateAndTime.getTime();
		System.out.println("End  :  " + endTime);

	} // End of Main Class

}   // End of Search.Java ******************************************************

