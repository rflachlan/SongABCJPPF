
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.node.policy.AtLeast;
import org.jppf.node.protocol.Task;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;




public class SongABCJPPF {
	
	//String fileLoc="/Users/Rob/Desktop/ABCPMC";
	String fileLoc="/home/rflachlan/Desktop/ABCPMC";
	
	//String empFile="Classification189.csv";
	String empFile="Classification160.csv";
	
	//String fileLocation="/Users/Rob/Dropbox/Classification189.csv";
		//String fileLocation="/data/home/btw774/ABCPMC/Classification189.csv";
		//String fileLocation="/Users/Rob/Desktop/sim/Classification189.csv";
		
		//String saveLoc="/Users/Rob/Desktop/abcout";
		//String fileLocation="/data/home/btw774/ABCPMC/Classification189.csv";
		
		EmpData ed;
		
		//Parameters[] pset, psetf;
		Parameters[] pset;
		
		double[][] pvals1, pvals2;
		
		double[][] stats;
		double[][] scores;
		
		double[][][] ageProfile;
		int[][][] freqProfile;
		
		Parameters param;
		
		//double[] eps={25, 20, 17, 16, 15, 14, 13, 12};
		//double[] eps={20, 14, 10, 7, 6, 5, 4, 3, 2, 1.5, 1};
		double[] eps={30, 20, 16, 13, 11, 9, 8, 7, 6, 5, 4, 3, 2, 1.5, 1};
		//double[] eps={10,6, 4, 3, 2, 1.5, 1};
		
		double[] weights;
		double[][] cov;

		int nsamps=1000;
		int npieces=25;
				
		int currentRound=0;
		XoRoShiRo128PlusRandom random;
		
		int nRounds=eps.length;
		
		SimulationRunner[] sr;
		
		
		
		
		
		public SongABCJPPF(String[] args, JPPFClient jppfclient){
			random=new XoRoShiRo128PlusRandom();
			
			//param=new Parameters();
			
			if (args.length>0){
				fileLoc=System.getProperty("user.dir");
				nsamps=Integer.parseInt(args[0]);
			}
			
			
			String fileLocation=fileLoc+System.getProperty("file.separator")+empFile;
			ed=new EmpData(fileLocation);
			
			restartABC(jppfclient);
			
			//ABCRunner(jppfclient);
		
			//simpleRunner(jppfclient);
			
			//elementChecker();
			
			//crossValidation();
			
			//predictiveCheck();
		}
		
		public void crossValidation(){
			//ResultsProcessor rp=new ResultsProcessor("/Users/Rob/Desktop/FinalABCResults/abcout1.csv", true);
			//ResultsProcessor rp=new ResultsProcessor("/Users/Rob/Desktop/abcout1.csv", "/Users/Rob/Desktop/crossval1.csv", true);
			ResultsProcessor rp=new ResultsProcessor("/Users/Rob/Desktop/FinalABCResults/abcout12.csv", "/Users/Rob/Desktop/FinalABCResults/abcout12out.csv", true);
			
		}
		
		
		
		public void elementChecker(){
			
			ElementProcessor ep=new ElementProcessor("/Users/Rob/Desktop/ConneautNoteTypes.csv", random);
			
		}
		
		public void simpleRunner(JPPFClient jppfclient){
			//nsamps=16;
			LinkedList<Parameters> out=simpleRound(0, jppfclient);
			
			writeResults2(out, 0);
		}
		
		public void restartABC(JPPFClient jppfclient){
			File cfile=new File("/home/rflachlan/Desktop/ABCPMC/covmat12.csv");
			cov=readFiles(cfile);
			
			for (int i=0; i<cov.length; i++){
				for (int j=0; j<cov[i].length; j++){
					System.out.print(cov[i][j]+" ");
				}
				System.out.println();
			}
			
			File pfile=new File("/home/rflachlan/Desktop/ABCPMC/abcout12.csv");
			double[][] x=readFiles(pfile);
			
			Parameters[]par=new Parameters[nsamps];
			double[] w=new double[nsamps];
			for (int i=0; i<nsamps; i++){
				par[i]=new Parameters(x[i]);
				w[i]=x[i][x[i].length-1];
				System.out.println(w[i]);
			}	
			pset=par;
			weights=w;
			
			currentRound=12;
			
			pvals1=new double[nsamps][];
			
			for (int i=0; i<pset.length; i++){
				pvals1[i]=pset[i].getPVec();
			}
			
			pvals2=new double[nsamps][];
			
			for (int i=currentRound; i<nRounds; i++){
				iterateRound(jppfclient);
			}
			
		}
		
		
		public double[][] readFiles(File file){
			double[][] out=null;
			try{
				String cvsSplitBy = ",";
				BufferedReader reader=new BufferedReader(new FileReader(file));
				String line=null;
				LinkedList<double[]> data=new LinkedList<double []>();
				

				while((line=reader.readLine())!=null){
					
					String[] s=line.split(cvsSplitBy);
					double[] d=new double[s.length];
					for (int i=0; i<s.length; i++){
						d[i]=Double.parseDouble(s[i]);
					}
					data.add(d);

				}
				System.out.println(data.size());
				out=data.toArray(new double[data.size()][]);
				reader.close();
			}
			
			catch(Exception e){
				e.printStackTrace();
			}
		
			
			return out;
			
		}
		
		public void ABCRunner(JPPFClient jppfclient){
			currentRound=0;
			
			
			
			pset=new Parameters[nsamps];
			pvals1=new double[nsamps][];
			pvals2=new double[nsamps][];
			
			for (int i=0; i<nRounds; i++){
				iterateRound(jppfclient);
			}
		
		}
		
		public void iterateRound(JPPFClient jppfclient){
			System.gc();
			if (currentRound==0){
				firstRound(2, jppfclient);

				weights=calculateWeightsRoundOne();
				
			}
			else{
				standardRound(3, jppfclient);
				//firstRound(2, jppfclient);
				weights=calculate_Weights(weights, cov, pvals1, pvals2);
				
			}
			cov=calculate_CovMat(weights, pvals2);
			//cov2=doubleCovMat(cov);
			updatePVals();	

			//testWeights(cov, pvals1);
			
			currentRound++;
			System.out.println("Finished Round: "+currentRound);
			writeResults(currentRound);
		}
		
		public LinkedList<Parameters> simpleRound(int type, JPPFClient jppfclient){
			try{
			JPPFJob job = new JPPFJob();
		    // give this job a readable name that we can use to monitor and manage it.
		    job.setName("Initial Round");
			
			
			int q=nsamps/npieces;
			  
			  
			for (int i=0; i<q; i++){
				long seed=random.nextLong();
				
				job.add(new SimulationRunner(ed.songFreqs, seed, eps[0], npieces, type, i));
				
			}
			System.out.println("Ready to start ");
			//long GB=1024L*1024L*1024L;
			//job.getSLA().setExecutionPolicy(new AtLeast("freeMemory", 4 * GB));
			//job.getSLA().setSuspended(true);
			
			
			  
			 job.setBlocking(true);

			List<Task<?>> results = jppfclient.submitJob(job);
			
			LinkedList<Parameters> out=new LinkedList<Parameters>();
			for (Task<?> task: results) {
				String taskName = task.getId();
			      // if the task execution resulted in an exception
			    if (task.getThrowable() != null) {
			        // process the exception here ...
			    	System.out.println(taskName + ", an exception was raised: " + task.getThrowable ().getMessage());
			    } else {
			    	LinkedList<Parameters> x = (LinkedList<Parameters>) task.getResult();
			    	
			    	for (Parameters y : x){
						out.add(y);
					}
			    }
			}
			return out;
			}
			catch(Exception e){e.printStackTrace();}
			return null;
		}
		

		public void firstRound(int type, JPPFClient jppfclient){
			try{
			JPPFJob job = new JPPFJob();
		    // give this job a readable name that we can use to monitor and manage it.
		    job.setName("Initial Round");
			
			
			int q=nsamps/npieces;
			  
			
			for (int i=0; i<q; i++){
				long seed=random.nextLong();
				
				job.add(new SimulationRunner(ed.songFreqs, seed, eps[0], npieces, type, i));
				
			}
			System.out.println("Ready to start "+q+" "+npieces+" "+nsamps);
			//long GB=1024L*1024L*1024L;
			//job.getSLA().setExecutionPolicy(new AtLeast("freeMemory", 4 * GB));
			//job.getSLA().setSuspended(true);
			
			
			  
			 job.setBlocking(true);

			List<Task<?>> results = jppfclient.submitJob(job);

			processResults(results, eps[currentRound]);
			  
			extractParamVals();
			 
			}
			catch(Exception e){e.printStackTrace();}
			
		}
		
		public void standardRound(int type, JPPFClient jppfclient){
			try{
				JPPFJob job = new JPPFJob();
			    // give this job a readable name that we can use to monitor and manage it.
			    job.setName("Standard Round ");
				
				
				int q=nsamps/npieces;
				  
				  
				for (int i=0; i<q; i++){
					long seed=random.nextLong();
					
					job.add(new SimulationRunner(ed.songFreqs, pset, weights, cov, seed, eps[currentRound], npieces, type, i));
					  
				}
				System.out.println("Ready to start ");

				job.setBlocking(true);

				List<Task<?>> results = jppfclient.submitJob(job);

				processResults(results, eps[currentRound]);
				
				results=null;
				extractParamVals();
				 
				}
				catch(Exception e){e.printStackTrace();}
				//
				
		}

		
		
		
		
		public void processResults(List<Task<?>> res, double threshold){
			
			LinkedList<Parameters> successes=new LinkedList<Parameters>();
			//LinkedList<Parameters> failures=new LinkedList<Parameters>();
			for (Task<?> task: res) {
				String taskName = task.getId();
			      // if the task execution resulted in an exception
			    if (task.getThrowable() != null) {
			        // process the exception here ...
			    	System.out.println(taskName + ", an exception was raised: " + task.getThrowable ().getMessage());
			    } else {
			    	LinkedList<Parameters> x = (LinkedList<Parameters>) task.getResult();
			    	
			    	System.out.println(x.size());
			    	for (Parameters y : x){
						
						if (y.abcscore<threshold){
							successes.add(y);
						}
						else{
							System.out.println("Oops");
							//failures.add(y);
						}
					}
			    }
			}
			
			pset=successes.toArray(new Parameters[0]);
			//psetf=failures.toArray(new Parameters[0]);
			
			//System.out.println("OUTPUT1: "+successes.size()+" "+failures.size());
			
			
		}

		public double[] calculateWeightsRoundOne(){
			double out[]=new double[nsamps];
			
			for (int i=0; i<nsamps; i++){
				out[i]=1/(nsamps+0.0);
			}	
			return out;
		}
		
		
		//weights=calculate_Weights(weights, cov, pvals1, pvals2);
		public double[] calculate_Weights(double[] pw, double[][] pc, double[][] thetp, double[][] thetc){
			
			int n=thetp.length;
		
			double out[]=new double[n];
			
			for (int i=0; i<n; i++){
				MultivariateNormalDistribution mnd=new MultivariateNormalDistribution(thetc[i], pc);					
				for (int j=0; j<n; j++){				
					double d=mnd.density(thetp[j]);
					d*=pw[j];
					out[i]+=d;			
				}
				out[i]=1/out[i];
			}
			
			double t=0;
			for (int i=0; i<n; i++){
				t+=out[i];
			}
			for (int i=0; i<n; i++){
				out[i]/=t;
			}
			
			return out;
		}
		
		public double[][] calculate_CovMat(double[] pw, double[][] thetas){
			
			int n=pw.length;
			int m=thetas[0].length;
			
			double s1=0;
			double s2=0;
			for (int i=0; i<n; i++){
				s1+=pw[i];
				s2+=pw[i]*pw[i];
			}
			double s3=s1*s1;
			
			double s4=s1/(s3-s2);
			
			System.out.println(s1+" "+s2+" "+s3+" "+s4);
			
			
			double[] wm=new double[m];
			
			for (int i=0; i<m; i++){
				double t=0;
				for (int j=0; j<n; j++){
					wm[i]+=thetas[j][i]*pw[j];
					t+=pw[j];
				}
				wm[i]/=t;
			}
			
			
			double[][] out=new double[m][m];
			
			for (int i=0; i<m; i++){
				for (int j=0; j<m; j++){
					for (int k=0; k<n; k++){
						out[i][j]+=pw[k]*(thetas[k][i]-wm[i])*(thetas[k][j]-wm[j]);
					}
					out[i][j]*=2*s4;
					
					System.out.print(out[i][j]+" ");
				}
				System.out.println();
			}
		
			return out;		
		}
		
		public double[][] doubleCovMat(double[][] cov){
			int n=cov.length;
			double[][] cov2=new double[n][n];
			for (int i=0; i<n; i++){
				for (int j=0; j<n; j++){
					cov2[i][j]=2*cov[i][j];
				}
			}
			
			return cov2;
		}
		
		public void extractParamVals(){
			pvals2=new double[pset.length][];	
			int p=pset.length;
			for (int i=0; i<pset.length; i++){
				//pvals2[i]=pset[i].getPVec(pset[i].modelType);
				pvals2[i]=pset[i].getPVec();
			}
			
			
			stats=new double[pset.length][];
			for (int i=0; i<pset.length; i++){
				stats[i]=pset[i].stats;
			}
			
			
			
			scores=new double[pset.length][];
			for (int i=0; i<pset.length; i++){
				scores[i]=pset[i].score;
			}
			
			
			ageProfile=new double[pset.length][][];
			for (int i=0; i<pset.length; i++){
				ageProfile[i]=pset[i].ageProfile;
			}
			freqProfile=new int[pset.length][][];
			for (int i=0; i<pset.length; i++){
				freqProfile[i]=pset[i].freqProfile;
			}
		}
		
		/*
		public void extractParamVals(){
			pvals2=new double[pset.length+psetf.length][];	
			int p=pset.length;
			for (int i=0; i<pset.length; i++){
				//pvals2[i]=pset[i].getPVec(pset[i].modelType);
				pvals2[i]=pset[i].getPVec();
			}
			for (int i=0; i<psetf.length; i++){
				//pvals2[i+p]=psetf[i].getPVec(psetf[i].modelType);
				pvals2[i+p]=psetf[i].getPVec();
			}
			
			stats=new double[pset.length+psetf.length][];
			for (int i=0; i<pset.length; i++){
				stats[i]=pset[i].stats;
			}
			for (int i=0; i<psetf.length; i++){
				stats[i+p]=psetf[i].stats;
			}
			
			
			scores=new double[pset.length+psetf.length][];
			for (int i=0; i<pset.length; i++){
				scores[i]=pset[i].score;
			}
			for (int i=0; i<psetf.length; i++){
				scores[i+p]=psetf[i].score;
			}
			
			
			ageProfile=new double[pset.length][][];
			for (int i=0; i<pset.length; i++){
				ageProfile[i]=pset[i].ageProfile;
			}
			freqProfile=new int[pset.length][][];
			for (int i=0; i<pset.length; i++){
				freqProfile[i]=pset[i].freqProfile;
			}
		}
		*/
		
		public void updatePVals(){
			for (int i=0; i<pvals1.length; i++){
				pvals1[i]=new double[pvals2[i].length];
				System.arraycopy(pvals2[i], 0, pvals1[i], 0, pvals2[i].length);
			}
		}
		
		public void writeResults(int n){
			String fileLocation=fileLoc+System.getProperty("file.separator")+"abcout"+n+".csv";
			//String fileLoc=saveLoc+" "+n+".csv";
			
			DocumentSave ds=new DocumentSave(fileLocation, ",");
			
			for (int i=0; i<pvals2.length; i++){
				for (int j=0; j<pvals2[i].length; j++){
					ds.writeDouble(pvals2[i][j]);
				}
				for (int j=0; j<stats[i].length; j++){
					ds.writeDouble(stats[i][j]);
				}
				for (int j=0; j<scores[i].length; j++){
					ds.writeDouble(scores[i][j]);
				}
				if (i<weights.length){
					ds.writeDouble(weights[i]);
				}
				ds.writeLine();
			}
			
			ds.finishWriting();
			
			fileLocation=fileLoc+System.getProperty("file.separator")+"songageout"+n+".csv";
			//String fileLoc=saveLoc+" "+n+".csv";
			
			ds=new DocumentSave(fileLocation, ",");
			
			
			for (int i=0; i<ageProfile.length; i++){
				for (int j=0; j<ageProfile[i].length; j++){
					for (int k=0; k<ageProfile[i][j].length; k++){
						if(freqProfile[i][j][k]>0){
							ds.writeDouble(ageProfile[i][j][k]);
						}
					}
					ds.writeLine();
				}
			}
			
			ds.finishWriting();
			
			fileLocation=fileLoc+System.getProperty("file.separator")+"songfreqout"+n+".csv";
			//String fileLoc=saveLoc+" "+n+".csv";
			
			ds=new DocumentSave(fileLocation, ",");
			
			
			for (int i=0; i<ageProfile.length; i++){
				for (int j=0; j<ageProfile[i].length; j++){
					for (int k=0; k<ageProfile[i][j].length; k++){
						if(freqProfile[i][j][k]>0){
							ds.writeInt(freqProfile[i][j][k]);
						}
					}
					ds.writeLine();
				}
			}
			
			ds.finishWriting();
			
			
			fileLocation=fileLoc+System.getProperty("file.separator")+"covmat"+n+".csv";
			//String fileLoc=saveLoc+" "+n+".csv";
			
			ds=new DocumentSave(fileLocation, ",");
			
			
			for (int i=0; i<cov.length; i++){
				for (int j=0; j<cov[i].length; j++){
					ds.writeDouble(cov[i][j]);
				}
				ds.writeLine();
			}
			ds.finishWriting();
			
			
		}
		
		public void writeResults2(LinkedList<Parameters>  out, int n){
			String fileLocation=fileLoc+System.getProperty("file.separator")+"abcout"+n+".csv";
			//String fileLoc=saveLoc+" "+n+".csv";
			
			DocumentSave ds=new DocumentSave(fileLocation, ",");
			
			for (Parameters y: out){
				
				for (int i=0; i<y.stats2.length; i++){
					
					for (int j=0; j<y.paramvals[i].length; j++){
						ds.writeDouble(y.paramvals[i][j]);
					}
					
					for (int j=0; j<y.stats2[i].length; j++){
						ds.writeDouble(y.stats2[i][j]);
					}
					ds.writeLine();
				}
			}
			
			ds.finishWriting();
			
			
			
		}
		

		public static void main (String args[]) {
			try (JPPFClient jppfClient = new JPPFClient()) {
				SongABCJPPF sabc=new SongABCJPPF(args, jppfClient);
			}
		}
		

	}
