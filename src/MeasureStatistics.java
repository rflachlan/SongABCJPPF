

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;



public class MeasureStatistics implements Serializable{
	
	XoRoShiRo128PlusRandom random;
	
	double[] empStats, simStats;
	double[] rstats;
	double[] scores;
	int type=0;
	int pop=0;
	
	public MeasureStatistics(XoRoShiRo128PlusRandom random, int[][] empData, int[][] simData, int type, int pop){
		this.random=random;
		this.type=type;
		this.pop=pop;
		scores=calculatePLSDist(empData, simData);
		
		//checkRepSizes(empData);
		//checkRepSizes(simData);
	}
	
	public void checkRepSizes(int[][] fd){
		
		double t=0;
		for (int i=0; i<fd.length; i++){
			t+=fd[i].length;
		}
		System.out.println("Mean rep size: "+(t/(fd.length+0.0)));
	}
	
	
	
	public int[] calculateShareSpectrum(int[][] f){
		int[] shareSpectrum=new int[20];
		
		for (int i=0; i<f.length; i++){
			for (int j=0; j<i; j++){
				int countsh=0;
				for (int g=0; g<f[i].length; g++){
					for (int h=0; h<f[j].length; h++){
						if (f[i][g]==f[j][h]){
							countsh++;
						}
					}
				}
				shareSpectrum[countsh]++;	
			}
		}	
		
		return shareSpectrum;
	}
	
	public double[] simulateShareSpectrum(int[][]f, int reps){
		double[] simSpectrum=new double[20];
		int[][] simPop=new int[f.length][];
		int nso=0;
		
		for (int i=0; i<f.length; i++){
			nso+=f[i].length;
			simPop[i]=new int[f[i].length];
		}
		
		int[] songs=new int[nso];
		nso=0;
		for (int i=0; i<f.length; i++){
			for (int g=0; g<f[i].length; g++){
				songs[nso]=f[i][g];
				nso++;
			}
		}		
		
		for (int i=0; i<reps; i++){
			
			for (int j=0; j<f.length; j++){
				
				for (int k=0; k<simPop[j].length; k++){
					
					boolean found=true;
					while (found){
						found=false;
						int x=songs[random.nextInt(nso)];
						for (int a=0; a<k; a++){
							if (x==simPop[j][a]){
								found=true;
								a=simPop[j].length;
							}
						}
						if (!found){
							simPop[j][k]=x;
						}
					}	
				}
				
				for (int k=0; k<j; k++){
					int countsh=0;
					for (int g=0; g<simPop[j].length; g++){
						for (int h=0; h<simPop[k].length; h++){
							if (simPop[j][g]==simPop[k][h]){
								countsh++;
							}
						}
					}
					simSpectrum[countsh]++;		
				}	
			}	
		}
		
		
		for (int i=0; i<simSpectrum.length; i++){
			simSpectrum[i]/=reps+0.0;
		}
		
		return simSpectrum;
	}
	
	
	public int[] processFreqs(int[][] f){
		
		int maxval=0;
		
		
		for (int i=0; i<f.length; i++){	
			for (int j=0; j<f[i].length; j++){
				if (f[i][j]>maxval){
					maxval=f[i][j];
				}
			}	
		}
		maxval++;
		int[] unfilt=new int[maxval];
		for (int i=0; i<f.length; i++){	
			for (int j=0; j<f[i].length; j++){
				unfilt[f[i][j]]++;
			}	
		}
		
		int pc=0;
		for (int i=0; i<maxval; i++){
			if (unfilt[i]>0){pc++;}
		}
		
		int[] out=new int[pc];
		
		pc=0;
		for (int i=0; i<maxval; i++){
			if (unfilt[i]>0){
				out[pc]=unfilt[i];
				pc++;
			}
		}
		
		return out;
	}
	
	public double logit(double x, double y){
		if (x==0){x=1;}
		if (x==y){x=y-1;}
		
		double z=x/(y-x);
		
		return Math.log(z);
	}
	
	public double[] calculateStats(int[][] freqs){
		
		int[] x=processFreqs(freqs);
		/*
		for (int i=0; i<x.length; i++){
			System.out.print(x[i]+" ");
		}
		System.out.println();
		*/
		
		int t=0;
	
		double h1=0;
		
		double sing1=0;
		double int1=0;
		double com1=0;
		double rare1=0;
		
		double[] counts1=new double[freqs.length+1];
		double alpha1=0;
		
		double sampsize=0;
		for (int i=0; i<x.length; i++){
			sampsize+=x[i];
		}
		
		int commthresh=(int)Math.round(0.05*sampsize);
		double log2=1/Math.log(2);
		
		for (int i=0; i<x.length; i++){
			t+=x[i];
			if (x[i]==1){sing1++;}
			else if (x[i]<commthresh){int1+=x[i];}
			else{com1+=x[i];}
			
			if (x[i]<=4){rare1++;}
			
			double x2=x[i]/sampsize;
			
			h1+=x2*Math.log(x2)*log2;
			counts1[x[i]]++;
			
			alpha1+=Math.log(x[i]*2.0);
		}
		
		//sing1/=sampsize;
		//com1/=sampsize;
		//int1/=sampsize;
		//rare1/=sampsize;
		
		sing1=logit(sing1, sampsize);
		com1=logit(com1, sampsize);
		int1=logit(int1, sampsize);
		rare1= logit(rare1, sampsize);
		
		
		alpha1=1+(x.length/alpha1);
		
		double[] fit1=calculateFit(alpha1, counts1);
		
		int[] p=new int[t];
		
		t=0;
		for (int i=0; i<x.length; i++){
			for (int j=0; j<x[i]; j++){
				p[t]+=x[i];
				t++;
			}
		}
		
		Arrays.sort(p);
		
		int[] y=calculateShareSpectrum(freqs);

		//double[] z=simulateShareSpectrum(freqs, 1000);
		
		double st=0;
		double sts=0;
		for (int i=1; i<6; i++){
			st+=y[i];
			//sts+=z[i];
			//System.out.println(y[i]);
		}
		
		double shareprop=logit(st, y[0]+st);
		double shareprop2=logit(st-y[1], st);
		
		//System.out.println(st+" "+y[1]+" "+sts+" "+z[1]);
		//double shareratio=((st-y[1])-(sts-z[1]))/(st-y[1]);
		//double shareratio2=Math.log((st-y[1])/(sts-z[1]));
		//if ((st==y[1])||(sts==z[1])){
			//shareratio2=Math.log(0.0001);
		//}
		
		
		double d1=linkageDisequilibrium(freqs);
		
		//double d2=expectedSharing2(freqs);
		
		//System.out.println("Linkage Disequ: "+d1);
		
		double[] results=new double[13];
		
		
		results[0]=sing1;
		results[1]=rare1;
		results[2]=int1;
		results[3]=com1;
		//results[4]=Math.log(x.length/sampsize);
		results[4]=logit(x.length, sampsize);
		//results[5]=p[p.length-1]/sampsize;
		results[5]=logit(p[p.length-1], sampsize);
		
		results[6]=h1;
		
		results[7]=alpha1-1;
		results[8]=fit1[0];
		results[9]=fit1[1];
		results[10]=shareprop;
		//results[11]=shareratio2;
		results[11]=shareprop2;
		results[12]=d1;
		//results[13]=d2;
		//results[11]=d1;
		//System.out.println();
		
		
		return results;		
	}
	
	
	public double[] calculatePLSDist(int[][] freqs1, int[][] freqs2){
		//System.out.print("Emp data: ");
		double[] xs=calculateStats(freqs1);
		//System.out.print("Sim data: ");
		
		double[] ys=calculateStats(freqs2);
		rstats=ys;
		/*
		System.out.print("a "+pop+" ");
		for (int i=0; i<xs.length; i++){
			System.out.print((xs[i]-ys[i])+" ");
		}
		System.out.println();
		*/
		/*
		double[] sd={1.346222883,
				1.544285951,
				2.811906572,
				3.563840864,
				1.050956358,
				0.739536457,
				1.349085962,
				0.167761494,
				0.193617635,
				0.155355171,
				1.680012819,
				1.430937384,
				0.474423783};
		for (int i=0; i<13; i++){
			System.out.println(i+" "+xs[i]+" "+ys[i]+" "+((xs[i]-ys[i])/sd[i]));
		}
		*/
	
		/*
		if (type>1){
			for (int i=0; i<13; i++){
				System.out.print(ys[i]+" ");
				if (type==3){
					System.out.print(xs[i]+" ");
				}
			}
			if (type==2){System.out.println();}
		}
		*/
		double[] compsx=calculatePLSComponents(xs);
		double[] compsy=calculatePLSComponents(ys);
		/*
		for (int i=0; i<compsy.length; i++){
			System.out.print(compsx[i]+" ");
		}
		System.out.println();
		*/
		
		empStats=compsx;
		simStats=compsy;
		
		for (int i=0; i<xs.length; i++){
			if (Double.isNaN(xs[i])){System.out.println("NaN error: xs: "+i);}
			if (Double.isNaN(ys[i])){System.out.println("NaN error: ys: "+i);}
		}
		
		double d[]=new double[compsx.length];
		for (int i=0; i<compsx.length; i++){
			//d[i]=Math.abs(compsx[i]-compsy[i]);
			d[i]=compsx[i]-compsy[i];
			if (Double.isNaN(d[i])){System.out.println("NaN error: "+i);}
		}
		/*
		if (type==3){
			for (int i=0; i<compsx.length; i++){
				System.out.print(d[i]+" ");
			}
			System.out.println();
		}
		*/
		//
		
		return d;

	}
	
	public double[] calculatePLSComponents(double[] input){
		/*double[] means={0.065676301,
				0.126486469,
				0.472793286,
				0.461530413,
				-2.06914033,
				0.112886166,
				-4.5456449,
				0.51944852,
				0.326080028,
				-0.143285624,
				-0.070111923,
				-1.614484781};
		*/
		/*
		double[] means={-3.366751126,
				-2.680846861,
				-0.535085376,
				-0.660096489,
				-1.842404619,
				-2.327050642,
				-4.628690453,
				0.50779992,
				0.332755788,
				-0.158650542,
				-0.241224458,
				-1.742588906,
				0.412283493};
		*/
		/*
		double[] means={-3.321806632,
		-2.59706617,
		-0.536626704,
		-0.749484184,
		-1.809416732,
		-2.345836432,
		-4.63933392,
		0.519537631,
		0.328920378,
		-0.154849676,
		-0.187757769,
		-1.705591177,
		0.443515923};
		*/
		
		double[] means={-3.1351143045,
		-2.4495459906,
		-0.4312670815,
		-0.8193544739,
		-1.7155249849,
		-2.3724063804,
		-4.745947627,
		0.5300124366,
		0.2970659171,
		-0.15148097,
		-0.4169377356,
		-1.8669366634,
		0.3382096};


	

		/*
		double[] sd={0.07499966,
				0.135854857,
				0.371525974,
				0.40149376,
				0.918002517,
				0.07429548,
				1.426419801,
				0.175365328,
				0.189114756,
				0.164537164,
				1.880374398,
				1.585998525};*/
		/*
		double[] sd={1.346222883,
				1.544285951,
				2.811906572,
				3.563840864,
				1.050956358,
				0.739536457,
				1.349085962,
				0.167761494,
				0.193617635,
				0.155355171,
				1.680012819,
				1.430937384,
				0.474423783};
		*/
		/*
		double[] sd={1.35080242,
		1.534113774,
		2.897580943,
		3.603725021,
		1.08098822,
		0.775272441,
		1.414957979,
		0.17264124,
		0.185654234,
		0.165769609,
		1.885999071,
		1.562906383,
		0.558142095};
		*/
		
		double[] sd={1.233189008,
		1.3882612717,
		2.5840579208,
		3.3293578441,
		0.979282534,
		0.679345857,
		1.2564976376,
		0.1609667216,
		0.1705248998,
		0.1488665276,
		1.4823699272,
		1.2859282067,
		0.3627164621};


		
		
		
		/*
		double[][] loadings={{0.372842328,0.433903477,0.002412801,0.124406501,0.056459922,0.460515468},
			{0.155608792,0.193596787,-0.461628753,-0.331304089,-0.331996417,-0.162000426},
			{-0.288561345,0.183417501,-0.207060759,-0.027181059,-0.373191995,-0.065218208},
			{0.266242536,-0.203121228,0.204438423,0.020568419,0.365833273,0.041167663},
			{-0.007474499,0.285447623,-0.299154953,0.144056945,0.28651794,0.122498664},
			{0.208339182,-0.420422559,-0.234193433,-0.178896551,0.21902677,0.316606203},
			{0.164238465,-0.322435178,0.109880698,-0.134666298,-0.099685131,-0.047629894},
			{0.383485017,0.083011304,-0.378443896,-0.243772023,-0.005288812,0.215873772},
			{-0.407181201,-0.117786611,0.190058066,-0.302384986,-0.270865283,0.724919207},
			{0.496349147,0.191833119,0.522699678,0.006434972,-0.458747454,-0.007539172},
			{0.202973392,-0.382332351,-0.100390246,-0.303812286,-0.197297136,-0.18234448},
			{0.10848247,-0.365247028,-0.288400456,0.747999043,-0.388214717,0.181117276}};	
		*/
		/*
		double[][] loadings={{0.422594055,-0.011574463,-0.256083229,0.583082404,0.100703681},
			{0.339497645,-0.150055539,-0.659602249,-0.114660691,-0.157829999},
			{-0.01706856,-0.38856542,0.015493523,-0.322307285,0.458979239},
			{-0.063146677,0.361724743,0.033499671,0.189329333,-0.443532301},
			{0.281070288,-0.205974957,-0.025178792,-0.306816704,-0.009373183},
			{0.018290296,0.399454708,0.038589063,0.244571135,0.614874869},
			{-0.13668156,0.303906091,-0.288978489,-0.391562979,-0.179921211},
			{0.477764549,0.044162184,-0.017003179,-0.086864007,0.157030181},
			{-0.463032926,-0.092495476,-0.593730739,0.22889487,0.138976494},
			{0.387836829,0.389378758,0.030607605,-0.153607802,-0.054370417},
			{-0.072616178,0.340712834,-0.226915376,-0.292188401,0.161917604},
			{-0.074235536,0.35066518,-0.084377783,-0.175953178,0.271824605}};
		*/
		/*
		double[][] loadings={{0.443878917,-0.094866144,0.071912912,0.380893203,0.012050662,-0.084732901},
			{0.296396855,-0.235465848,-0.435357928,-0.126597748,-0.489521803,-0.259189079},
			{-0.10274901,-0.328423448,0.217275225,-0.354973974,-0.233488393,0.330373543},
			{-0.00690988,0.301821538,-0.260424167,0.153171757,-0.077743374,-0.23037924},
			{0.178731198,-0.300487431,-0.400189741,0.10156807,0.226479117,0.236193067},
			{0.12302263,0.365845307,0.088803235,0.323194485,0.094613542,0.534171208},
			{-0.017246807,0.303041314,-0.352269469,-0.286408536,-0.261801827,0.09491684},
			{0.40385011,-0.145858337,-0.351283754,0.033869769,0.278351686,0.25783961},
			{-0.461677438,0.046311322,-0.444395603,-0.167264101,0.513832685,-0.072846055},
			{0.52261153,0.270286282,0.230533326,-0.568451432,0.392308519,-0.261553809},
			{0.061895072,0.343875522,-0.107524089,-0.106892279,-0.213903684,0.179361818},
			{0.057325092,0.341484181,-0.104471569,-0.151232395,-0.166435952,0.332656756},
			{-0.025641751,0.298818736,-0.040274496,0.325677759,-0.044857997,-0.360909195}};
		*/
		/*
		double[][] loadings={{0.464638626,0.010420206,0.252985231,0.364090483,-0.044807993},
				{0.339749356,-0.155752524,-0.507429458,-0.027020399,-0.36944716},
				{-0.04132624,-0.37244399,-0.08809123,-0.106618021,0.171662258},
				{-0.072597272,0.315549299,-0.029902501,-0.035939319,-0.164941549},
				{0.237816275,-0.244870768,-0.46071603,0.085126217,0.130235036},
				{0.037271145,0.372579613,0.059252563,0.30862014,0.340734706},
				{-0.086425271,0.299719361,-0.471042796,-0.252249316,-0.126816372},
				{0.447719916,-0.009627516,-0.191863099,0.291416894,0.404675965},
				{-0.454132871,-0.018338988,-0.315228897,0.093503572,0.50067874},
				{0.427686528,0.352067852,0.115526429,-0.641565897,0.238292397},
				{-0.009086234,0.343144335,-0.256176889,0.044554511,-0.043276539},
				{-0.006763552,0.352334873,-0.107802551,0.123077638,0.166997099},
				{-0.080957984,0.287174252,-0.073709571,0.406841082,-0.39331629}};
		
		*/
		/*
		double[][] loadings={{0.4658025692,0.0067038631,0.2768120957,0.3787896265,-0.0398722387,0.0187885036,0.1390526513,0.062791195},
		{0.3389368991,-0.1591703833,-0.4903167099,-0.0312982441,-0.3873246519,-0.0717783769,-0.0724088908,-0.1174680093},
		{-0.0446826142,-0.3701230602,-0.0645169315,-0.1428827815,0.1575852029,0.4769276316,-0.1455645216,-0.4354681555},
		{-0.0698841577,0.3145287969,-0.0536669319,-0.0084938216,-0.1632625692,-0.5817989984,0.1850104875,-0.1569901205},
		{0.2341596941,-0.2495482649,-0.4763160153,0.1084224286,0.1015696571,0.1320531675,0.5081942947,0.2856587594},
		{0.0421176799,0.3724579162,0.062623075,0.3100485292,0.3140194083,0.1297037961,0.3508761017,-0.3592054213},
		{-0.0817837977,0.3012815582,-0.4586153737,-0.2482170979,-0.1381642066,0.0360711647,0.1609871715,-0.1292358195},
		{0.4465247806,-0.0171738855,-0.2141097809,0.3135548079,0.3846132411,-0.2615071371,-0.4435527617,-0.0958312424},
		{-0.4556956715,-0.0152601633,-0.3436038298,0.1605191479,0.5030048693,-0.1425687445,-0.1456784091,0.3389977337},
		{0.4302618565,0.3442637206,0.0839211364,-0.6272504457,0.2878713722,0.1014671139,-0.1069529374,0.3420186419},
		{-0.0030499169,0.3453944604,-0.2258780358,0.0320723944,-0.047726846,0.1117528033,-0.3940378918,-0.329269029},
		{-0.0018051043,0.3529818387,-0.0999625676,0.0954968216,0.1347496881,0.3819718295,0.2028717799,-0.0385595745},
		{-0.0761276054,0.2898133094,-0.0532222462,0.3713213512,-0.4057605489,0.3682173289,-0.2947677927,0.4355042888}};
		*/
		double[][] loadings={{0.4037858363,-0.1804684317,-0.0235407689,0.208468453,0.034216949,-0.0241268601,0.1840182019,0.0279863452,-0.5447861696,0.2047208565},
			{0.2474774017,-0.3230331938,-0.404573521,-0.2225843027,-0.0934655434,-0.4208361254,0.0453201236,-0.1756986804,-0.075526955,-0.0961547721},
			{-0.1476708798,-0.265998118,0.3427994333,-0.3667711799,-0.0898376145,0.0258216262,0.0580620285,-0.3735998036,0.3455157889,-0.0597821346},
			{0.0442866363,0.2791151735,-0.2556780377,-0.0266203547,0.3593717377,-0.3630799372,-0.2768275345,0.3654126882,0.1364747635,-0.0553859781},
			{0.1372340945,-0.3393595077,-0.2480719724,0.1894126291,0.3237159818,0.1914740661,0.2601088727,-0.0118514846,0.2080568847,-0.6717090067},
			{0.1963891338,0.4004191198,0.3075744688,0.1770620309,0.4427207708,-0.1691838643,-0.1350758207,-0.3330649852,0.1290289608,-0.1519623272},
			{0.021911002,0.2625411612,-0.4031123948,-0.405956506,0.1139279317,0.1877071208,0.4186652433,0.1572381646,0.3214095231,0.2915825037},
			{0.3906064617,-0.2011754341,-0.1594749651,0.1996481384,0.2000299434,0.1558650504,-0.1998907339,-0.3716204658,0.338503254,0.5249096952},
			{-0.4514691092,0.0608230846,-0.4852699294,0.1921373648,0.0186193447,0.3938719143,-0.3614087367,-0.2941266853,-0.1761096252,-0.0341917725},
			{0.5562341394,0.1567132862,0.0226643502,-0.1545810332,-0.3956369308,0.4245209111,-0.3923859423,0.2092554615,0.1202854682,-0.2546673683},
			{0.1053759872,0.3045610812,-0.2201810826,-0.3392787289,-0.1829231321,-0.248624723,-0.0957656583,-0.4840878459,-0.2122235216,-0.1901483581},
			{0.1305386041,0.3601526716,0.0718954405,-0.0744479472,0.1667568382,0.3520135733,0.4344819956,-0.209414914,-0.2852859532,-0.060967031},
			{0.011106329,0.2807040393,-0.1384215392,0.56428238,-0.5300427377,-0.2095871669,0.3168770138,-0.1133628485,0.3313041207,-0.0383159843}};
		//System.out.println(loadings.length+" "+loadings[0].length);
		
		double[] out=new double[loadings[0].length];
		
		for (int i=0; i<loadings[0].length; i++){
			for (int j=0; j<loadings.length; j++){
				out[i]+=((input[j]-means[j])/sd[j])*loadings[j][i];
			}
		}
		return out;
		
	}
	
	
	public double calculateStats(int[] x, int[] y){
		
		int t=0;
		
		int sing1=0;
		
		double h1=0;
		
		double int1=0;
		double com1=0;
		
		double rare1=0;
		
		double[] counts1=new double[200];
		double[] counts2=new double[200];
		double alpha1=0;
		double alpha2=0;
		
		
		for (int i=0; i<x.length; i++){
			t+=x[i];
			if (x[i]==1){sing1++;}
			else if (x[i]<10){int1+=x[i];}
			else{com1+=x[i];}
			
			if (x[i]<=4){rare1++;}
			
			h1+=x[i]*Math.log(x[i]);
			counts1[x[i]]++;
			
			alpha1+=Math.log(x[i]*2.0);
		}
		
		alpha1=1+(x.length/alpha1);
		
		double singleRatio1=sing1/rare1;
		
		h1/=x.length+0.0;
		h1+=Math.log(x.length);
		h1/=Math.log(2);
		
		int u=0;
		
		int sing2=0;
		
		double h2=0;
		double int2=0;
		double com2=0;
		double rare2=0;
		
		
		
		for (int i=0; i<y.length; i++){
			u+=y[i];
			if (y[i]==1){sing2++;}
			else if (y[i]<10){int2+=y[i];}
			else{com2+=y[i];}
			
			if (y[i]<=4){rare2++;}
			
			h2+=y[i]*Math.log(y[i]);
			counts2[y[i]]++;
			
			alpha2+=Math.log(y[i]*2.0);
		}
		alpha2=1+(y.length/alpha2);
		
		
		double singleRatio2=sing2/rare2;
		
		//System.out.println(alpha1+" "+alpha2);
		
		h2/=y.length+0.0;
		h2+=Math.log(y.length);
		h2/=Math.log(2);
		
		//System.out.println(t+" "+u);
		
		
		double[] fit1=calculateFit(alpha1, counts1);
		double[] fit2=calculateFit(alpha2, counts2);
		
		/*
		for (int i=0; i<200; i++){
			double alpha=1.00+0.04*i;
			double fit=calculateFit(alpha, counts1);
			System.out.println(Math.round(alpha*100)+" "+alpha1+" "+fit);
		}
		*/
		//System.out.println("FIT: "+fit1+" "+fit2);
		
		if (u>t){t=u;}
		
		int[] p=new int[t];
		int[] q=new int[t];		
		
		t=0;
		for (int i=0; i<x.length; i++){
			for (int j=0; j<x[i]; j++){
				p[t]+=x[i];
				t++;
			}
		}
		
		t=0;
		for (int i=0; i<y.length; i++){
			for (int j=0; j<y[i]; j++){
				q[t]+=y[i];
				t++;
			}
		}
		
		Arrays.sort(p);
		Arrays.sort(q);
		
		int max1=p[p.length-1];
		int max2=q[q.length-1];
		
		double s1=Math.abs(max2-max1)/(max1+max2+0.0);
		
		double s2=Math.abs(sing2-sing1)/(sing1+sing2+0.0);
		
		double s3=Math.abs(h2-h1)/(h1+h2+0.0);
		
		double s4=Math.abs(int2-int1)/(int1+int2+0.0);
		if (int2+int1==0){s4=0;}
		
		double s5=Math.abs(com2-com1)/(com1+com2+0.0);
		if (com2+com1==0){s5=0;}
		
		double s6=Math.abs(alpha2-alpha1)/(alpha1+alpha2+0.0);
		
		double s7=Math.abs(fit2[0]-fit1[0])/(fit1[0]+fit2[0]+0.0);
		
		//double s8=Math.abs(singleRatio1-singleRatio2)/(singleRatio1+singleRatio2);
		double s8=Math.abs(fit2[1]-fit1[1])/(fit1[1]+fit2[1]+0.0);
		
		double s9=Math.abs(y.length-x.length)/(x.length+y.length+0.0);
		
		//System.out.println(singleRatio1+" "+singleRatio2+" "+s8);
		
		//System.out.println(s1+" "+s2+" "+s3+" "+s4+" "+s5+" "+s6+" "+s7+" "+s8+" "+s9);
		//System.out.println(sing1+" "+sing2+" "+x.length+" "+y.length+" "+t+" "+u);
		return s1*s1+s2*s2+s3*s3+s4*s4+s5*s5+s6*s6+s7*s7+s8*s8+s9*s9;
	}
	
	
	public double[] calculateFit(double alpha, double[] x){
		double fmin=0;
		for (int i=0; i<1000; i++){
			fmin+=Math.pow(i+1, -1*alpha);
		}
		
		double[] cum=new double[x.length];
		
		for (int i=cum.length-2; i>0; i--){
			cum[i]=cum[i+1]+x[i];
		}
		double ts=cum[1];
		for (int i=0; i<cum.length; i++){
			cum[i]/=ts;
		}
		
		
		double[] fit={0,0};
		for (int i=0; i<cum.length; i++){		
			if ((cum[i]>0)&&(cum[i]<1)){
				
				double f=0;
				for (int j=0; j<50; j++){
					f+=Math.pow(j+i, -1*alpha);	
				}
				
				f/=fmin;
				
				
				
				double p=Math.abs(f - cum[i]);	
				//System.out.println(i+" "+cum[i]+" "+f+" "+p);
				if (p>fit[0]){fit[0]=p;}
				if (i==2){fit[1]=f - cum[i];}
			}	
		}
		return fit;
		
	}
	
	public LinkedList<int[]> getAllLists(int elements, int lengthOfList, double[] freq){
		LinkedList<int[]> allLists=new LinkedList<int[]>();
		int ll1=lengthOfList-1;
		int ll2=ll1-1;
		
		System.out.println(lengthOfList);
	    if(lengthOfList == 1){
	    	for (int i=0; i<elements; i++){
	    		if (freq[i]>0){
	    			int[] a=new int[7];
	    			Arrays.fill(a, -1);
	    			a[0]=i;
	    			allLists.add(a);
	    		}
	    	}    
	    } 
	    else {
	    	LinkedList<int[]> allSublists = getAllLists(elements, lengthOfList - 1, freq);
	    	System.out.println(lengthOfList+" "+allSublists.size());
	    	for (int[] y : allSublists){
	    		allLists.add(y);
	    		//System.out.println(lengthOfList+" "+y[lengthOfList-1]);
	    		for (int i=0; i<y[ll2]; i++){
	    			if (freq[i]>0){
	    				int[] x=Arrays.copyOf(y, y.length);
	    				x[ll1]=i;
	    				allLists.add(x);
	    			}
	        	}	
	        }    
	    }
	    return allLists;
	}
	
	public LinkedList<int[]> getAllLists(int elements, int lengthOfList){
		LinkedList<int[]> allLists=new LinkedList<int[]>();

	    if(lengthOfList == 1){
	    	for (int i=0; i<elements; i++){
	    		int[] a={i};
	    		allLists.add(a);
	    	}    
	    } 
	    else {
	    	LinkedList<int[]> allSublists = getAllLists(elements, lengthOfList - 1);	  
	    	for (int[] y : allSublists){
	    		allLists.add(y);
	    		for (int i=0; i<y[y.length-1]; i++){
	    			int[] x=combinearray(i, y);
	    			allLists.add(x);
	        	}	
	        }    
	    }
	    return allLists;
	}
	
	public int[] combinearray(int a, int[] b){
		int[] x=new int[b.length+1];
		System.arraycopy(b, 0, x, 0, b.length);
		x[b.length]=a;
		return x;
	}
	
	public double expectedSharing(int[][] f){
		double d=0;
		double n=f.length;
		int maxval=0;
		int maxlength=0;
		double totlength=0;
		for (int i=0; i<f.length; i++){
			for (int j=0; j<f[i].length; j++){
				if (f[i][j]>maxval){maxval=f[i][j];}
			}
			if (f[i].length>maxlength){maxlength=f[i].length;}
			totlength+=f[i].length;
		}
		maxval++;
		maxlength++;
		
		
		double[] freq=new double[maxval];
		int[] lengths=new int[maxlength];
		for (int i=0; i<f.length; i++){
			for (int j=0; j<f[i].length; j++){
				freq[f[i][j]]++;			
			}
			lengths[f[i].length]++;
		}
		
		int types=0;
		for (int i=0; i<freq.length; i++){
			freq[i]/=totlength;
			if (freq[i]>0){types++;}
		}
		System.out.println(maxlength+" "+types);
		LinkedList<int[]> combs2=getAllLists(maxval, maxlength, freq);
		
		System.out.println("DONE! "+combs2.size());
		
		
		
		int[] e=new int[8];
		
		double[] h=new double[8];
		double count=0;
		
		for (int i=0; i<f.length; i++){
			for (int j=0; j<f.length; j++){
				if (i!=j){
					int c=0;
					int a2=f[i].length;
					int b2=f[j].length;
					for (int a=0; a<a2; a++){
						for (int b=0; b<b2; b++){
							if (f[i][a]==f[j][b]){
								c++;
							}
						}
					}
					e[c]++;
					
					double[] g=new double[a2];
					double ftot=0;
					for (int a=0; a<a2; a++){
						g[a]=freq[f[i][a]];	
						ftot+=g[a];
					}
					ftot=1-ftot;
					
					LinkedList<int[]> combs=getAllLists(a2, b2);
					
					double[] h2=new double[8];
					
					double zscore=1;
					for (int a=0; a<b2; a++){
						zscore*=ftot;
					}
					
					
					h2[0]=zscore;
					for (int x[] : combs){
						int t=x.length;
						double v=1;
						for (int u=0; u<t; u++){
							v*=g[x[u]];
						}
						for (int u=t; u<b2; u++){
							v*=ftot;
						}
						h2[t]+=v;
					}
					double fs=0;
					for (int a=0; a<8; a++){
						fs+=h2[a];
					}
					//System.out.println(ftot+" "+zscore+" "+fs);
					for (int a=0; a<8; a++){
						h[a]+=(h2[a]);
					}
					count++;
					
									
					

				}
			}
				
		}
		
		double emptot=0;
		for (int i=0; i<8; i++){
			emptot+=e[i];
		}
		
		for (int i=0; i<8; i++){	
			System.out.println(i+" "+(e[i]/emptot)+" "+(h[i]/count));
		}
		
		
		
		return d;
	}
	
	public double expectedSharing2(int[][] f){
		double d=0;
		double n=f.length;
		int maxval=0;
		int maxlength=0;
		double totlength=0;
		for (int i=0; i<f.length; i++){
			for (int j=0; j<f[i].length; j++){
				if (f[i][j]>maxval){maxval=f[i][j];}
			}
			if (f[i].length>maxlength){maxlength=f[i].length;}
			totlength+=f[i].length;
		}
		maxval++;
		maxlength++;
		
		
		double[] freq=new double[maxval];
		int[] lengths=new int[maxlength];
		for (int i=0; i<f.length; i++){
			for (int j=0; j<f[i].length; j++){
				freq[f[i][j]]++;			
			}
			lengths[f[i].length]++;
		}
		
		int types=0;
		for (int i=0; i<freq.length; i++){
			freq[i]/=totlength;
			if (freq[i]>0){types++;}
		}
		//System.out.println(maxlength+" "+types);
		
		double[] empscores=getShareSpectrum(f);
		
		double[] simscore=new double[8];
		int nrep=1000;
		for (int i=0; i<nrep; i++){
			int[][] sf=simulateSongFreqs(f);
			double[] sscore=getShareSpectrum(sf);
			for (int j=0; j<8; j++){
				simscore[j]+=sscore[j];
			}
		}

		for (int i=0; i<8; i++){
			simscore[i]/=nrep+0.0;
		}
		
		for (int i=0; i<8; i++){	
			System.out.println(i+" "+empscores[i]+" "+simscore[i]);
		}
		
		for (int i=2; i<8; i++){
			d+=empscores[i]-simscore[i];
		}
		
		
		return d;
	}
	
	public int[][] simulateSongFreqs(int[][] f){
		int n=f.length;
		int[][] out=new int[n][];
		boolean found;
		int a,b,c=0;
		for (int i=0; i<n; i++){
			int m=f[i].length;
			out[i]=new int[m];
			for (int j=0; j<m; j++){
				found=false;
				while (!found){
					found=true;
					a=random.nextInt(n);
					b=random.nextInt(f[a].length);
					c=f[a][b];
					for (int k=0; k<j; k++){
						if (out[i][k]==c){
							found=false;
							k=j;
						}
					}
				}
				out[i][j]=c;	
			}
		}
		return out;
	}
	
	public double[] getShareSpectrum(int[][] f){
		double[] e=new double[8];
		for (int i=0; i<f.length; i++){
			for (int j=0; j<f.length; j++){
				if (i!=j){
					int c=0;
					int a2=f[i].length;
					int b2=f[j].length;
					for (int a=0; a<a2; a++){
						for (int b=0; b<b2; b++){
							if (f[i][a]==f[j][b]){
								c++;
							}
						}
					}
					e[c]++;
				}
			}
		}
		
		for (int i=0; i<8; i++){
			e[i]/=f.length*(f.length-1.0);
		}
		return e;
	}
	
	public double linkageDisequilibrium(int[][] f){
		double d=0;
		double n=f.length;
		int maxval=0;
		double tot=0;
		for (int i=0; i<f.length; i++){
			for (int j=0; j<f[i].length; j++){
				if (f[i][j]>maxval){maxval=f[i][j];}
			}
			tot+=f[i].length;
		}
		maxval++;
		
		double[][] tab=new double[maxval][maxval];
		
		double[] freq=new double[maxval];
		
		for (int i=0; i<f.length; i++){
			for (int j=0; j<f[i].length; j++){
				freq[f[i][j]]++;			
				for (int k=0; k<j; k++){
					tab[f[i][j]][f[i][k]]++;
					tab[f[i][k]][f[i][j]]++;
				}
			}	
		}
		
		for (int i=0; i<maxval; i++){
			freq[i]/=tot;
		}
		
		double m=0;
		for (int i=0; i<maxval; i++){
			for (int j=0; j<i; j++){
				tab[i][j]/=n;
				
				if ((freq[i]>0)&&(freq[j]>0)){
					double q=tab[i][j]-(freq[i]*freq[j]);
					double r=q/(Math.sqrt(freq[i]*(1-freq[i])*freq[j]*(1-freq[j])));
					double s=Math.sqrt(freq[i]*freq[j]);
					d+=r*s;
					m+=s;
				}
				
			}
		}

		
		return (d/m);
	}
	
	
}
