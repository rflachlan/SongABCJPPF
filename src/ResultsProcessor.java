
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;


public class ResultsProcessor {
	
	String outLocation;
	LinkedList<SimulationResults> sr=new LinkedList<SimulationResults>();
	
	public ResultsProcessor(String s, String t, boolean validation){
		this.outLocation=t;
		File file=new File(s);
		parseDataFile(file, 2);
		
		if (validation){LOOVal();}
	
	}
	
	public Parameters[] getParameters(){
		
		Parameters[] p=new Parameters[sr.size()];
		
		for (int i=0; i<sr.size(); i++){
			SimulationResults s=sr.get(i);
			p[i]=s.getParameters();	
		}
		return p;
		
	}
	
	public void LOOVal(){
		
		//System.out.println("SIZE: "+sr.size());
		double threshold=2;
		
		int n=20000;
		double[][] results=new double[n][];
		for (int i=0; i<n; i++){
			
			SimulationResults sres=sr.get(i);
			
			LinkedList<SimulationResults> matchList=new LinkedList<SimulationResults>();
			
			for (SimulationResults sc : sr){
				if (sc!=sres){
					double c=sres.compareSR(sc);
					if (c<threshold){
						matchList.add(sc);
					}
				}
			}	
			results[i]=compareResults(sres, matchList);		
		}
		writeResults(results);
	}
	
	public double[] compareResults(SimulationResults s, LinkedList<SimulationResults> slist){
		int m=s.parameters.length;
		double means[]=new double[m];
		
		double n=0;
		for (SimulationResults sres : slist){
			for (int i=0; i<m; i++){
				means[i]+=sres.parameters[i];
			}
			n++;
		}
		
		for (int i=0; i<m; i++){
			means[i]/=n;
		}
		double[] out=null;
		if(n>0){
			out=new double[2*(m-6)+1];
			out[0]=n;
			for (int i=6; i<m; i++){
				out[(i-6)*2+1]=s.parameters[i];
				out[(i-6)*2+2]=means[i];System.out.print(s.parameters[i]+" "+means[i]+" ");
			}
		}
		return out;
	}
	
	
	public void parseDataFile(File file, int type){
		try{
			String cvsSplitBy = ",";
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=null;

			int offset=42;
			
			while((line=reader.readLine())!=null){
				
				String[] s=line.split(cvsSplitBy);
				
				double[] a=new double[12];
				double[] b=new double[5];
				
				for (int i=0; i<12; i++){
					a[i]=Double.parseDouble(s[i]);
				}
				for (int i=0; i<5; i++){
					b[i]=Double.parseDouble(s[i+offset]);
					//System.out.print(b[i]+" ");
				}
				//System.out.println();
					
				SimulationResults sres=new SimulationResults();
				sres.parameters=a;
				sres.stats=b;
				sr.add(sres);
					
			}
			reader.close();
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
	
		
	}
	
	public void writeResults(double[][] out){
		
		DocumentSave ds=new DocumentSave(outLocation, ",");
		
		
		for (int i=0; i<out.length; i++){
			if (out[i]!=null){
				for (int j=0; j<out[i].length; j++){
					ds.writeDouble(out[i][j]);
				}
				ds.writeLine();
			}
		}

		ds.finishWriting();
	}
}
