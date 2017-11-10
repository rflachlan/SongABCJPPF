

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;



//This is a class to read the empirical data from a csv file and process it. It's a bit ugly in places.

public class EmpData {
	
	
	double[][] repSizes;	//a double[][] that includes the frequencies of different repertoire sizes (with index 0 equivalent to rep size 1) for the different populations
	String[] populations;	//array of names of populations
	
	int[][][] songFreqs;
	
	int currentPopulation=0;	//switch indicating population-specific simulations

	
	public EmpData(String fileLocation){
		File file=new File(fileLocation);
		parseDataFile(file, 2);
	}
	
	public void parseDataFile(File file, int type){
		try{
			String cvsSplitBy = ",";
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=null;
			LinkedList<String> pops=new LinkedList<String>();
			LinkedList<Individual> individuals=new LinkedList<Individual>();
			

			while((line=reader.readLine())!=null){
				
				String[] s=line.split(cvsSplitBy);
				
				if (!s[0].equals("Population")){
				
					String name=s[1];
					String pop=s[0];
				
					int[] t=new int[s.length-2];
					for (int i=2; i<s.length; i++){
						t[i-2]=Integer.parseInt(s[i]);
					}

					pops.add(pop);

					
					boolean found=false;
					for (Individual ind: individuals){
						if ((ind.indid.equals(name))&&(ind.popid.equals(pop))){
							ind.addSongToRepertoire(t[0]);
							found=true;
						}
					}
					if (!found){
						Individual ind=new Individual(pop, name, t[0]);
						individuals.add(ind);
					}		
				}
			}
			
			
			String[] popl=new String[10];
			int maxpop=0;
			
			for (String p: pops){
				boolean found=false;
				for (int i=0; i<maxpop; i++){
					//System.out.println(i+" "+popl[i]);
					if (p.equals(popl[i])){
						i=maxpop;
						found=true;
					}
				}
				if (!found){
					popl[maxpop]=p;
					maxpop++;
				}
			}
			
			populations=new String[maxpop];
			
			System.arraycopy(popl, 0, populations, 0, maxpop);
			
			
			songFreqs=new int[maxpop][][];
			
			for (int i=0; i<populations.length; i++){
				String p=populations[i];
				
				int c=0;
				for (Individual ind: individuals){
					if (ind.popid.equals(p)){
						c++;
					}
				}
				
				songFreqs[i]=new int[c][];
				
				c=0;
				for (Individual ind: individuals){
					if (ind.popid.equals(p)){
						songFreqs[i][c]=new int[ind.repSize];
						System.arraycopy(ind.repertoire, 0, songFreqs[i][c], 0, ind.repSize);
						c++;
					}
				}
			}	
			reader.close();
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
	
		
	}

}
