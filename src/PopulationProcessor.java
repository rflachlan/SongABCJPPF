

public class PopulationProcessor {
	
	int[][] songFreqs;
	
	
	public PopulationProcessor(Individual[] pop){
		
		
		int n=0;
		for (int i=0; i<pop.length; i++){
			if (pop[i].repType>=0){
				n++;
			}
		}
		
		songFreqs=new int[n][];
		
		int j=0;
		for (int i=0; i<pop.length; i++){	
			if (pop[i].repType>=0){
			
				songFreqs[j]=new int[pop[i].repSize];
				System.arraycopy(pop[i].repertoire, 0, songFreqs[j], 0, pop[i].repSize);
				
				for (int a=0; a<songFreqs[j].length; a++){
					for (int b=0; b<a; b++){
						if (songFreqs[j][a]==songFreqs[j][b]){
							System.out.println("ERROR: "+i+" "+j+" "+a+" "+b+" "+songFreqs[j][a]);
						}
					}
				}
				
				j++;
			}
		}	
	
	}

}
