/******************************************************************************
*  A Teaching GA					  Developed by Hal Stringer & Annie Wu, UCF
*  Version 2, January 18, 2004
*******************************************************************************/

import java.io.*;
import java.util.*;
import java.text.*;

public abstract class Chromo
{
/*******************************************************************************
*                            INSTANCE VARIABLES                                *
*******************************************************************************/

	public String chromo;
	
	
	public double rawFitness;
	public double sclFitness;
	public double proFitness;

/*******************************************************************************
*                            INSTANCE VARIABLES                                *
*******************************************************************************/

	protected static double randnum;

/*******************************************************************************
*                              CONSTRUCTORS                                    *
*******************************************************************************/

	public Chromo()
	{

	}


/*******************************************************************************
*                                MEMBER METHODS                                *
*******************************************************************************/

	//  Get Alpha Represenation of a Gene **************************************

	public String getGeneAlpha(int geneID){
		// Edited to allow for Non-Coding Regions
		int start = geneID*Parameters.geneSize + geneID*Parameters.nonCodingRegion;
		int end = (geneID+1)*Parameters.geneSize + geneID*Parameters.nonCodingRegion;
		String geneAlpha = this.chromo.substring(start, end);
		return (geneAlpha);
	}

	//  Get Integer Value of a Gene (Positive or Negative, 2's Compliment) ****
	public abstract int getIntGeneValue(int geneID);
	
	//  Get Integer Value of a Gene (Positive or Negative, 2's Compliment) ****
	public abstract double getFloatGeneValue(int geneID);
	
	
	//  Get Integer Value of a Gene (Positive only) ****************************
	public abstract int getPosIntGeneValue(int geneID);

	//  Mutate a Chromosome Based on Mutation Type *****************************
	public abstract void doMutation();

	//  Produce a new child from two parents  **********************************
	public abstract void mateWith(Chromo otherParent, Chromo child1, Chromo child2);
	
	//  Produce a new child from a single parent  ******************************
	public abstract void mate(Chromo child);

	//  Copy one chromosome to another  ***************************************
	public abstract void copyTo(Chromo targetChromo);

	public abstract void generateRandomChromosome();
	
/*******************************************************************************
*                             STATIC METHODS                                   *
*******************************************************************************/

	//  Select a parent for crossover ******************************************

	public static int selectParent(){

		double rWheel = 0;
		int j = 0;
		int k = 0;

		switch (Parameters.selectType){

		case 1:     // Proportional Selection
			randnum = Search.r.nextDouble();
			for (j=0; j<Parameters.popSize; j++){
				rWheel = rWheel + Search.member[j].proFitness;
				if (randnum < rWheel) return(j);
			}
			break;

		case 3:     // Random Selection
			randnum = Search.r.nextDouble();
			j = (int) (randnum * Parameters.popSize);
			return(j);

		case 2:     //  Tournament Selection

		default:
			System.out.println("ERROR - No selection method selected");
		}
	return(-1);
	}

}   // End of Chromo.java ******************************************************
