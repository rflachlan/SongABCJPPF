
import java.io.Serializable;
import java.text.DecimalFormat;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;


public class Parameters implements Serializable{
	
	
	double[][] stats2;
	int ncomps=6;
	
	double[][] paramvals;
	
	double[] stats;
	double[] score;
	//double[] p;
	double abcscore=0;
	double[][] ageProfile;
	int[][] freqProfile;

	int modelType=1;
	
	int nYears=5000;
	
	int[] repSizes;
	
	double mortalityRate=0.4;
	
	double[] nPrior={400, 3000};  //first parameter is the minimum population size; second is max-min. This is a hyperprior	
	//double[] nPrior={1000, 1000}; 
	double[] npop={1000, 1000, 1000, 1000, 1000, 1000};
	
	double[] tutorVarPrior={0.01, 6};
	//double[] tutorVarPrior={0.8, 0.8};
	double tutorVar=0.01;
	//double tutorVar=5;
	
	double[] propAttractiveSongsPrior={0.01, 1};
	//double[] propAttractiveSongsPrior={0.63, 0.63};
	double propAttractiveSongs=1;
	//double propAttractiveSongs=0.1;
	
	double unattractiveSongPenalty=0.05;
	
	double[] confBiasPrior={0.25, 4};
	//double[] confBiasPrior={0.75, 2};
	double confBias=1;	//conformity bias
	
	double[] sampleNumberPrior={2.5001, 50.499};	//min max on uniform dist
	//double[] sampleNumberPrior={4.13, 4.13};
	double sampleNumber=5;
	
	double[] sampleNumber2Prior={1.5001, 50.499};	//min max on uniform dist
	//double[] sampleNumber2Prior={6.26,6.26};
	double sampleNumber2=20;	//the number of territories sampled from the population to learn from (if learningMethod>1)
	
	double[] firstStageWeightPrior={0.01, 20};
	//double[] firstStageWeightPrior={2.31, 2.31};
	double firstStageWeight=5;
	
	
	double[] mutationRatePrior={0.0001, 0.3};   //mean, variance on log scale
	//double[] mutationRatePrior={0.0137, 0.0137};
	double mutationRate=0.01;
	
	double[] nPSP={180, 500};
	double numPossibleSongs=283.6117699;	//the number of possible song-types that could be present in a population
	
	XoRoShiRo128PlusRandom random;
	MeasureStatistics[] ced;
	double[][] cov;
	
	
	public Parameters(long seed){
		//System.out.println("INIT");
		random=new XoRoShiRo128PlusRandom(seed);
		//random.setSeed(r.nextLong());
		this.random=random;
		setUp();
	}
	
	public Parameters(double[] p){
		npop=new double[npop.length];
		for (int i=0; i<npop.length; i++){
			npop[i]=Math.exp(p[i]);
		}
		
		int a=npop.length;
		
		this.tutorVar=Math.exp(p[a]);
		a++;
		this.propAttractiveSongs=p[a];
		a++;
		
		if (modelType>2){
			this.sampleNumber=Math.exp(p[a]);
			a++;
			this.sampleNumber2=Math.exp(p[a]);
			a++;
			this.firstStageWeight=Math.exp(p[a]);
			a++;
			this.mutationRate=Math.exp(p[a]);
			a++;
			this.numPossibleSongs=Math.exp(p[a]);
		}
		else{
			this.confBias=Math.exp(p[a]);
			a++;
			this.sampleNumber=Math.exp(p[a]);
			a++;
			this.mutationRate=Math.exp(p[a]);
			a++;
			this.numPossibleSongs=Math.exp(p[a]);
		}
		setUp();
	}
	
	public Parameters(double[] p, long seed){
		
		
		npop=new double[npop.length];
		for (int i=0; i<npop.length; i++){
			npop[i]=Math.exp(p[i]);
		}
		
		int a=npop.length;
		
		this.tutorVar=Math.exp(p[a]);
		a++;
		this.propAttractiveSongs=p[a];
		a++;
		this.confBias=Math.exp(p[a]);
		a++;
		this.sampleNumber=Math.exp(p[a]);
		a++;
		this.mutationRate=Math.exp(p[a]);
		a++;
		this.numPossibleSongs=Math.exp(p[a]);
		setUp();
		
		random=new XoRoShiRo128PlusRandom(seed);
		
		setUp();
	}
	
	public Parameters(Parameters p, long seed){
		
		npop=new double[p.npop.length];
		System.arraycopy(p.npop, 0, npop, 0, npop.length);
		
		this.tutorVar=p.tutorVar;
		this.propAttractiveSongs=p.propAttractiveSongs;
		this.confBias=p.confBias;
		this.sampleNumber=p.sampleNumber;
		this.sampleNumber2=p.sampleNumber2;
		this.firstStageWeight=p.firstStageWeight;
		this.mutationRate=p.mutationRate;
		this.numPossibleSongs=p.numPossibleSongs;
		//random=p.random.split();
		random=new XoRoShiRo128PlusRandom(seed);
		
		setUp();
	}
	
	
	
	public void drawFromPrior(){
		for (int i=0; i<npop.length; i++){
			npop[i]=drawLogUni(nPrior);
		}	
		tutorVar=drawLogUni(tutorVarPrior);
		propAttractiveSongs=drawUni(propAttractiveSongsPrior);
		confBias=drawLogUni(confBiasPrior);
		sampleNumber=drawLogUni(sampleNumberPrior);

		sampleNumber2=drawLogUni(sampleNumber2Prior);
		firstStageWeight=drawLogUni(firstStageWeightPrior);
		mutationRate=drawLogUni(mutationRatePrior);
		numPossibleSongs=drawLogUni(nPSP);
		//System.out.print(tutorVar+" "+propAttractiveSongs+" "+confBias+" "+sampleNumber+" "+mutationRate+" "+numPossibleSongs+" ");
	}
	
	public void setUp(){
		ageProfile=new double[npop.length][];
		freqProfile=new int[npop.length][];
		ced=new MeasureStatistics[npop.length];
		//paramvals=new double[npop.length][];
	}
	
	public void calculateAgeProfile(Individual[] population, int i){
		AgeFrequencyProfile afp=new AgeFrequencyProfile(population, i, this);
		ageProfile[i]=afp.age;
		freqProfile[i]=afp.freq;
	}
	
	public void setStatistics(MeasureStatistics ms, int i){
		ced[i]=ms;
		double[] x=getPVec(modelType);
		//paramvals[i]=new double[x.length];
		//System.arraycopy(x, 0, paramvals[i], 0, x.length);
	}
	
	public void analyseStats(){
		
		int npops=npop.length;
		
		score=new double[ncomps];		
		
		
		for (int i=0; i<npops; i++){
			double[] t=ced[i].scores;
			for (int j=0; j<ncomps; j++){
				score[j]+=t[j];
			}
		}
		
		stats=new double[npops*ced[0].scores.length];
		int sc=0;
		for (int i=0; i<npops; i++){
			double[] t=ced[i].scores;
			for (int j=0; j<t.length; j++){
				stats[sc]=t[j];
				sc++;
			}
		}
		/*
		stats2=new double[npops][];
		for (int i=0; i<npops; i++){
			stats2[i]=new double[ced[i].rstats.length];
			System.arraycopy(ced[i].rstats, 0, stats2[i], 0, stats2[i].length);
		}
		*/
		abcscore=0;
		for (int i=0; i<ncomps; i++){
			abcscore+=score[i]*score[i];
		}
		abcscore=Math.sqrt(abcscore);
		if (Double.isNaN(abcscore)){abcscore=Double.MAX_VALUE;}
		
		//p=getPVec(modelType);
	}
	
	public void drawFromProposal(double[][] cov){		
		double[] v=getPVec(modelType);	
		MultivariateNormalDistribution mnd=new MultivariateNormalDistribution(v, cov);
		mnd.reseedRandomGenerator(random.nextLong());
		boolean allok=false;
		while(!allok){
			
			double[] v2= mnd.sample();
			double x=0;
			double w;
			for (int i=0; i<v2.length; i++){
				w=v2[i]-v[i];
				x+=w*w;
			}
			
			allok=setPVec(v2, modelType);
			//System.out.print(allok+" "+x+" ");
			//reportParameters();
		}
	}
	
	public double drawSampleSizeX(double[] prior){
		double q=Math.log(confBias);
		double y=q*q;
		if (y>0.1215){y=0.1215;}
		double z=3.84627-14.26321*Math.sqrt(y)+20.46066*y;
		
		double ma=Math.log(prior[1]);
		double mi=Math.log(prior[0]);
		double r=ma-mi;
		double p=random.nextDouble()*r;
		p+=mi;
		return Math.exp(p);
		
	}
	
	public double drawLogUni(double[] prior){
		double ma=Math.log(prior[1]);
		double mi=Math.log(prior[0]);
		double r=ma-mi;
		double p=random.nextDouble()*r;
		p+=mi;
		return Math.exp(p);
	}
	
	public double drawUni(double[] prior){
		double max=prior[1];
		double min=prior[0];
		double r=max-min;
		double p=random.nextDouble()*r;
		p+=min;
		return p;
	}
	
	
	public double[] getPVec(){
		return getPVec(modelType);
	}
	
	public double[] getPVec(int modelType){
		
		if (modelType==0){
			return getPVecSimple();
		}
		else if (modelType==1){
			return getPVecConformistBias();
		}
		else  if (modelType==2){
			return getPVecTwoStageLearning();
		}
		else if (modelType==3) {
			return getPVecTwoStageLearningCombine();
		}
		else{
			return getPVecTwoStageLearningCombine2();
		}
	}
	
	
	public double[] getPVecSimple(){
		double[] v=new double[npop.length+4];
		for (int i=0; i<npop.length; i++){
			v[i]=Math.log(npop[i]);
		}
		v[npop.length]=Math.log(tutorVar);
		v[npop.length+1]=propAttractiveSongs;
		v[npop.length+2]=Math.log(mutationRate);
		v[npop.length+3]=Math.log(numPossibleSongs);
		return v;
	}
	
	public double[] getPVecConformistBias(){
		double[] v=new double[npop.length+6];
		for (int i=0; i<npop.length; i++){
			v[i]=Math.log(npop[i]);
		}
		v[npop.length]=Math.log(tutorVar);
		v[npop.length+1]=propAttractiveSongs;
		v[npop.length+2]=Math.log(confBias);
		v[npop.length+3]=Math.log(sampleNumber);
		v[npop.length+4]=Math.log(mutationRate);
		v[npop.length+5]=Math.log(numPossibleSongs);
		return v;
	}
	
	public double[] getPVecTwoStageLearning(){
		double[] v=new double[npop.length+5];
		for (int i=0; i<npop.length; i++){
			v[i]=Math.log(npop[i]);
		}
		//v[npop.length]=Math.log(tutorVar);
		//v[npop.length+1]=propAttractiveSongs;
		v[npop.length]=Math.log(sampleNumber);
		v[npop.length+1]=Math.log(sampleNumber2);
		v[npop.length+2]=Math.log(firstStageWeight);
		v[npop.length+3]=Math.log(mutationRate);
		v[npop.length+4]=Math.log(numPossibleSongs);
		return v;
	}
	
	public double[] getPVecTwoStageLearningCombine(){
		/*
		double[] v=new double[npop.length+7];
		for (int i=0; i<npop.length; i++){
			v[i]=Math.log(npop[i]);
		}
		v[npop.length]=Math.log(tutorVar);
		v[npop.length+1]=propAttractiveSongs;
		v[npop.length+2]=Math.log(sampleNumber);
		v[npop.length+3]=Math.log(sampleNumber2);
		v[npop.length+4]=Math.log(firstStageWeight);
		v[npop.length+5]=Math.log(mutationRate);
		v[npop.length+6]=Math.log(numPossibleSongs);
		*/
		double[] v=new double[4];
		
		v[0]=Math.log(sampleNumber);
		v[1]=Math.log(sampleNumber2);
		v[2]=Math.log(firstStageWeight);
		v[3]=Math.log(mutationRate);
		return v;
	}
	
	public double[] getPVecTwoStageLearningCombine2(){
		double[] v=new double[npop.length+8];
		for (int i=0; i<npop.length; i++){
			v[i]=Math.log(npop[i]);
		}
		v[npop.length]=Math.log(tutorVar);
		v[npop.length+1]=propAttractiveSongs;
		v[npop.length+2]=Math.log(sampleNumber);
		v[npop.length+3]=Math.log(sampleNumber2);
		v[npop.length+4]=Math.log(firstStageWeight);
		v[npop.length+5]=Math.log(confBias);
		v[npop.length+6]=Math.log(mutationRate);
		v[npop.length+7]=Math.log(numPossibleSongs);
		return v;
	}
	
	public boolean setPVec(double[] v, int modelType){
		
		//System.out.println("Particles: "+v.length);
		
		if (modelType==0){
			return setPVecSimple(v);
		}
		else if (modelType==1){
			return setPVecConformistBias(v);
		}
		else if (modelType==2) {
			return setPVecTwoStageLearning(v);
		}
		else  if (modelType==3){
			return setPVecTwoStageLearningCombine(v);
		}
		else  {
			return setPVecTwoStageLearningCombine2(v);
		}
	}
	
	public boolean setPVecSimple(double[] v){
		boolean check=true;
		for (int i=0; i<npop.length; i++){
			npop[i]=Math.exp(v[i]);
			if ((npop[i]<nPrior[0])||(npop[i]>nPrior[1])){check=false;}
		}
		tutorVar=Math.exp(v[npop.length]);
		if ((tutorVar<tutorVarPrior[0])||(tutorVar>tutorVarPrior[1])){check=false;}
		propAttractiveSongs=v[npop.length+1];
		if ((propAttractiveSongs<propAttractiveSongsPrior[0])||(propAttractiveSongs>propAttractiveSongsPrior[1])){check=false;}
		mutationRate=Math.exp(v[npop.length+2]);
		if ((mutationRate<mutationRatePrior[0])||(mutationRate>mutationRatePrior[1])){check=false;}
		numPossibleSongs=Math.exp(v[npop.length+3]);
		if ((numPossibleSongs<nPSP[0])||(numPossibleSongs>nPSP[1])){check=false;}
		
		return check;
	}
	
	
	public boolean setPVecConformistBias(double[] v){
		boolean check=true;
		for (int i=0; i<npop.length; i++){
			npop[i]=Math.exp(v[i]);
			if ((npop[i]<nPrior[0])||(npop[i]>nPrior[1])){check=false;}
		}
		tutorVar=Math.exp(v[npop.length]);
		if ((tutorVar<tutorVarPrior[0])||(tutorVar>tutorVarPrior[1])){check=false;}
		propAttractiveSongs=v[npop.length+1];
		if ((propAttractiveSongs<propAttractiveSongsPrior[0])||(propAttractiveSongs>propAttractiveSongsPrior[1])){check=false;}
		confBias=Math.exp(v[npop.length+2]);
		if ((confBias<confBiasPrior[0])||(confBias>confBiasPrior[1])){check=false;}
		sampleNumber=Math.exp(v[npop.length+3]);
		if ((sampleNumber<sampleNumberPrior[0])||(sampleNumber>sampleNumberPrior[1])){check=false;}
		mutationRate=Math.exp(v[npop.length+4]);
		if ((mutationRate<mutationRatePrior[0])||(mutationRate>mutationRatePrior[1])){check=false;}
		numPossibleSongs=Math.exp(v[npop.length+5]);
		if ((numPossibleSongs<nPSP[0])||(numPossibleSongs>nPSP[1])){check=false;}
		
		return check;
	}
	
	public boolean setPVecTwoStageLearning(double[] v){
		boolean check=true;
		for (int i=0; i<npop.length; i++){
			npop[i]=Math.exp(v[i]);
			if ((npop[i]<nPrior[0])||(npop[i]>nPrior[1])){check=false;}
		}
		//tutorVar=Math.exp(v[npop.length]);
		//if ((tutorVar<tutorVarPrior[0])||(tutorVar>tutorVarPrior[1])){check=false;}
		//propAttractiveSongs=v[npop.length+1];
		//if ((propAttractiveSongs<propAttractiveSongsPrior[0])||(propAttractiveSongs>propAttractiveSongsPrior[1])){check=false;}
		sampleNumber=Math.exp(v[npop.length]);
		if ((sampleNumber<sampleNumberPrior[0])||(sampleNumber>sampleNumberPrior[1])){check=false;}
		sampleNumber2=Math.exp(v[npop.length+1]);
		if ((sampleNumber2<sampleNumber2Prior[0])||(sampleNumber2>sampleNumber2Prior[1])){check=false;}
		firstStageWeight=Math.exp(v[npop.length+2]);
		if ((firstStageWeight<firstStageWeightPrior[0])||(firstStageWeight>firstStageWeightPrior[1])){check=false;}
		mutationRate=Math.exp(v[npop.length+3]);
		if ((mutationRate<mutationRatePrior[0])||(mutationRate>mutationRatePrior[1])){check=false;}
		numPossibleSongs=Math.exp(v[npop.length+4]);
		if ((numPossibleSongs<nPSP[0])||(numPossibleSongs>nPSP[1])){check=false;}
		
		return check;
	}
	
	public boolean setPVecTwoStageLearningCombine(double[] v){
		boolean check=true;
		/*
		for (int i=0; i<npop.length; i++){
			npop[i]=Math.exp(v[i]);
			if ((npop[i]<nPrior[0])||(npop[i]>nPrior[1])){check=false;}
		}
		tutorVar=Math.exp(v[npop.length]);
		if ((tutorVar<tutorVarPrior[0])||(tutorVar>tutorVarPrior[1])){check=false;}
		propAttractiveSongs=v[npop.length+1];
		if ((propAttractiveSongs<propAttractiveSongsPrior[0])||(propAttractiveSongs>propAttractiveSongsPrior[1])){check=false;}
		*/
		sampleNumber=Math.exp(v[0]);
		if ((sampleNumber<sampleNumberPrior[0])||(sampleNumber>sampleNumberPrior[1])){check=false;}
		sampleNumber2=Math.exp(v[1]);
		if ((sampleNumber2<sampleNumber2Prior[0])||(sampleNumber2>sampleNumber2Prior[1])){check=false;}
		firstStageWeight=Math.exp(v[2]);
		if ((firstStageWeight<firstStageWeightPrior[0])||(firstStageWeight>firstStageWeightPrior[1])){check=false;}
		mutationRate=Math.exp(v[3]);
		if ((mutationRate<mutationRatePrior[0])||(mutationRate>mutationRatePrior[1])){check=false;}
		/*
		numPossibleSongs=Math.exp(v[npop.length+6]);
		if ((numPossibleSongs<nPSP[0])||(numPossibleSongs>nPSP[1])){check=false;}
		*/
		return check;
	}
	
	public boolean setPVecTwoStageLearningCombine2(double[] v){
		boolean check=true;
		for (int i=0; i<npop.length; i++){
			npop[i]=Math.exp(v[i]);
			if ((npop[i]<nPrior[0])||(npop[i]>nPrior[1])){check=false;}
		}
		tutorVar=Math.exp(v[npop.length]);
		if ((tutorVar<tutorVarPrior[0])||(tutorVar>tutorVarPrior[1])){check=false;}
		propAttractiveSongs=v[npop.length+1];
		if ((propAttractiveSongs<propAttractiveSongsPrior[0])||(propAttractiveSongs>propAttractiveSongsPrior[1])){check=false;}
		sampleNumber=Math.exp(v[npop.length+2]);
		if ((sampleNumber<sampleNumberPrior[0])||(sampleNumber>sampleNumberPrior[1])){check=false;}
		sampleNumber2=Math.exp(v[npop.length+3]);
		if ((sampleNumber2<sampleNumber2Prior[0])||(sampleNumber2>sampleNumber2Prior[1])){check=false;}
		firstStageWeight=Math.exp(v[npop.length+4]);
		if ((firstStageWeight<firstStageWeightPrior[0])||(firstStageWeight>firstStageWeightPrior[1])){check=false;}
		confBias=Math.exp(v[npop.length+5]);
		if ((confBias<confBiasPrior[0])||(confBias>confBiasPrior[1])){check=false;}
		mutationRate=Math.exp(v[npop.length+6]);
		if ((mutationRate<mutationRatePrior[0])||(mutationRate>mutationRatePrior[1])){check=false;}
		numPossibleSongs=Math.exp(v[npop.length+7]);
		if ((numPossibleSongs<nPSP[0])||(numPossibleSongs>nPSP[1])){check=false;}
		
		return check;
	}
	
	public void reportParameters(){
		
		DecimalFormat myFormat=new DecimalFormat("0.000");
		
		double[] v=getPVec(modelType);
		for (int i=0; i<v.length; i++){
			System.out.print(myFormat.format(v[i])+" ");
		}
		System.out.println();
	}
	
	
	

}
