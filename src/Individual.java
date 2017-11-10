

import java.io.Serializable;
import java.util.Arrays;


//This is the main class for objects representing individuals - including all song learning behaviour

public class Individual implements Serializable{
	
	private static final long DOUBLE_MASK = (1L << 53) - 1;
    private static final double NORM_53 = 1. / (1L << 53);
    private long state0, state1;

	int modelType=0;
	
	String popid, indid=null;
	
	
	int[] repertoire, newRepertoire, tutorList, ages, newAges;
	
	//repertoire is the current repertoire - each int within the array is a song.
	//newRepertoire is used to construct a new repertoire after individuals are replaced. This is necessary
	//to separate years/generations properly in the simulation (so juveniles don't learn from peers).
	
	//tutorList provides indices within the population for individuals that can serve as tutors
	//(for learningMethod==1 or >4)

	int repSize, newRepSize;
	
	//as with repertoire, there is a current repertoire size, and a value for the newly constructed one
	//after learning. These get merged at the end of each year.
	
	int territory;
	//the id for this individual within the population
	
	//the population object (to which this individual belongs)
	Individual[] population;
	
	//XoRoShiRo128PlusRandom random;
	//Random random;
	//XoRoRNG random;
	
	//Below are a list of characteristics that are taken from the defaults object.
	//see defaults for more info
	//Defaults defaults;
	
	int popSize=100;
	double mortalityRate=0.4;
	
	double repSizes[];
	
	int learningMethod=1;
	
	double mutationRate=0.01;
	double mutationVariance=0.1;
	int numPossibleSongs=200;	
	int numAttractiveSongs=100;
	double rejectSongProportion=0;
	double confBias=1;
	double tutorVar=1;
	int sampleNumber=10;
	int sampleNumber2=10;
	double firststageweight=1;
	double samplePref=0.5;
	
	int maxRep=8;	//this is used to limit array sizes
	int mr;	//maxRep-1 ; to save some simple repetition later
	
	
	int repType=-1;	
	//for the individuals that correspond to the empirical sample, this parameter is set to their repertoire size
	//and that is what their repertoire size is set to in the simulation too.
	//for the individuals that were not included in the empirical sample, this parameter is set to -1, and then 
	//the program sets their repertoire size sampling from the repertoire size distribution
	
	boolean isDead=true;
	//trigger to let program know that an individual needs to be replaced that year.
	
	
	int sampleTypes=0;
	//this records the number of song types sampled by the individual prior to learning (for a record)
	
	//int sampleSize=0;
	//this records the number of songs sampled by the individual prior to learning.
	
	int sampleMax=50;
	//a limit to decide how large to initiate the buffer in which the sampled song types are stored
	
	int[] sample, sampleC, agesC;
	//in most cases, sample is the id of sampled types, and sampleC is their frequency in the sample.
	
	double[] sampleCount, sampleCountCum, songPref, sampleD;
	boolean[] pp;
	//various parameters for normalised versions of type frequencies.
	
	double[] powerLookUp=new double[108];
	//for conformity bias, a count is raised to a power. Rather than calculating that each time, I simply create a
	//look-up array.
	
	boolean[] isMutation;
	
	double tutorAbility=1;
	double[] popTutorAbilities=null;
	
	boolean[] deletelist;
	
	int age=0;
	
	
	public Individual(String popid, String indid, int song){
		this.popid=popid;
		this.indid=indid;
		repSize=1;
		repType=1;
		repertoire=new int[1];
		repertoire[0]=song;
	}
	
	public Individual(Individual ind){
		this.popid=ind.popid;
		this.indid=ind.indid;
		this.repSize=ind.repSize;
		this.modelType=ind.modelType;
		repertoire=new int[repSize];
		ages=new int[repSize];
		agesC=new int[repSize];
		System.arraycopy(ind.repertoire, 0, repertoire, 0, repSize);
		System.arraycopy(ind.ages, 0, ages, 0, repSize);
	}
	
	//public Individual(int territory, XoRoShiRo128PlusRandom random, Individual[] population, Parameters param, int repType, double[] songPref){
		
	//public Individual(int territory, Random random, Individual[] population, Parameters param, int repType, double[] songPref){
	
	public Individual(int territory, long seed, Individual[] population, Parameters param, int repType, double[] songPref2){
			
		this.territory=territory;
		this.population=population;
		//this.random=random;
		setSeed(seed);
		this.repType=repType;
			
		this.modelType=param.modelType;
		this.mortalityRate=param.mortalityRate;
		this.mutationRate=param.mutationRate;
		this.numPossibleSongs=(int)Math.round(param.numPossibleSongs);
		this.sampleNumber=(int)Math.round(param.sampleNumber);
		this.sampleNumber2=(int)Math.round(param.sampleNumber2);
		this.firststageweight=param.firstStageWeight;
		this.confBias=param.confBias;
		this.tutorVar=param.tutorVar;
		
		this.songPref=new double[songPref2.length];
		System.arraycopy(songPref2, 0, songPref, 0, songPref.length);		
		
		maxRep=param.repSizes.length;
		
		//if (modelType==2){
			//if ()
		//}
		
		sampleC=new int[numPossibleSongs+16];
		sampleD=new double[numPossibleSongs+16];		
		agesC=new int[numPossibleSongs+16];
		deletelist=new boolean[numPossibleSongs+16];
		
		sampleMax=sampleNumber*maxRep;
		sample=new int[this.sampleMax+16];
		sampleCount=new double[numPossibleSongs+16];
		sampleCountCum=new double[numPossibleSongs+16];
		pp=new boolean[numPossibleSongs+16];
		
		
		popSize=population.length;
		
		this.repSizes=new double[maxRep+8];
		repertoire=new int[maxRep+16];
		newRepertoire=new int[maxRep+16];
		//agesC=new int[sampleMax];
		ages=new int[maxRep+16];
		newAges=new int[maxRep+16];
		isMutation=new boolean[maxRep+16];
		
		repSizes[0]=param.repSizes[0];
		double avrepsize=0;
		double aa=0;
		for (int i=1; i<maxRep; i++){
			repSizes[i]=param.repSizes[i]+repSizes[i-1];
			avrepsize+=i*param.repSizes[i];
			aa+=param.repSizes[i];
			//System.out.println(i+" "+repSizes[i]);
		}
		double rs1=repSizes[maxRep-1];	
		avrepsize/=aa;
		
		double xx=0;
		if (modelType==4){xx=firststageweight*avrepsize-1;}
		for (int i=0; i<powerLookUp.length; i++){
			powerLookUp[i]=Math.pow(i+xx, confBias);
			if (modelType==4){
				powerLookUp[i]=1/powerLookUp[i];
			}
		}
		
		
		for (int i=0; i<maxRep; i++){
			repSizes[i]/=rs1;
		}
		mr=maxRep-1;
		
		setRepertoireSize();
		initiateRepertoire();
		updateRepertoire();
	}
	
	public void addSongToRepertoire(int newSong){
		
		int ns=repSize+1;
		int[] newRep=new int[ns];
		System.arraycopy(repertoire, 0, newRep, 0, repSize);
		newRep[ns-1]=newSong;	
		repSize=ns;
		repertoire=newRep;		
	}
	
	
	
	
	
	
	//decides whether individual dies that year or not.
	public void mortality(){
		if (nextDouble()<mortalityRate){
			isDead=true;
		}
		else{
			isDead=false;
		}
	}
	
	public void setNewTutorAbility(){
		tutorAbility=Math.exp(nextGaussian()*tutorVar);
		
		//System.out.println(tutorVar+" "+tutorAbility);
	}
	
	public void setTutorAbilities(double[] x){
		popTutorAbilities=x;
	}
	
	
	//at beginning of simulation run, repertoires are initiated with randomly selected values.
	public void initiateRepertoire(){
		for (int i=0; i<newRepSize; i++){
			newRepertoire[i]=nextInt(numPossibleSongs);
			//System.out.println("New song: "+newRepertoire[i]);
			ages[i]=0;
		}
	}

	
	
	//at the end of each year, each replaced individual's song values are updated to their newly calculated ones
	public void updateRepertoire(){
		if (isDead){
			for (int i=0; i<newRepSize; i++){
				repertoire[i]=newRepertoire[i];
				ages[i]=newAges[i];
			}
			repSize=newRepSize;
			setNewTutorAbility();
		}
	}

	//in this function, individuals learn songs. First they are assigned a repertoire size. Then that
	//repertoire is filled according to the learningMethod.
		
	
	
	public void learnSongs(int year){
		if (isDead){
			setRepertoireSize();
			if (modelType==4){
				buildRepertoireTwoStageCombine2();
			}
			else if (modelType==3){
				buildRepertoireTwoStageCombine();
			}
			else if (modelType==2){
				buildRepertoireTwoStage();
			}
			else if (modelType==1){
				buildRepertoireCombine4();
			}
			else{
				buildRepertoireSimple();
			}
			mutate(year);
			age=year;
		}
	}
	
	
	
	//simple function to determine repertoire size for new male after replacement.
	//this depends on whether you are from the original empirically sampled set (repType>=0) or not.
	
	public void setRepertoireSize(){
		if (repType<0){
			double x=nextDouble();
			newRepSize=maxRep;
			for (int i=1; i<mr; i++){
				if(x<repSizes[i]){
					newRepSize=i;
					i=mr;
				}
			}
			//System.out.println(newRepSize);
		}
		else{
			newRepSize=repType;
		}
		
		if (newRepSize>7){
			for (int i=0; i<repSizes.length; i++){
				System.out.println(mr+" "+i+" "+repSizes[i]+" "+repType);
			}
		}
		
	}

	
	//Simplest form of song learning...
	
	public void buildRepertoireSimple(){
		for (int i=0; i<newRepSize; i++){
			boolean found=true;
			int checker=maxRep*100;
			int c2=0;
			while (found){
				found=false;
				int t=nextInt(popSize);
				int u=nextInt(population[t].repSize);
				int h=population[t].repertoire[u];
				
				for (int j=0; j<i; j++){				
					if (h==newRepertoire[j]){
						found=true;
						j=i;
					}
				}
				if (!found){
					newRepertoire[i]=h;
					//if (mutationRate>nextDouble()){
						//newRepertoire[i]=nextInt(numPossibleSongs);
					//}
				}
				c2++;
				if ((found)&&(c2==checker)){
					found=false;
					newRepertoire[i]=nextInt(numPossibleSongs);
				}
			}
		}
	}

	
	public void buildRepertoireCombine4(){
				
		int c=sampleSongs();
		sampleTypes=c;
		if (c<newRepSize){newRepSize=c;}
		weightSongs(c);
		pickSongs(c);
		
	}
	
	public void buildRepertoireCombine4S(){
		int u, x;
		double v;	
		int c=0;
		
		
		Arrays.fill(sampleC, 0);
		
		for (int i=0; i<sampleNumber; i++){

			Individual ind=population[nextInt(popSize)];
			
			u=ind.repSize;
			v=ind.tutorAbility;
			
			for (int j=0; j<u; j++){
				x=ind.repertoire[j];
				if (sampleC[x]==0){
					sample[c]=x;
					agesC[c]=ind.ages[j];
					sampleD[x]=0;
					agesC[x]=0;
					c++;
				}
				sampleC[x]++;
				sampleD[x]+=v;
			}			
		}
		sampleTypes=c;
		if (c<newRepSize){newRepSize=c;}
		
		for (int i=0; i<c; i++){
			int y=sample[i];
			int z=sampleC[y];
			sampleCount[i]=powerLookUp[z]*songPref[y]*sampleD[y]/(z+0.0);
			sampleCountCum[i]=sampleCount[i];
			if (i>0){sampleCountCum[i]+=sampleCountCum[i-1];}
		}
		
		int t=0;
		int c2=c-1;
		for (int i=0; i<newRepSize; i++){
			v=nextDouble()*sampleCountCum[c2];
			for (int j=0; j<c; j++){
				if (v<sampleCountCum[j]){
					newRepertoire[i]=sample[j];
					newAges[i]=agesC[j];
					t=j;
					j=c;
				}
			}
			for (int j=t; j<c; j++){
				sampleCountCum[j]-=sampleCount[t];
			}
			
		}
	}
	
		
	public int sampleSongs(){
		int t, u, x;
		double v;	
		int c=0;
		//int d=0;
		//long t1=System.nanoTime();
		/*
		for (i=0; i<numPossibleSongs; i++){
			sampleC[i]=0;
			sampleD[i]=0;
			agesC[i]=0;
		}
		*/
		
		//long t1a=System.nanoTime();
		/*
		sampleC=new int[numPossibleSongs];
		sampleD=new double[numPossibleSongs];
		agesC=new int[numPossibleSongs];
		*/
		
		Arrays.fill(sampleC, 0);
		//Arrays.fill(sampleD, 0);
		//Arrays.fill(agesC, 0);
		
		
		//long t2=System.nanoTime();
		for (int i=0; i<sampleNumber; i++){

			//t=nextInt(popSize);		
			
			Individual ind=population[nextInt(popSize)];
			
			//u=population[t].repSize;
			//v=population[t].tutorAbility;
			
			u=ind.repSize;
			v=ind.tutorAbility;
			
			for (int j=0; j<u; j++){
				//x=population[t].repertoire[j];
				x=ind.repertoire[j];
				if (sampleC[x]==0){
					sample[c]=x;
					//agesC[c]=population[t].ages[j];
					agesC[c]=ind.ages[j];
					sampleD[x]=0;
					c++;
				}
				//d++;
				sampleC[x]++;
				sampleD[x]+=v;
			}			
		}
		//long t3=System.nanoTime();
		
		//sampleSize=d;
		
		
		//long t4=System.nanoTime();
		//System.out.println((t3-t2)+" "+(t2-t1));
		
		//System.out.println((t1a-t1)+" "+(t2-t1a));
		return c;
	}

	public void weightSongs(int c){
		for (int i=0; i<c; i++){
			int y=sample[i];
			int z=sampleC[y];
			sampleCount[i]=powerLookUp[z]*songPref[y]*sampleD[y]/(z+0.0);
			sampleCountCum[i]=sampleCount[i];
			if (i>0){sampleCountCum[i]+=sampleCountCum[i-1];}
		}
	}
	
	public void pickSongs(int c){
		int i,j;
		double v;
		int t=0;
		int c2=c-1;
		for (i=0; i<newRepSize; i++){
			v=nextDouble()*sampleCountCum[c2];
			for (j=0; j<c; j++){
				if (v<sampleCountCum[j]){
					newRepertoire[i]=sample[j];
					newAges[i]=agesC[j];
					t=j;
					j=c;
				}
			}
			for (j=t; j<c; j++){
				sampleCountCum[j]-=sampleCount[t];
			}
			
		}
	}
	
				
		//This is a combined learning method.
		//Individuals learn from a subset of possible tutors.
		//Individuals select a sample of sampleNumber tutors, and memorize their repertoires.
		//Frequency of different types in the repertoires is memorised.
		//Frequency is taken to the exponent of confbias.
		//Attractiveness is modified according to whether songs are 'attractive' or not/
		//Songs are sampled according to the relative size of the output.
		//I use the algorithm A-Res: https://en.wikipedia.org/wiki/Reservoir_sampling#Weighted_Random_Sampling_using_Reservoir
		//from Efraimidis and Spirakis to try to sample efficiently with weights. I'm not sure how efficient this is, since it requires exponents
				
		public void buildRepertoireCombine5(){
					

			int i,j, t, u, x, y;
			double v, w;
					
			int c=0;
			//int d=0;
			
			for (i=0; i<numPossibleSongs; i++){
				sampleC[i]=0;
				sampleD[i]=0;
				agesC[i]=0;
			}
			
			for (i=0; i<sampleNumber; i++){
				/*
				w=nextDouble();
				t=0;
				for (j=0; j<popSize; j++){
					if (popTutorAbilities[j]>w){
						t=j;
						j=popSize;
					}
				}
				*/
				t=nextInt(popSize);			
				u=population[t].repSize;	
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]==0){
						sample[c]=x;
						agesC[c]=population[t].ages[j];
						c++;
						
					}
					//d++;
					sampleC[x]++;
					sampleD[x]+=population[t].tutorAbility;	
				}			
			}
			sampleTypes=c;
			//sampleSize=d;
					
			if (c<newRepSize){newRepSize=c;}	
					
			for (i=0; i<c; i++){
				y=sample[i];
				sampleCount[i]=powerLookUp[sampleC[y]]*songPref[y]*sampleD[y]/(sampleC[y]+0.0);
				sampleCountCum[i]=sampleCount[i];
				if (i>0){sampleCountCum[i]+=sampleCountCum[i-1];}
			}
					
			boolean f=false;
			t=0;
			int c2=c-1;
			
			/*
			
			for (i=0; i<newRepSize; i++){
				v=nextDouble()*sampleCountCum[c2];
				f=false;
				for (j=0; j<c; j++){
					if (!f){
						if (v<sampleCountCum[j]){
							newRepertoire[i]=sample[j];
							newAges[i]=agesC[j];
							f=true;
							t=j;
							sampleCountCum[j]-=sampleCount[j];
						}
					}
					else{
						sampleCountCum[j]-=sampleCount[t];
					}
				}
				
			}
			*/
			
			for (i=0; i<newRepSize; i++){
				v=nextDouble()*sampleCountCum[c2];
				for (j=0; j<c; j++){
					if (v<sampleCountCum[j]){
						newRepertoire[i]=sample[j];
						newAges[i]=agesC[j];
						t=j;
						j=c;
					}
				}
				for (j=t; j<c; j++){
					sampleCountCum[j]-=sampleCount[t];
				}
				
			}
			
			
		}
		
		
		//This is a combined learning method.
				//Individuals learn from a subset of possible tutors.
				//Individuals select a sample of sampleNumber tutors, and memorize their repertoires.
				//Frequency of different types in the repertoires is memorised.
				//Frequency is taken to the exponent of confbias.
				//Attractiveness is modified according to whether songs are 'attractive' or not/
				//Songs are sampled according to the relative size of the output.
				//I use the algorithm A-Res: https://en.wikipedia.org/wiki/Reservoir_sampling#Weighted_Random_Sampling_using_Reservoir
				//from Efraimidis and Spirakis to try to sample efficiently with weights. I'm not sure how efficient this is, since it requires exponents
						
		public void buildRepertoireTwoStage(){
			//double firststageweight=2;
			int h, i,j, t, u;
			double v;
			boolean f;
			
			//sampleCount=new double[sampleNumber];
			//sample=new int[sampleNumber];
			
			//stage1
			int checker=sampleNumber*10;
			int c2=0;
			
			for (i=0; i<sampleNumber; i++){
				f=true;
				c2=0;
				while (f){
					f=false;
					t=nextInt(popSize);
					u=nextInt(population[t].repSize);
					h=population[t].repertoire[u];
					
					for (j=0; j<i; j++){				
						if (h==sample[j]){
							f=true;
							j=i;
						}
					}
					if (!f){
						sample[i]=h;
						sampleCount[i]=firststageweight;
						//System.out.println(agesC.length+" "+i);
						agesC[i]=population[t].ages[u];
					}
					c2++;
					
					if ((f)&&(c2==checker)){
						//f=false;
						while(f){
							//System.out.println("Here "+c2+" "+checker+" "+sampleNumber+" "+i);
							f=false;
							sample[i]=nextInt(numPossibleSongs);
							for (j=0; j<i; j++){
								if (sample[i]==sample[j]){
									f=true;
								}
							}
						}
						//System.out.println("Done "+c2+" "+checker+" "+sampleNumber+" "+i);
						sampleCount[i]=firststageweight;
						agesC[i]=0;
					}
				}
			}

			//stage 2		
			
			for (i=0; i<sampleNumber2; i++){
				t=nextInt(popSize);
				u=nextInt(population[t].repSize);
				h=population[t].repertoire[u];
					
				for (j=0; j<sampleNumber; j++){				
					if (h==sample[j]){
						sampleCount[j]++;
					}
				}
			}
			
			sampleCountCum[0]=sampleCount[0];
			for (i=1; i<sampleNumber; i++){

				sampleCountCum[i]=sampleCount[i]+sampleCountCum[i-1];
			}
			
			t=0;
			int sn1=sampleNumber-1;
			for (i=0; i<newRepSize; i++){
				v=nextDouble()*sampleCountCum[sn1];
				f=false;
				for (j=0; j<sampleNumber; j++){
					if (!f){
						if (v<sampleCountCum[j]){
							newRepertoire[i]=sample[j];
							newAges[i]=agesC[j];
							f=true;
							t=j;
							sampleCountCum[j]-=sampleCount[j];
						}
					}
					else{
						sampleCountCum[j]-=sampleCount[t];
					}
				}
			}
			
			
			
			for (i=0; i<newRepSize; i++){
				for (j=0; j<i; j++){
					if (newRepertoire[i]==newRepertoire[j]){
						System.out.println("E: "+newRepertoire[i]+" "+newRepertoire[j]+" "+sampleNumber+" "+newRepSize);
					}
				}
			}
			
			
		}
		
		
		public void buildRepertoireTwoStageCombine(){
			int i,j, k, t, u, x, y;
			double v, w;
					
			int c=0;
			//int d=0;
			
			for (i=0; i<numPossibleSongs; i++){
				sampleC[i]=0;
				sampleD[i]=0;
				agesC[i]=0;
			}
			
			for (i=0; i<sampleNumber; i++){
				
				t=nextInt(popSize);			
				u=population[t].repSize;	
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]==0){
						sample[c]=x;
						agesC[c]=population[t].ages[j];
						c++;
						
					}
					//d++;
					sampleC[x]=1;
					sampleD[x]=1;	
				}			
			}
			sampleTypes=c;
			if (c<newRepSize){newRepSize=c;}
			
			
			double sc=0;
			
			for (i=0; i<sampleNumber2; i++){
				
				t=nextInt(popSize);			
				u=population[t].repSize;	
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]>0){
						sc++;
						sampleC[x]++;
						sampleD[x]+=population[t].tutorAbility;
					}
						
				}			
			}
					
			sc*=firststageweight;	
					
			for (i=0; i<c; i++){
				y=sample[i];
				sampleCount[i]=(sampleC[y]+sc)*songPref[y]*sampleD[y]/(sampleC[y]+0.0);
				sampleCountCum[i]=sampleCount[i];
				if (i>0){sampleCountCum[i]+=sampleCountCum[i-1];}
			}
					
			boolean f=false;
			t=0;
			int c2=c-1;
			
			boolean[] pp=new boolean[c];
			
			int c3=c;
			if (c3>8){c3=8;}
			
						
			for (i=c3; i>newRepSize; i--){
				for (j=0; j<c; j++){
					pp[j]=false;
				}
				
				w=0;
				f=true;
				for (k=0; k<i; k++){
					v=nextDouble()*sampleCountCum[c2];
					f=true;
					for (j=0; j<c; j++){
						if ((f)&&(v<sampleCountCum[j])){
							pp[j]=true;
							f=false;
							w=sampleCount[j];
						}
						if (!f){
							sampleCountCum[j]-=w;
						}
					}
				}
				
				if (pp[0]){sampleCountCum[0]=sampleCount[0];}
				else{sampleCountCum[0]=0;}
				for (j=1; j<c; j++){
					sampleCountCum[j]=sampleCountCum[j-1];
					if (pp[j]){
						sampleCountCum[j]+=sampleCount[j];
					}	
				}
			}
			
			int a=0;
			for (i=0; i<c; i++){
				if (pp[i]){
					newRepertoire[a]=sample[i];
					newAges[a]=agesC[i];
					//System.out.println(newRepertoire[a]);
					a++;
					
				}
			}
		}
		
		
		
		public void buildRepertoireTwoStageCombine2X4(){
			

			int i,j, t, u, x, y;
			double v, w;
					
			int c=0;
			//int d=0;
			
			for (i=0; i<numPossibleSongs; i++){
				sampleC[i]=0;
				sampleD[i]=0;
				agesC[i]=0;
			}
			
			for (i=0; i<sampleNumber; i++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]==0){
						sample[c]=x;
						agesC[c]=population[t].ages[j];
						c++;
						
					}
					//d++;
					//sampleC[x]++;
					//sampleD[x]+=population[t].tutorAbility*firststageweight;
					sampleD[x]=firststageweight;
					sampleC[x]=1;
				}			
			}
			sampleTypes=c;
			
			
			
			for (i=0; i<sampleNumber2; i++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					
					if (sampleC[x]>0){
						sampleC[x]++;
						//sampleD[x]+=population[t].tutorAbility;
						sampleD[x]++;
					}
				}			
			}
			
			
			//sampleSize=d;
					
			if (c<newRepSize){newRepSize=c;}	
					
			for (i=0; i<c; i++){
				y=sample[i];
				//sampleCount[i]=1/(songPref[y]*sampleD[y]);
				sampleCount[i]=1/(sampleD[y]);
				sampleCountCum[i]=sampleCount[i];
				if (i>0){sampleCountCum[i]+=sampleCountCum[i-1];}
				deletelist[i]=false;
			}
					
			t=0;
			int c2=c-1;

			int nrs=c-newRepSize;
			
			for (i=0; i<nrs; i++){
				v=nextDouble()*sampleCountCum[c2];
				for (j=0; j<c; j++){
					if (v<sampleCountCum[j]){
						/*
						if(deletelist[j]){
							System.out.println("oops "+sampleCountCum[c2]);
							for (int k=0; k<c; k++){
								System.out.println(k+" "+sampleCountCum[k]+" "+deletelist[k]+" "+j+" "+t);
							}	
						}
						*/
						deletelist[j]=true;
						//newRepertoire[i]=sample[j];
						//newAges[i]=agesC[j];
						t=j;
						j=c;
					}
				}
				if (t>0){
					sampleCountCum[t]=sampleCountCum[t-1];
				}
				else{
					sampleCountCum[t]=0;
				}
				for (j=t+1; j<c; j++){
					if (deletelist[j]){
						sampleCountCum[j]=sampleCountCum[j-1];
					}
					else{
						sampleCountCum[j]=sampleCountCum[j-1]+sampleCount[j];
					}
				}
				
				
				//for (j=t; j<c; j++){
				//	sampleCountCum[j]-=sampleCount[t];
				//}
				
				
			}
			j=0;
			for (i=0; i<c; i++){
				if (!deletelist[i]){
					newRepertoire[j]=sample[i];
					newAges[j]=agesC[i];
					j++;
				}
			}
			//if (j!=newRepSize){System.out.println("ALERT!!!"+j+" "+newRepSize+" "+nrs);}
		}

		public void buildRepertoireTwoStageCombine2X(){
			
			int i,j, t, u, x, y;
			double v, w;
					
			int c=0;
			//int d=0;
			
			for (i=0; i<numPossibleSongs; i++){
				sampleC[i]=0;
				sampleD[i]=0;
				agesC[i]=0;
			}
			
			for (i=0; i<sampleNumber; i++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]==0){
						sample[c]=x;
						agesC[c]=population[t].ages[j];
						c++;
						
					}
					//d++;
					//sampleC[x]++;
					sampleD[x]+=population[t].tutorAbility;
					//sampleD[x]=firststageweight;
					sampleC[x]=1;
				}			
			}
			sampleTypes=c;
			
			
			
			for (i=0; i<sampleNumber2; i++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					
					if (sampleC[x]>0){
						sampleC[x]++;
						sampleD[x]+=population[t].tutorAbility;
						//sampleD[x]++;
					}
				}			
			}
			
			
			//sampleSize=d;
					
			if (c<newRepSize){newRepSize=c;}	
					
			for (i=0; i<c; i++){
				y=sample[i];
				
				sampleCount[i]=sampleC[y]/(powerLookUp[sampleC[y]]*songPref[y]*sampleD[y]);
				
				//sampleCount[i]=1/(songPref[y]*sampleD[y]);
				
				sampleCountCum[i]=sampleCount[i];
				if (i>0){sampleCountCum[i]+=sampleCountCum[i-1];}
				deletelist[i]=false;
			}
					
			t=0;
			int c2=c-1;

			int nrs=c-newRepSize;
			
			for (i=0; i<nrs; i++){
				v=nextDouble()*sampleCountCum[c2];
				for (j=0; j<c; j++){
					if (v<sampleCountCum[j]){
						/*
						if(deletelist[j]){
							System.out.println("oops "+sampleCountCum[c2]);
							for (int k=0; k<c; k++){
								System.out.println(k+" "+sampleCountCum[k]+" "+deletelist[k]+" "+j+" "+t);
							}	
						}
						*/
						deletelist[j]=true;
						//newRepertoire[i]=sample[j];
						//newAges[i]=agesC[j];
						t=j;
						j=c;
					}
				}
				if (t>0){
					sampleCountCum[t]=sampleCountCum[t-1];
				}
				else{
					sampleCountCum[t]=0;
				}
				for (j=t+1; j<c; j++){
					if (deletelist[j]){
						sampleCountCum[j]=sampleCountCum[j-1];
					}
					else{
						sampleCountCum[j]=sampleCountCum[j-1]+sampleCount[j];
					}
				}
				
				
				//for (j=t; j<c; j++){
				//	sampleCountCum[j]-=sampleCount[t];
				//}
				
				
			}
			j=0;
			for (i=0; i<c; i++){
				if (!deletelist[i]){
					newRepertoire[j]=sample[i];
					newAges[j]=agesC[i];
					j++;
				}
			}
			//if (j!=newRepSize){System.out.println("ALERT!!!"+j+" "+newRepSize+" "+nrs);}
		}
		
		public void buildRepertoireTwoStageCombine2X3(){
			
			int i,j, t, u, x, y;
			double v, w;
					
			int c=0;
			//int d=0;
			
			for (i=0; i<numPossibleSongs; i++){
				sampleC[i]=0;
				//sampleD[i]=0;
				agesC[i]=0;
				deletelist[i]=false;
			}
			
			for (i=0; i<sampleNumber; i++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]==0){
						sample[c]=x;
						agesC[c]=population[t].ages[j];
						c++;
						sampleC[x]=1;
						
					}
					//d++;
					
					//sampleD[x]+=population[t].tutorAbility;
					//sampleD[x]=firststageweight;
				}			
			}
			sampleTypes=c;
			
			int nsa=0;
			for (int i2=0; i2<sampleNumber2; i2++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				nsa+=u;
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]>0){
						sampleC[x]++;
					}
				}			
			}
			if (nsa==0){nsa=1;}
					
			for (int i2=0; i2<c; i2++){

				if (i2==0){
					sampleCountCum[i2]=0;
				}
				else{
					sampleCountCum[i2]=sampleCountCum[i2-1];
				}
				if (!deletelist[i2]){
					y=sample[i2];
					v=sampleC[y]-1+firststageweight*nsa;
					sampleCount[i2]=1/(songPref[y]*v);
					sampleCountCum[i2]+=sampleCount[i2];
				}
			}	

			
			if (c<newRepSize){newRepSize=c;}	
					
			t=0;
			int c2=c-1;

			int nrs=c-newRepSize;
			double aa=0;
			for (i=0; i<nrs; i++){	
				
				v=nextDouble()*sampleCountCum[c2];
				
				
				boolean flag=false;
				aa=0;
				for (j=0; j<c; j++){
					if (!flag){
						if (v<sampleCountCum[j]){
							deletelist[j]=true;
							flag=true;
						}
					}
					if (flag){
						sampleCountCum[j]=aa;
						if (!deletelist[j]){
							sampleCountCum[j]+=sampleCount[j];
						}	
					}
					aa=sampleCountCum[j];
					
				}
			}
			j=0;
			for (i=0; i<c; i++){
				if (!deletelist[i]){
					newRepertoire[j]=sample[i];
					newAges[j]=agesC[i];
					j++;
				}
			}
			//if (j!=newRepSize){System.out.println("ALERT!!!"+j+" "+newRepSize+" "+nrs);}
		}
		
		
		public void buildRepertoireTwoStageCombine2(){
			
			int i,j, t, u, x, y;
			double v, w;
					
			int c=0;
			//int d=0;
			
			for (i=0; i<numPossibleSongs; i++){
				sampleC[i]=0;
				//sampleD[i]=0;
				agesC[i]=0;
				deletelist[i]=false;
			}
			
			for (i=0; i<sampleNumber; i++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]==0){
						sample[c]=x;
						agesC[c]=population[t].ages[j];
						c++;
						sampleC[x]=1;
						
					}
					//d++;
					
					//sampleD[x]+=population[t].tutorAbility;
					//sampleD[x]=firststageweight;
				}			
			}
			sampleTypes=c;
			
			int nsa=0;
			for (int i2=0; i2<sampleNumber2; i2++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				nsa+=u;
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]>0){
						sampleC[x]++;
					}
				}			
			}
			if (nsa==0){nsa=1;}
					
			for (int i2=0; i2<c; i2++){

				if (i2==0){
					sampleCountCum[i2]=0;
				}
				else{
					sampleCountCum[i2]=sampleCountCum[i2-1];
				}
				if (!deletelist[i2]){
					y=sample[i2];
					//v=sampleC[y]-1+firststageweight*nsa;
					sampleCount[i2]=powerLookUp[sampleC[y]];
					sampleCountCum[i2]+=sampleCount[i2];
				}
			}	

			
			if (c<newRepSize){newRepSize=c;}	
					
			t=0;
			int c2=c-1;

			int nrs=c-newRepSize;
			double aa=0;
			for (i=0; i<nrs; i++){	
				
				v=nextDouble()*sampleCountCum[c2];
				
				
				boolean flag=false;
				aa=0;
				for (j=0; j<c; j++){
					if (!flag){
						if (v<sampleCountCum[j]){
							deletelist[j]=true;
							flag=true;
						}
					}
					if (flag){
						sampleCountCum[j]=aa;
						if (!deletelist[j]){
							sampleCountCum[j]+=sampleCount[j];
						}	
					}
					aa=sampleCountCum[j];
					
				}
			}
			j=0;
			for (i=0; i<c; i++){
				if (!deletelist[i]){
					newRepertoire[j]=sample[i];
					newAges[j]=agesC[i];
					j++;
				}
			}
			//if (j!=newRepSize){System.out.println("ALERT!!!"+j+" "+newRepSize+" "+nrs);}
		}
		
		public void buildRepertoireTwoStageCombine2X2(){
			
			int i,j, t, u, x, y;
			double v, w;
					
			int c=0;
			//int d=0;
			
			for (i=0; i<numPossibleSongs; i++){
				sampleC[i]=0;
				sampleD[i]=0;
				agesC[i]=0;
				deletelist[i]=false;
			}
			
			for (i=0; i<sampleNumber; i++){

				t=nextInt(popSize);			
				u=population[t].repSize;	
				for (j=0; j<u; j++){
					x=population[t].repertoire[j];
					if (sampleC[x]==0){
						sample[c]=x;
						agesC[c]=population[t].ages[j];
						c++;
						
					}
					//d++;
					sampleC[x]++;
					//sampleD[x]+=population[t].tutorAbility;
					//sampleD[x]=firststageweight;
				}			
			}
			sampleTypes=c;
			
			
			if (c<newRepSize){newRepSize=c;}	
			
			
					
			t=0;
			int c2=c-1;

			int nrs=c-newRepSize;
			
			for (i=0; i<nrs; i++){
				
				for (int i2=0; i2<sampleNumber2; i2++){

					t=nextInt(popSize);			
					u=population[t].repSize;	
					
					for (j=0; j<u; j++){
						x=population[t].repertoire[j];
						
						if (sampleC[x]>0){
							sampleD[x]=1;
						}
					}			
				}
				
						
				for (int i2=0; i2<c; i2++){

					if (i2==0){
						sampleCountCum[i2]=0;
					}
					else{
						sampleCountCum[i2]=sampleCountCum[i2-1];
					}
					if (!deletelist[i2]){
						y=sample[i2];
						sampleD[y]+=firststageweight;
						sampleCount[i2]=1/(songPref[y]*sampleD[y]);
						sampleD[y]=0;
						sampleCountCum[i2]+=sampleCount[i2];
					}
				}	
				
				v=nextDouble()*sampleCountCum[c2];
				
				
				boolean flag=false;
				
				for (j=0; j<c; j++){
					if (!flag){
						if (v<sampleCountCum[j]){
							deletelist[j]=true;
							flag=true;
						}
					}
					if (flag){
						if (j>0){
							sampleCountCum[j]=sampleCountCum[j-1];
						}
						else{
							sampleCountCum[j]=0;
						}
						
						
						if (!deletelist[j]){
							sampleCountCum[j]+=sampleCount[j];
						}
					}
					
				}
			}
			j=0;
			for (i=0; i<c; i++){
				if (!deletelist[i]){
					newRepertoire[j]=sample[i];
					newAges[j]=agesC[i];
					j++;
				}
			}
			//if (j!=newRepSize){System.out.println("ALERT!!!"+j+" "+newRepSize+" "+nrs);}
		}
		
		
		
		public void buildRepertoireTwoStageCombineX(){
			//double firststageweight=2;
			int h, i,j, t, u;
			double v;
			boolean f;
			
			//sampleCount=new double[sampleNumber];
			//sample=new int[sampleNumber];
			
			//stage1
			int checker=sampleNumber*10;
			int c2=0;
			
			for (i=0; i<sampleNumber; i++){
				f=true;
				c2=0;
				while (f){
					f=false;
					t=nextInt(popSize);
					u=nextInt(population[t].repSize);
					h=population[t].repertoire[u];
					
					for (j=0; j<i; j++){				
						if (h==sample[j]){
							f=true;
							j=i;
						}
					}
					if (!f){
						sample[i]=h;
						//sampleCount[i]=firststageweight;
						//System.out.println(agesC.length+" "+i);
						agesC[i]=population[t].ages[u];
						sampleCount[i]=population[t].tutorAbility*firststageweight;	
					}
					c2++;
					
					if ((f)&&(c2==checker)){
						//f=false;
						while(f){
							//System.out.println("Here "+c2+" "+checker+" "+sampleNumber+" "+i);
							f=false;
							sample[i]=nextInt(numPossibleSongs);
							for (j=0; j<i; j++){
								if (sample[i]==sample[j]){
									f=true;
								}
							}
						}
						//System.out.println("Done "+c2+" "+checker+" "+sampleNumber+" "+i);
						sampleCount[i]=firststageweight;
						agesC[i]=0;
					}
				}
			}
		
			
			for (i=0; i<sampleNumber2; i++){
				t=nextInt(popSize);
				u=nextInt(population[t].repSize);
				h=population[t].repertoire[u];
					
				for (j=0; j<sampleNumber; j++){				
					if (h==sample[j]){
						sampleCount[j]+=population[t].tutorAbility;
					}
				}
			}

			for (i=0; i<sampleNumber; i++){
				sampleCount[i]=sampleCount[i]*songPref[sample[i]];
				sampleCountCum[i]=sampleCount[i];
				if (i>0){sampleCountCum[i]+=sampleCountCum[i-1];}
			}
			
			
			t=0;
			int sn1=sampleNumber-1;
			for (i=0; i<newRepSize; i++){
				v=nextDouble()*sampleCountCum[sn1];
				f=false;
				for (j=0; j<sampleNumber; j++){
					if (!f){
						if (v<sampleCountCum[j]){
							newRepertoire[i]=sample[j];
							newAges[i]=agesC[j];
							f=true;
							t=j;
							sampleCountCum[j]-=sampleCount[j];
						}
					}
					else{
						sampleCountCum[j]-=sampleCount[t];
					}
				}
			}
			
			for (i=0; i<newRepSize; i++){
				for (j=0; j<i; j++){
					if (newRepertoire[i]==newRepertoire[j]){
						System.out.println("E: "+newRepertoire[i]+" "+newRepertoire[j]+" "+sampleNumber+" "+newRepSize);
					}
				}
			}
			
			
		}
		
		
		public void mutate(int age){
			for (int i=0; i<newRepSize; i++){	
				isMutation[i]=false;
				if (mutationRate>nextDouble()){
					int z=0;
					boolean found=true;
					while (found){
						z=nextInt(numPossibleSongs);
						found=false;
						for (int j=0; j<newRepSize; j++){
							if (newRepertoire[j]==z){
								found=true;
							}
						}
					}
					newRepertoire[i]=z;
					isMutation[i]=true;	
					newAges[i]=age;
				}						
			}
		}
		
		public long nextLong() {
	        final long s0 = state0;
	        long s1 = state1;
	        final long result = s0 + s1;

	        s1 ^= s0;
	        state0 = Long.rotateLeft(s0, 55) ^ s1 ^ (s1 << 14); // a, b
	        state1 = Long.rotateLeft(s1, 36); // c

	        return result;
	    }

	    /**
	     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
	     * @return any int, all 32 bits are random
	     */
	    public int nextInt() {
	        return (int)nextLong();
	    }

	    /**
	     * Exclusive on the upper bound.  The lower bound is 0.
	     * @param bound the upper bound; should be positive
	     * @return a random int less than n and at least equal to 0
	     */
	    public int nextInt( final int bound ) {
	        if ( bound <= 0 ) return 0;
	        int threshold = (0x7fffffff - bound + 1) % bound;
	        for (;;) {
	            int bits = (int)(nextLong() & 0x7fffffff);
	            if (bits >= threshold)
	                return bits % bound;
	        }
	    }

	    public double nextDouble() {
	        return (nextLong() & DOUBLE_MASK) * NORM_53;
	    }


	    /**
	     * Sets the seed of this generator using one long, running that through LightRNG's algorithm twice to get the state.
	     * @param seed the number to use as the seed
	     */
	    public void setSeed(final long seed) {

	        long state = seed + 0x9E3779B97F4A7C15L,
	                z = state;
	        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
	        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
	        state0 = z ^ (z >>> 31);
	        state += 0x9E3779B97F4A7C15L;
	        z = state;
	        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
	        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
	        state1 = z ^ (z >>> 31);
	    }		
	
		public double nextGaussian(){
			double x1, x2, w, y1;
			 
	         do {
	        	 x1 = 2.0 * nextDouble() - 1.0;
	        	 x2 = 2.0 * nextDouble() - 1.0;
	        	 w = x1 * x1 + x2 * x2;
	         } while ( w >= 1.0 );

	         //w = StrictMath.sqrt( (-2.0 * StrictMath.log( w ) ) / w );
	         w = Math.sqrt( (-2.0 * Math.log( w ) ) / w );
	         y1 = x1 * w;
	         return y1;
		}
		

/**
 * A port of Blackman and Vigna's xoroshiro 128+ generator; should be very fast and produce high-quality output.
 * Testing shows it is within 5% the speed of LightRNG, sometimes faster and sometimes slower, and has a larger period.
 * It's called XoRo because it involves Xor as well as Rotate operations on the 128-bit pseudo-random state.
 * <br>
 * Machines without access to efficient bitwise rotation (such as all desktop JREs and some JDKs run specifying the
 * {@code -client} flag or that default to the client VM, which includes practically all 32-bit Windows JREs but almost
 * no 64-bit JREs or JDKs) may benefit from using XorRNG over XoRoRNG. LightRNG should continue to be very fast, but has
 * a significantly shorter period (the amount of random numbers it will go through before repeating), at
 * {@code pow(2, 64)} as opposed to XorRNG and XoRoRNG's {@code pow(2, 128)}, but LightRNG also allows the current RNG
 * state to be retrieved and altered with {@code getState()} and {@code setState()}. For most cases, you should decide
 * between LightRNG and XoRoRNG based on your needs for period length and state manipulation (LightRNG is also used
 * internally by almost all StatefulRNG objects).
 * <br>
 * Original version at http://xoroshiro.di.unimi.it/xoroshiro128plus.c
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 *
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger
 */

}