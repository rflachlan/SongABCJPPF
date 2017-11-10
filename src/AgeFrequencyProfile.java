

public class AgeFrequencyProfile {
	
	int indage[]=null;
	double age[]=null;
	int freq[]=null;
	
	public AgeFrequencyProfile(Individual[] population, int index, Parameters p){
		
		int numPossibleSongs=population[0].numPossibleSongs;
		
		freq=new int[numPossibleSongs];
		age=new double[numPossibleSongs];
		
		indage=new int[p.nYears];
		
		for (int i=0; i<population.length; i++){
			if (population[i].repType>=0){
				for (int j=0; j<population[i].repSize; j++){
					if (population[j].repType>=0){
						freq[population[i].repertoire[j]]++;
						age[population[i].repertoire[j]]+=population[i].ages[j];
						indage[population[i].age]++;
					}
				}
			}

		}
		
		for (int i=0; i<numPossibleSongs; i++){
			if (freq[i]>0){
				age[i]/=(freq[i]+0.0);
				//System.out.print(age[i]+" ")
				//System.out.print(freq[i]+" ");
				//System.out.println(index+" "+i+" "+freq[i]+" "+age[i]+" "+population.length);
			}
		}
		//System.out.println();
		//for (int i=0; i<indage.length; i++){
			//if (indage[i]>0){
				//System.out.println(" "+i+" "+indage[i]);
			//}
		//}
		
	}

}
