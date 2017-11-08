package com.deitel.voicerecorder;

import java.io.*;

import java.util.*;



   public class em 
   {
   	String file, name,path;
   	byte[] data0=null;
   	byte[] data;
  	byte[] data2;
  	
 	public em(String wavefile,String filepath) throws IOException
  	{
 		
 		file = wavefile;
  		StringTokenizer st = new StringTokenizer(file, ".");
  		
  		name = st.nextToken();
  		path=filepath;
  		
  		FileInputStream in = new FileInputStream(path+File.separator + 
                name + ".wav");       
  		
  		// Create a DataInputStream to read the audio data from the saved file
  		DataInputStream dis = new DataInputStream(in);

  		
  		data0 = new byte[in.available()];
  		
  		
  		int i = 0; // Read the file into the "music" array
  		while (dis.available() > 0) {
  		    // dis.read(music[i]); // This assignment does not reverse the order
  			 
  			
  			data0[i]=dis.readByte();
  			
  			
  			i++;
  		}

  		dis.close();        
  		
  		data = new byte[data0.length-44];
			System.arraycopy(data0, 44, data, 0, data0.length-44);
  	}
 	
  	
 	
 	public String analysis() throws Exception
  	{
  		
  		
  		//語音分割
  		int i,j,k,l,m,n,p;
  		for(k=1;10*k<data.length;k++)
  		{
  			int s=0;
  			for(j=1;j<=10;j++)
  			{
  				s=s+Math.abs(data[10*k-j]);
  			}
  			if(s<1200)
  			{
  				break;
  			}
  		}
  		for(i=1;data.length-10*i>0;i++)
  		{
  			int s=0;
  			for(j=1;j<=10;j++)
  			{
  				s=s+Math.abs(data[data.length-1-10*i+j]);					}
  			if(s<1200)
  			{
  				break;
  			}
  		}
  		data2 = new byte[data.length-10*k-10*i+20];
  		System.arraycopy(data, 10*k-10, data2, 0, data.length-10*k-10*i+20);
 		int frame=299;
 		int window=(int)(data2.length/((frame+1)/2));
 		double[] temp = new double[window];
 		double[] coeff = new double[11];
 		double[] h = new double[21];
 		double[][] a = new double[11][11];
 		double[][] feature = new double[299][20];
 		double[][] feature2 = new double[299][10];
 		double sum,E,K;
 		int fs = 0;
 		for(l=0;l<frame;l++)
 		{
 			//預強調處理
 			temp[0]=(double)data2[fs];
 			for(m=1;m<window;m++)
 			{
 				temp[m]=(double)data2[m+fs]-0.95*(double)data2[m+fs-1];
  			}
  			//漢明窗處理
  			for(m=0;m<window;m++)
  			{
  			temp[m]*=0.54-0.46*Math.cos(m*2.0*3.1415926/(window-1));
  			}
  			//自相關處理
  			for(n=0;n<=10;n++)
  			{
  				coeff[n]=0.0;
  				for(m=n;m<window;m++)
  				{
  					coeff[n]+=temp[m]*temp[m-n];
  				}
  			}
  			//用杜賓公式解LPC系數
  			if(coeff[0]==0.0)
  			{
  				coeff[0]=1.0E-30;
  			}
  			E=coeff[0];
  			for(n=1;n<=10;n++)
 			{
 				sum=0.0;
 				for(m=1;m<n;m++)
 				{
 					sum+=a[m][n-1]*coeff[n-m];
 				}
 				K=(coeff[n]-sum)/E;
 				a[n][n]=K;
 				E*=(1-K*K);
 				for(m=1;m<n;m++)
 				{
 					a[m][n]=a[m][n-1]-K*a[n-m][n-1];
 				}
 			}
 			for(n=1;n<=10;n++)
 			{
 				coeff[n]=a[n][10];
 			}
 			
 			
 			
 			
 			
 			
 			//用遞迴公式解倒頻譜係數並加上帶通提昇的視窗處理
 			h[0]=coeff[0];
 			h[1]=coeff[1];
 			for(p=2;p<=10;p++)
 			{
 				sum=0.0;
 				for(m=1;m<p;m++)
 				{
 					sum+=(double)m/(double)p*h[m]*coeff[p-m];
 				}
 				h[p]=coeff[p]+sum;
 				
 			}
 			
 			for(n=1;n<=10;n++)
 			{
 				coeff[n-1]=h[n]*(1+10/2*Math.sin((3.1415926/10)*n));
 			}
 			for(m=0;m<=9;m++)
 			{
 			feature[l][m]=coeff[m];
 			feature2[l][m] =h[m+1];
 			}
 			
 			//取音框起點
 			/*if((window%2)==0)
 			{
 				fs=fs+(window/2);
 			}
 			else if((window%2)==1);
 			{
 				if((l%2)==0)
 				{
 					fs=fs+(int)((window-1)/2);
 				}
 				else if((l%2)==1)
 				{
 					fs=fs+(int)((window+1)/2);
 				}
 			}*/
 			fs=fs+(window/2);
 		}
 		for(int q = 0;q<299;q++)
 		 {
 			for(int r = 0;r<10;r++)
 			{
 				switch(q)
 				{
 				case 0:
 					h[r+11]=((2*feature2[2][r])+feature2[1][r])/5;
 					break;
 				case 1:
 					h[r+11]=((2*feature2[3][r])+feature2[2][r]-feature2[0][r])/6;
 					break;
 				case 297:
 					h[r+11]=(feature2[298][r]-feature2[296][r]-(2*feature2[295][r]))/6;
 					break;
 				case 298:
 					h[r+11]=((-feature2[297][r])-(2*feature2[296][r]))/5;
 					break;
 				default:
 					h[r+11]=((2*feature2[q+2][r])+feature2[q+1][r]-feature2[q-1][r]+(2*feature2[q-2][r]))/10;
 					
 				}
 				feature[q][r+10]=h[r+11];
 			}
 			
 		 }
 		try
 		{
 			
 			PrintWriter out = new PrintWriter(new FileWriter(path+File.separator + 
                    name + ".txt"), true);		
 			for(l=0;l<299;l++)
 			{
 				out.print("frame "+l+" :");
 				for(m=0;m<=19;m++)
 				{
 					out.print(" "+feature[l][m]);
 				}
 				out.println("");
 			}
 			out.close();
 			data = null;
 			data2=null;
 		}
 		catch(Exception e){System.out.println(e.toString());}
 		/*System.out.println("window="+window);
 		System.out.println("data2="+data2.length);
 		System.out.println("fs="+fs);
 		ByteArrayInputStream bais = new ByteArrayInputStream(data2);
 		AudioInputStream audio2 = new AudioInputStream(bais, format, data2.length / format.getFrameSize());
 		AudioSystem.write(audio2, AudioFileFormat.Type.WAVE, new File("new_"+name+".wav"));*/
 		return name + ".txt";
 	
  	}
 
 	
 }

