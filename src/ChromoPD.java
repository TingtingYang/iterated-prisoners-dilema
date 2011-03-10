import java.util.Random;


public class ChromoPD extends Chromo {

	//constructor for a chromosome
	public ChromoPD()
	{
		generateRandomChromosome();
		
		this.rawFitness = -1;   //  Fitness not yet evaluated
		this.sclFitness = -1;   //  Fitness not yet scaled
		this.proFitness = -1;   //  Fitness not yet proportionalized
	}

	//generates a random population
	public void generateRandomChromosome() 
	{
		//  Set gene values to a randum sequence of 0's and 1's
		char geneBit;
		chromo = "";
		for (int i=0; i<Parameters.numGenes; i++){
			for (int j=0; j<Parameters.geneSize; j++){
				randnum = Search.r.nextDouble();
				if (randnum > 0.5) geneBit = '1';
				else geneBit = '0';
				this.chromo = chromo + geneBit;
			}
		}	
	}
	
	public void doMutation()
	{	
		String mutChromo = "";
		char x;

		switch (Parameters.mutationType){

		case 1:     //  Replace with new random number

			for (int j=0; j<(Parameters.geneSize * Parameters.numGenes); j++)
			{
				x = this.chromo.charAt(j);
				randnum = Search.r.nextDouble();
				if (randnum < Parameters.mutationRate)
				{
					if (x == '0') x = '1';
					else x = '1';
				}
				mutChromo = mutChromo + x;
			}
			this.chromo = mutChromo;
			break;

		default:
			System.out.println("ERROR - No mutation method selected");
		}
	}
	
	public int getIntGeneValue(int geneID)
	{
		// NOT IMPLEMENTED, WE DON'T NEED IT FOR THIS PROBLEM
		return 0;
	}
	
	//  Produce a new child from two parents  **********************************
	public void mateWith(Chromo otherParent, Chromo child1, Chromo child2)
	{
		int xoverPoint1;
		
		switch (Parameters.xoverType)
		{

			case 1:     //  Single Point Crossover
	
				//  Select crossover point
				xoverPoint1 = 1 + (int)(Search.r.nextDouble() * (Parameters.numGenes * Parameters.geneSize-1));
	
				//  Create child Chromosome from parental material
				child1.chromo = this.chromo.substring(0,xoverPoint1) + otherParent.chromo.substring(xoverPoint1);
				child2.chromo = otherParent.chromo.substring(0,xoverPoint1) + this.chromo.substring(xoverPoint1);
				break;
	
			case 2:     //  Two Point Crossover
	
			case 3: //uniform crossover    
				
				Random r = new Random();
						
				//go through all the chromosomes. Assume equal length
				for(int i = 0; i < Parameters.numGenes*Parameters.geneSize-1; i++){
					if (r.nextDouble() < 0.3){
						child1.chromo += otherParent.chromo.charAt(i);
						child2.chromo += this.chromo.charAt(i);
					} else {
						child1.chromo += this.chromo.charAt(i);
						child2.chromo += otherParent.chromo.charAt(i);
					}
				}			
				
				break;
				
			default:
				System.out.println("ERROR - Bad crossover method selected");
		}

		//  Set fitness values back to zero
		child1.rawFitness = -1;   //  Fitness not yet evaluated
		child1.sclFitness = -1;   //  Fitness not yet scaled
		child1.proFitness = -1;   //  Fitness not yet proportionalized
		child2.rawFitness = -1;   //  Fitness not yet evaluated
		child2.sclFitness = -1;   //  Fitness not yet scaled
		child2.proFitness = -1;   //  Fitness not yet proportionalized
		
	}
	
	public int getPosIntGeneValue(int geneID)
	{
		// NOT IMPLEMENTED, WE DON'T NEED IT
		return 0;
	}
	
	//  Produce a new child from a single parent  ******************************
	public void mate(Chromo child)
	{
		//  Create child chromosome from parental material
		child.chromo = this.chromo;
				
		//  Set fitness values back to zero
		child.rawFitness = -1;   //  Fitness not yet evaluated
		child.sclFitness = -1;   //  Fitness not yet scaled
		child.proFitness = -1;   //  Fitness not yet proportionalized
	}	

	//  Copy one chromosome to another  ***************************************
	public void copyTo(Chromo targetChromo)
	{
		targetChromo.chromo = this.chromo;
		
		targetChromo.rawFitness = this.rawFitness;
		targetChromo.sclFitness = this.sclFitness;
		targetChromo.proFitness = this.proFitness;		
	}

	@Override
	public double getFloatGeneValue(int geneID){ return 0.0;}	
}
