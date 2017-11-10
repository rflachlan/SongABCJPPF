

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedList;

import org.jppf.node.protocol.AbstractTask;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;


public class SimulationRunner extends AbstractTask<LinkedList<Parameters>>{
	Individual[] population;
	
	int[][][] empfreqs;
	int[][] repSizes;
	XoRoShiRo128PlusRandom random;
	Parameters param;
	Parameters[] params;
	double[] weights;
	double[][] cov;
	int npops;
	double score;
	double epsilon;
	int type;
	int target=100;
	LinkedList<Parameters> paramList=new LinkedList<Parameters>();
	LinkedList<Parameters> failList=new LinkedList<Parameters>();
	
	int id=0;
	
	public SimulationRunner(int[][][] ef, long seed, double epsilon, int target, int type, int id){
		random=new XoRoShiRo128PlusRandom(seed);
		//this.random=random.split();
		this.random.setSeed(random.nextLong());
		this.epsilon=epsilon;
		this.target=target;
		this.type=type;
		this.id=id;
			
		//System.out.println("here"+type);
		
		processEmpiricalData(ef);
		
	}
	
	public SimulationRunner(int[][][] ef, Parameters[] params, double[] weights2, double[][] cov2, long seed, double epsilon, int target, int type, int id){
		random=new XoRoShiRo128PlusRandom(seed);
		//this.random=random.split();
		this.random.setSeed(random.nextLong());
		this.params=params;
		weights=new double[weights2.length];
		weights[0]=weights2[0];
		for (int i=1; i<weights.length; i++){
			weights[i]=weights[i-1]+weights2[i];
		}
		
		cov=new double[cov2.length][];
		for (int i=0; i<cov2.length; i++){
			cov[i]=new double[cov2[i].length];
			for (int j=0; j<cov[i].length; j++){
				cov[i][j]=cov2[i][j];
			}
		}
		
		this.cov=cov;
		this.epsilon=epsilon;
		this.target=target;
		this.type=type;
		this.id=id;
			
		//System.out.println("here"+type);
		
		processEmpiricalData(ef);
		
	}
	
	
	public void processEmpiricalData(int[][][] ef){
		npops=ef.length;
		empfreqs=new int[npops][][];
		
		repSizes=new int[npops][10];
		
		for (int i=0; i<npops; i++){
			
			empfreqs[i]=new int[ef[i].length][];
			for (int j=0; j<ef[i].length; j++){
				repSizes[i][ef[i][j].length]++;
				empfreqs[i][j]=new int[ef[i][j].length];
				System.arraycopy(ef[i][j], 0, empfreqs[i][j], 0, ef[i][j].length);
			}
		}		
	}
		
	public void run(){
		DecimalFormat mdf=new DecimalFormat("0.000");
		int count=0;
		int count2=0;
		System.out.println(target);
		
		while(count<target){
			long a=System.currentTimeMillis();
			if (type==0){
				param=new Parameters(random.nextLong());
			}
			else if (type==1){
					//this is a bit broken now. Code for CV
				param=new Parameters(params[count], random.nextLong());
			}
			
			else if (type==2){
				param=new Parameters(random.nextLong());
				param.drawFromPrior();
				//param.reportParameters();
			}
			else if (type==3){	
				double x=random.nextDouble()*weights[weights.length-1];
				int loc=0;
				for (int i=0; i<weights.length; i++){
					if (weights[i]>x){
						loc=i;
						i=weights.length;
					}
				}	
				//System.out.println((id+1)+" "+loc+weights.length);
				param=new Parameters(params[loc], random.nextLong());	
				//System.out.print("A: ");
				//param.reportParameters();
				param.drawFromProposal(cov);
				//System.out.print("B: ");
				//param.reportParameters();
			}
				
			for (int i=0; i<npops; i++){
				if (type==0){
					param.drawFromPrior();
				}
				param.setStatistics(runSimulation(i), i);
				param.calculateAgeProfile(population, i);	
			}
			param.analyseStats();
			
			/*
			double cx=0;
			double[] xv=param.getPVec(param.modelType);
			for (Parameters pc : paramList){
				double[] yv=pc.getPVec(param.modelType);
				for (int j=0; j<xv.length; j++){
					cx+=(xv[j]-yv[j])*(xv[j]-yv[j]);
				}
				if (cx<0.00000001){
					System.out.print("Match found: ");
					param.reportParameters();
				}
			}
			*/
			param.random=null;
			param.ced=null;
			param.cov=null;
			long b=System.currentTimeMillis();
			
			if (param.abcscore<epsilon){
				paramList.add(param);
				System.out.print((id+1)+" "+mdf.format(param.abcscore)+" "+mdf.format((b-a)*0.001)+" ");
				//for (int i=0; i<param.score.length; i++){
					//System.out.print(param.score[i]+" ");
				//}
				count++;
				System.out.print(paramList.size()+" "+(count+count2)+" ");
				param.reportParameters();
				
			}
			else{
				count2++;
				//System.out.print("F: "+(id+1)+" "+mdf.format(param.abcscore)+" "+mdf.format((b-a)*0.001)+" ");
				//param.reportParameters();
				failList.add(param);
			}
		}
		try{
			writeResults(id);
		}
		catch(Exception e){}
		
		setResult(paramList);
		
		System.out.println("completed");
	}
	
	public MeasureStatistics runSimulation(int pop){
		long a=System.currentTimeMillis();
		int popsize=(int)Math.round(param.npop[pop]);

		
		param.repSizes=repSizes[pop];
		initiateSimulation(popsize);
		long b=System.currentTimeMillis();
		int nYears=param.nYears;

		for (int i=0; i<nYears; i++){
			iterateSimulation(i);
		}
		long c=System.currentTimeMillis();

		PopulationProcessor pp=new PopulationProcessor(population);

		MeasureStatistics ced=new MeasureStatistics(random, empfreqs[pop], pp.songFreqs, type, pop);
		long d=System.currentTimeMillis();

		return ced;
	}
	
	public void initiateSimulation(int popsize){
		
		population=new Individual[popsize];
		
		int a=0;
		int[] x=param.repSizes;
		
		double[] songatt=getAttractiveSongs();
		
		for (int i=0; i<x.length; i++){
			//System.out.println(i+" "+x[i]);
			for (int j=0; j<x[i]; j++){
				
				population[a]=new Individual(i, random.nextLong(), population, param, i, songatt);
				a++;
			}
		}

		for (int i=a; i<popsize; i++){
			population[i]=new Individual(i, random.nextLong(), population, param, -1, songatt);
		}
		
	}
	
	public void iterateSimulation(int age){
		for (int i=0; i<population.length; i++){
			population[i].mortality();
		}
		//double[] x=setIndividualVariation();
		for (int i=0; i<population.length; i++){
			//population[i].setTutorAbilities(x);
			population[i].learnSongs(age);
		}
		for (int i=0; i<population.length; i++){
			population[i].updateRepertoire();
		}

	}
	
	
	public double[] getAttractiveSongs(){
		
		int ns=(int)Math.round(param.numPossibleSongs);
		int a=(int)Math.round(param.propAttractiveSongs*param.numPossibleSongs);
		
		double[] songatt=new double[ns];
		
		for (int i=0; i<songatt.length; i++){songatt[i]=param.unattractiveSongPenalty;}
		
		for (int i=0; i<a; i++){
			boolean found=true;
			while(found){
				int x=random.nextInt(ns);
				if (songatt[x]==param.unattractiveSongPenalty){
					songatt[x]=1;
					found=false;
				}
			}	
		}
		return songatt;		
	}
	
	public void writeResults(int id){
		String fileLoc="/home/rflachlan/Desktop";
		String altLoc="/Users/Rob/Desktop";
		
		File filecheck=new File(fileLoc);
		if (!filecheck.exists()){
			fileLoc=altLoc;
		}
		
		String fileLocation=fileLoc+System.getProperty("file.separator")+"abcout"+id+".csv";
		//String fileLoc=saveLoc+" "+n+".csv";
		
		DocumentSave ds=new DocumentSave(fileLocation, ",");
		
		for (Parameters res : paramList){
			double[] p=res.getPVec();
			for (int j=0; j<p.length; j++){
				ds.writeDouble(p[j]);
			}
			for (int j=0; j<res.stats.length; j++){
				ds.writeDouble(res.stats[j]);
			}
			for (int j=0; j<res.score.length; j++){
				
				ds.writeDouble(res.score[j]);
			}
			ds.writeDouble(res.abcscore);
			
			ds.writeLine();
		}
		for (Parameters res : failList){
			double[] p=res.getPVec();
			for (int j=0; j<p.length; j++){
				ds.writeDouble(p[j]);
			}
			for (int j=0; j<res.stats.length; j++){
				ds.writeDouble(res.stats[j]);
			}
			for (int j=0; j<res.score.length; j++){
				ds.writeDouble(res.score[j]);
			}
			ds.writeDouble(res.abcscore);
			
			ds.writeLine();
		}
		
		ds.finishWriting();
		
		fileLocation=fileLoc+System.getProperty("file.separator")+"songageout"+id+".csv";
		//String fileLoc=saveLoc+" "+n+".csv";
		
		ds=new DocumentSave(fileLocation, ",");
		
		
		for (Parameters res : paramList){
			for (int j=0; j<res.ageProfile.length; j++){
				for (int k=0; k<res.ageProfile[j].length; k++){
					if(res.freqProfile[j][k]>0){
						ds.writeDouble(res.ageProfile[j][k]);
					}
				}
				ds.writeLine();
			}
		}
		for (Parameters res : failList){
			for (int j=0; j<res.ageProfile.length; j++){
				for (int k=0; k<res.ageProfile[j].length; k++){
					if(res.freqProfile[j][k]>0){
						ds.writeDouble(res.ageProfile[j][k]);
					}
				}
				ds.writeLine();
			}
		}
		
		ds.finishWriting();
		
		fileLocation=fileLoc+System.getProperty("file.separator")+"songfreqout"+id+".csv";
		//String fileLoc=saveLoc+" "+n+".csv";
		
		ds=new DocumentSave(fileLocation, ",");
		
		for (Parameters res : paramList){
			for (int j=0; j<res.ageProfile.length; j++){
				for (int k=0; k<res.ageProfile[j].length; k++){
					if(res.freqProfile[j][k]>0){
						ds.writeDouble(res.freqProfile[j][k]);
					}
				}
				ds.writeLine();
			}
		}
		for (Parameters res : failList){
			for (int j=0; j<res.ageProfile.length; j++){
				for (int k=0; k<res.ageProfile[j].length; k++){
					if(res.freqProfile[j][k]>0){
						ds.writeDouble(res.freqProfile[j][k]);
					}
				}
				ds.writeLine();
			}
		}
		
		ds.finishWriting();
		
	}
	
	
}


	