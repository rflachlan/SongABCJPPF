


import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;



//This is a class to read the empirical data from a csv file and process it. It's a bit ugly in places.

public class ElementProcessor {
	
	

	double[] songLengthCounter=new double[20];
	
	double[] songStarter=new double[11];
	double[][] songTrans=new double[11][12];
	double[][] adjTrans=new double[11][12];
	int[][] typeMat;
	int numSongs=0;
	
	XoRoShiRo128PlusRandom random;
	
	
	public ElementProcessor(String fileLocation, XoRoShiRo128PlusRandom random){
		this.random=random;
		File file=new File(fileLocation);
		parseDataFile(file);
		
		reduceFrequency(1);
		recastMatrix(adjTrans);
		simulateSongs(100000, typeMat);
		
		
	}
	
	
	public void reduceFrequency(int threshold){
		for (int i=0; i<11; i++){
			for (int j=0; j<12; j++){
				if (songTrans[i][j]>=threshold){
					adjTrans[i][j]=1;
				}
			}
		}	
		
		for (int i=0; i<adjTrans.length; i++){
			System.out.print(i+" ");
			for (int j=0; j<adjTrans[i].length; j++){
				System.out.print(adjTrans[i][j]+" ");
			}
			System.out.println();
		}
		
	}
	
	public void recastMatrix(double[][] x){
		typeMat=new int[11][];
		
		for (int i=0; i<11; i++){
			int c=0;
			for (int j=0; j<x[i].length; j++){
				if (x[i][j]>0){
					c++;
				}
			}
			typeMat[i]=new int[c];
			c=0;
			for (int j=0; j<x[i].length; j++){
				if (x[i][j]>0){
					typeMat[i][c]=j;
					c++;
				}
			}
			
		}
		
		for (int i=0; i<typeMat.length; i++){
			System.out.print(i+" ");
			for (int j=0; j<typeMat[i].length; j++){
				System.out.print(typeMat[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public void simulateSongs(int nreps, int[][] trans){
		int [][] songtypes=new int[nreps][];
		int [] freq=new int[nreps];
		int maxSongs=0;
		
		for (int i=0; i<nreps; i++){
			//System.out.println(i);
			int[] song=sampleSong(trans);
			int p=checkSongs(song, songtypes, maxSongs);
			if (p==-1){
				songtypes[maxSongs]=song;
				freq[maxSongs]=1;
				maxSongs++;
			}
			else{
				freq[p]++;
			}
		}	
		
		System.out.println("NUMBER TYPES: "+maxSongs);
		int[] x=new int[8];
		for (int i=0; i<maxSongs; i++){
			x[songtypes[i].length]++;
		}
		for (int i=1; i<x.length-1; i++){
			System.out.println(i+" "+x[i+1]+" "+(songLengthCounter[i]-songLengthCounter[i+1]));
		}
	
	}
	
	public int checkSongs(int[] song, int[][] songtypes, int maxSongs){
		int p=-1;
		for (int i=0; i<maxSongs; i++){
			
			if (song.length==songtypes[i].length){
				int q=0;
				for (int j=0; j<song.length; j++){
					if (song[j]!=songtypes[i][j]){q++;}
				}
				if (q==0){
					p=i;
					i=maxSongs;
				}
			}
		}
		return p;
	}
	
	public int[] sampleSong(int[][] trans){	
		boolean succeeded=false;
		int[] out=new int[8];
		double p=random.nextDouble();
		for (int i=0; i<11; i++){
			if (p>songStarter[i]){
				out[0]=i;
				i=11;
			}
		}
		int length=0;
		while (!succeeded){
			length=0;		
			boolean ended=false;
			while(!ended){
				
				int s=trans[out[length]].length;
				if (s==0){
					ended=true;
				}
				else{
					int q=random.nextInt(s);
					int r=trans[out[length]][q];
					length++;
					out[length]=r;
					if (r==11){
						ended=true;
						succeeded=true;
					}
					if (length==out.length-1){
						ended=true;
						succeeded=false;
					}
				}
			}	
		}
		
		length++;
		int[] out2=new int[length];
		System.arraycopy(out, 0, out2, 0, length);
		
		return out2;
	}
	
	public void parseDataFile(File file){
		try{
			String cvsSplitBy = ",";
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=null;
			

			while((line=reader.readLine())!=null){
				
				String[] s=line.split(cvsSplitBy);
				
				if (!s[0].equals("Individual")){
				
					int x=Integer.parseInt(s[3].substring(0,1));
					songLengthCounter[x]++;
					
					int y=Integer.parseInt(s[4]);
					//System.out.println(s[2]+" "+s[3]+" "+s[4]+" "+s[5]);
					if (x==1){
						songStarter[y]++;
						numSongs++;
					}
					if (s.length==5){
						songTrans[y][11]++;
					}
					else{
						int z=Integer.parseInt(s[5]);
						songTrans[y][z]++;
					}	
				}	
			}	
			reader.close();
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		
		for (int i=0; i<11; i++){
			songStarter[i]/=numSongs+0.0;
			System.out.println(i+" "+songStarter[i]);
			if (i>0){
				songStarter[i]+=songStarter[i-1];
			}
		}
		songStarter[10]=1;
		
		for (int i=0; i<11; i++){
			System.out.print(i+" ");
			for (int j=0; j<11; j++){
				System.out.print(songTrans[i][j]+" ");
			}
			System.out.println();
		}
		
		
	}

}
