package com.deitel.voicerecorder;


import java.io.*;
import java.util.*;


import android.os.Environment;
import android.util.*;


public class recog
{
	String path=Environment.getExternalStorageDirectory().getAbsolutePath();
	static int count =1;
	Vector<double[][]> sample = new Vector();
	Vector<Comparable> who =new Vector();
	BufferedReader fin;
	double [][] temp;
	
	

	public String recognize() throws Exception
	{
		String name2="temp";
		
		int z;
		int result = 0;
		double min_dis,dis;
		BufferedReader tempin;
		temp = new double[299][20];
		tempin = new BufferedReader(new InputStreamReader(new FileInputStream(path+"/Android/data/com.deitel.voicerecorder/files/"+name2 + ".txt")));
		String line;
		while((line = tempin.readLine()) != null)
		{
			StringTokenizer st = new StringTokenizer(line);
			st.nextToken();
			int nFrame = Integer.parseInt(st.nextToken());
			st.nextToken();
			for(int i = 0; st.hasMoreTokens() && i < 20; i++)
			{
				temp[nFrame][i] = Double.parseDouble(st.nextToken());
			}
			
		}
		tempin.close();
		min_dis=1.0e38;
		for(z=0;z<sample.size();z++)
		{
			dis= new dtw().dtwm(299,temp,299,(double [][])sample.get(z));
			System.out.println(dis);
			
			if(dis<min_dis)
			{
				result=z;
				System.out.println(result);
				min_dis=dis;
			}
		}
		
		Enumeration list = who.elements();
		String name3;
		temp = null;
		int nSamples;
		int nCount = 0;
		
		while(list.hasMoreElements())
		{
			name3 = (String)list.nextElement();
			nSamples = ((Integer)list.nextElement()).intValue();
			System.out.println(nSamples);
			nCount += nSamples;
			System.out.println(nCount);
		if(nCount > result){
				Log.v("answer",name3);
				return name3;
		}
		}
		return "none";
	}
	public void getFile(String filename, int nRows) throws Exception
	{
		String name = getName(filename);	
		temp = new double[299][20];
		fin = new BufferedReader(new InputStreamReader(new FileInputStream(path+"/Android/data/com.deitel.voicerecorder/files/"+name + ".txt")));
		String line;
		while((line = fin.readLine()) != null)
		{
			StringTokenizer st = new StringTokenizer(line);
			st.nextToken();
			int nFrame = Integer.parseInt(st.nextToken());
			st.nextToken();
			for(int i = 0; st.hasMoreTokens() && i < 20; i++)
			{
				temp[nFrame][i] = Double.parseDouble(st.nextToken());
				
			}
		}
		fin.close();
		sample.add(temp);
		temp = null;
		if(count%nRows==0)
		{
			who.add(name);
			who.add(new Integer(nRows));
			count = 1;
		}
		else
		{
			count++;
		}
	}
	
	private String getName(String filename)
	{
		StringTokenizer st = new StringTokenizer(filename, "_.");
		return st.nextToken();
	}
}
class dtw
{	
	Integer tm1,tm2;
	final static int maxframe = 299;
	public double cal_dx(double [][] test,double [][] ref,int t,int r)
	{
		double temp;
		int o;
		
		temp=0.0;
		for(o=0;o<20;o++)
		{
			temp+=Math.abs(test[t][o]-ref[r][o]);
			
		}
		
		return(temp);
	} 
	public void global_constrain(int n,int N,int M)
	{
		Integer start;
		Integer end;
		if(n/2>M-2*(N-n) && n/2>0)
		{
			start = new Integer(n/2);
		}
		else if(M-2*(N-n)>0)
		{
			start = new Integer(M-2*(N-n));
		}
		else
		{
			start = new Integer("0");
		}
		if((2*n)<M-(N-n)/2 && (2*n)<M)
		{
			end = new Integer(2*n);
		}
		else if(M-(N-n)/2 <M)
		{
			end = new Integer(M-(N-n)/2);
		}
		else
		{
			end = new Integer(M);
		}
		tm1=start;
		tm2=end; 
	}
	public double dtwm(int frames,double [][] test,int r_frame,double [][] ref)
	{
		int x,y;
		int t,r;
		int [] p= new int[299];
		Integer t1,t2;
		double [][] D=new double[299][299];
		double minD,temp;

		if(frames>maxframe)
		{
			frames=maxframe;
		}
		for(t=0;t<maxframe;t++)
		{
			for(r=0;r<299;r++)
			{
				D[t][r]=1.0e38;
			}
		}
		p[0]=0;
		D[0][0]=cal_dx(test,ref,0,0);
		minD=1.0e38;
		for(r=0;r<=2;r++)
		{
			D[1][r]=cal_dx(test,ref,1,r);
			if(D[1][r]<minD)
			{
				minD=D[1][r];
				p[1]=r;
			}
		}
		for(t=2;t<299;t++)
		{
			global_constrain(t,299,299);
			//System.out.println(tm1.intValue() + " " + tm2.intValue());
			for(r=tm1.intValue();r<tm2.intValue();r++)
			{
					minD=D[t-1][r];
					p[t]=r;
					if(D[t-1][r-1]<minD)
					{
					minD=D[t-1][r-1];
					p[t]=r-1;
					}
					if(r >= 2 && D[t-1][r-2]<minD)
					{
						minD=D[t-1][r-2];
						p[t]=r-2;
					}
					D[t][r]=cal_dx(test,ref,t,r)+minD;
					
			}
		}
		temp=D[298][298];
		return(temp);
	}	
}
