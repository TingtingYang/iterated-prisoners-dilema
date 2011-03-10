/**
 * Class containing iterated Prisoner's Dilemma (IPD).
 * @author	081028AW
 */
public class IteratedPD extends Object
   {
  /**
   * Iterated Prisoner's Dilemma.
   */

   int maxSteps;

   PrisonersDilemma pd;
   Strategy p1, p2;
   int p1Score;
   int p2Score;

   public IteratedPD(Strategy player1, Strategy player2)
      {
      this.p1 = player1;
      this.p2 = player2;

      pd = new PrisonersDilemma(p1, p2);
      p1Score = 0;
      p2Score = 0;
    
      }  /* IteratedPD */

   public void runSteps(int maxSteps)
      {
      int i;

      for (i=0; i<maxSteps; i++)
         {
         pd.playPD();
         p1Score += pd.getPlayer1Payoff();
         p2Score += pd.getPlayer2Payoff();

      }  /* for i */

      }

   public int player1Score()  {return p1Score;}
   public int player2Score()  {return p2Score;}

   }  /* class IteratedPD */

