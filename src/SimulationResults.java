

public class SimulationResults {
	
	double[] parameters;
	
	double[] stats;
	
	public double compareSR(SimulationResults s){
		
		double x=0;
		
		for (int i=0; i<stats.length; i++){
			double y=stats[i]-s.stats[i];
			x+=y*y;
		}
		
		return Math.sqrt(x);		
	}
	
	public Parameters getParameters(){
		Parameters p=new Parameters(parameters);

		return p;
		
	}
	

}
