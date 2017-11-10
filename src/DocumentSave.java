


import java.io.*;


public class DocumentSave {

	File file;
	
	PrintWriter Results;
	int rownum=0;
	int colnum=0;
	//Cursor c;
	//boolean suppressChoice=false;
	boolean readyToWrite=false;
	String sep=",";
	
	public DocumentSave(String fileLoc, String sep){
		this.sep=sep;
		file=new File(fileLoc);
		try{
			
			Results = new PrintWriter(new FileWriter(file));

			readyToWrite=true;
		}
		catch(IOException e2){
			
		}
	}
	
	
	public void writeBoolean(boolean b){
		if (b){writeString("true");}
		else{writeString("false");}
	}
	
	public void writeString(String s){
		try{
			Results.print(csvEntry(s));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeInt(int i){
		try{
			Results.print(i+sep);
		}
		catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	public void writeLong(long i){
		try{
			Results.print(i+sep);
		}
		catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	
	public void writeFloat(float i){
		try{
			Results.print(i+sep);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeDouble(double i){
		try{
			Results.print(i+sep);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeDate(long i){
		try{
			Results.print(i+sep);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void writeLine(){
		Results.println();
	}	
	
	
	
	public void finishWriting(){
		Results.close();
	}
	
	public String csvEntry(String s){
		char quot='"';
		StringBuffer sb=new StringBuffer(s);
		boolean encap=true;
		int i=s.lastIndexOf(',');
		int j=s.lastIndexOf(' ');
		if ((i>-1)||(j>-1)){
			sb.insert(0, quot);
			sb.append(quot);
			encap=true;
		}
		j=0; 
		int k=sb.length();
		if (encap){
			j++;
			k--;
		}
		for (i=j; i<k; i++){
			if (sb.charAt(i)==quot){
				sb.insert(i, quot);
				i+=2;
				sb.insert(i, quot);
				k+=2;
			}
		}
		sb.append(",");
		return sb.toString();
	}
	
	public String textEntry(String s){
		String t="\u0009";
		StringBuffer sb=new StringBuffer(s);
		sb.append(t);
		return sb.toString();
	}
	
}
