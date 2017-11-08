// VoiceRecorder.java
// Main Activity for the VoiceRecorder class.
package com.deitel.voicerecorder;









import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

 
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class VoiceRecorder extends Activity 
{
   private static final String TAG = VoiceRecorder.class.getName();	
   
   private Handler handler; // Handler for updating the visualizer
   private boolean recording; // are we currently recording?
   
   // variables for GUI
   private SQLiteDatabase db;
   private TextView text;
   private VisualizerView visualizer; 
   private ToggleButton recordButton;
   private Button saveButton;
   private Button averageButton;
   private Button recogButton;
   private Button deleteButton;
   private Button viewSavedRecordingsButton;
   private ExtAudioRecorder recorder2;

   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main); // set the Activity's layout
    db = SQLiteDatabase.openOrCreateDatabase(getExternalFilesDir(null).getAbsolutePath() + 
       File.separator + "mydb.db",null);
     
    String sql = "CREATE TABLE sound (_id integer primary key autoincrement, " +
			  "name text)"; 

try {
	db.execSQL(sql);
} catch (Exception e) {
	 
}
     

recogButton = (Button) findViewById(R.id.recog);
      recogButton.setEnabled(false); // disable saveButton initially
      recogButton.setOnClickListener(recogButtonListener);
      averageButton = (Button) findViewById(R.id.average);
      averageButton.setEnabled(true); // disable saveButton initially
      averageButton.setOnClickListener(averageButtonListener);
      recordButton = (ToggleButton) findViewById(R.id.recordButton);
      saveButton = (Button) findViewById(R.id.saveButton);
      saveButton.setEnabled(false); // disable saveButton initially
      deleteButton = (Button) findViewById(R.id.deleteButton);
      deleteButton.setEnabled(false); // disable deleteButton initially
      viewSavedRecordingsButton = 
         (Button) findViewById(R.id.viewSavedRecordingsButton);
      visualizer = (VisualizerView) findViewById(R.id.visualizerView);
      text= (TextView) findViewById(R.id.textView1);
    
      saveButton.setOnClickListener(saveButtonListener);
      deleteButton.setOnClickListener(deleteButtonListener);
      viewSavedRecordingsButton.setOnClickListener(
         viewSavedRecordingsListener);
            
      handler = new Handler(); // create the Handler for visualizer update
   } // end method onCreate
   
   // create the MediaRecorder
   @Override
   protected void onResume()
   {
      super.onResume();
      
      // register recordButton's listener
      recordButton.setOnCheckedChangeListener(recordButtonListener);
   } // end method onResume
   
   // release the MediaRecorder
   @Override
   protected void onPause()
   {
      super.onPause();
      recordButton.setOnCheckedChangeListener(null); // remove listener
      
      if (recorder2 != null)
      {
         handler.removeCallbacks(updateVisualizer); // stop updating GUI
         visualizer.clear(); // clear visualizer for next recording
         recordButton.setChecked(false); // reset recordButton
         viewSavedRecordingsButton.setEnabled(true); // enable
         recorder2.release(); // release MediaRecorder resources
         recording = false; // we are no longer recording
         recorder2 = null; 
         ((File) deleteButton.getTag()).delete(); // delete the temp file
      } // end if
   } // end method onPause

   // starts/stops a recording
   private void add (String name)
	{
		
		
		
		ContentValues values = new ContentValues();
		
		values.put("name", name);
		
		
		
		db.insert("sound", null, values);
		 
	
	}
  public Cursor getAll() {
	    return db.rawQuery("SELECT * FROM sound", null);
	}
   OnCheckedChangeListener recordButtonListener = 
      new OnCheckedChangeListener() 
      {
         @Override
         public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked)
         {
            if (isChecked)
            {
               visualizer.clear(); // clear visualizer for next recording
               saveButton.setEnabled(false); // disable saveButton
               deleteButton.setEnabled(false); // disable deleteButton
               viewSavedRecordingsButton.setEnabled(false); // disable 

               // create MediaRecorder and configure recording options
               if (recorder2 == null)
                   // create MediaRecorder 
               
               recorder2=ExtAudioRecorder.getInstanse(false);
               
               
               try 
               {
                  // create temporary file to store recording
                  File tempFile = File.createTempFile(
                     "VoiceRecorder", ".wav", getExternalFilesDir(null));
                  
                  // store File as tag for saveButton and deleteButton 
                  saveButton.setTag(tempFile);
                  deleteButton.setTag(tempFile);
                  recogButton.setTag(tempFile);
                  
                  // set the MediaRecorder's output file
                  recorder2.setOutputFile(tempFile.getAbsolutePath());
                  recorder2.prepare(); // prepare to record   
                  recorder2.start(); // start recording
                  recording = true; // we are currently recording
                  handler.post(updateVisualizer); // start updating view
               } // end try
               catch (IllegalStateException e) 
               {
                  Log.e(TAG, e.toString());
               } // end catch 
               catch (IOException e) 
               {
                  Log.e(TAG, e.toString());
               } // end catch               
            } // end if
            else
            {
               recorder2.stop(); // stop recording
               recorder2.reset(); // reset the MediaRecorder
               recording = false; // we are no longer recording
               recogButton.setEnabled(true); // enable saveButton
               saveButton.setEnabled(true); // enable saveButton
               deleteButton.setEnabled(true); // enable deleteButton
               recordButton.setEnabled(false); // disable recordButton
            } // end else
         } // end method onCheckedChanged
      }; // end OnCheckedChangedListener

   // updates the visualizer every 50 milliseconds
   Runnable updateVisualizer = new Runnable() 
   {
      @Override
      public void run() 
      {
         if (recording) // if we are already recording
         {
            // get the current amplitude
            int x = recorder2.getMaxAmplitude();
            visualizer.addAmplitude(x); // update the VisualizeView
            visualizer.invalidate(); // refresh the VisualizerView
            handler.postDelayed(this, 50); // update in 50 milliseconds
         } // end if
      } // end method run
   }; // end Runnable

   // saves a recording
   OnClickListener saveButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
         // get a reference to the LayoutInflater service
         LayoutInflater inflater = (LayoutInflater) getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
   
         // inflate name_edittext.xml to create an EditText
         View view = inflater.inflate(R.layout.name_edittext, null);
         final EditText nameEditText = 
            (EditText) view.findViewById(R.id.nameEditText);       
            
         // create an input dialog to get recording name from user
         AlertDialog.Builder inputDialog = 
            new AlertDialog.Builder(VoiceRecorder.this);
         inputDialog.setView(view); // set the dialog's custom View
         inputDialog.setTitle(R.string.dialog_set_name_title); 
         inputDialog.setPositiveButton(R.string.button_save, 
            new DialogInterface.OnClickListener()
            { 
               public void onClick(DialogInterface dialog, int which) 
               {
                  // create a SlideshowInfo for a new slideshow
                  String name = nameEditText.getText().toString().trim();
                  
                  if (name.length() != 0)
                  {
                     // create Files for temp file and new file name
                     File tempFile = (File) v.getTag();
                     File newFile = new File(
                        getExternalFilesDir(null).getAbsolutePath() + 
                           File.separator + 
                           name + ".wav");
                     tempFile.renameTo(newFile); // rename the file
                      
					try {
						
						String em = new em(name,getExternalFilesDir(null).getAbsolutePath()).analysis();
						add(name);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
			  		
					
			  		
					
      		
      					
      				
      					
      				
                     saveButton.setEnabled(false); // disable 
                     deleteButton.setEnabled(false); // disable 
                     recordButton.setEnabled(true); // enable 
                     viewSavedRecordingsButton.setEnabled(true); // enable 
                  } // end if
                  else
                  {
                     // display message that slideshow must have a name
                     Toast message = Toast.makeText(VoiceRecorder.this, 
                        R.string.message_name, Toast.LENGTH_SHORT);
                     message.setGravity(Gravity.CENTER, 
                        message.getXOffset() / 2, 
                        message.getYOffset() / 2);
                     message.show(); // display the Toast
                  } // end else
               } // end method onClick 
            } // end anonymous inner class
         ); // end call to setPositiveButton
         
         inputDialog.setNegativeButton(R.string.button_cancel, null);
         inputDialog.show();
      } // end method onClick
   }; // end OnClickListener
   
   OnClickListener averageButtonListener = new OnClickListener() 
   {
      @SuppressWarnings("null")
	@Override
      public void onClick(final View v) 
      {
    	  Cursor cursor = getAll();	//取得SQLite類別的回傳值:Cursor物件
			int rows_num = cursor.getCount();	//取得資料表列數
			String[] sound =new String[3];
			int j=0;
			if(rows_num != 0) {
				cursor.moveToFirst();			//將指標移至第一筆資料
				for(int i=0; i<rows_num; i++) {
					if(i>rows_num-4){
					//String[] sound = null;
					//Double[] sound = null;
					sound[j]=cursor.getString(1);
					System.out.println(sound[j]);
					j++;
					}
					cursor.moveToNext();		//將指標移至下一筆資料
					
					}
				
			
    	  
       
                  // create a SlideshowInfo for a new slideshow
                 
                  
                  if (sound[0].length() != 0)
                  {
                     
                	  double[][] temp = new double[299][20];
                	 
              		BufferedReader tempin = null;
              		
					String[] username=new String[3];
              		
					for(int q=0;q<3;q++){
              		try {
						tempin = new BufferedReader(new InputStreamReader(new FileInputStream( getExternalFilesDir(null).getAbsolutePath() + 
						        File.separator + 
						        sound[q] + ".txt")));
						 
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
              		String line;
              		try {
						if(q==0){
              			while((line = tempin.readLine()) != null)
						{
							StringTokenizer st = new StringTokenizer(line);
							st.nextToken();
							int nFrame = Integer.parseInt(st.nextToken());
							st.nextToken();
							for(int i = 0; st.hasMoreTokens() && i <20; i++)
							{
								temp[nFrame][i] = Double.parseDouble(st.nextToken());
							}
							
						}
						}
						if(q==1){
						while((line = tempin.readLine()) != null)
						{
							StringTokenizer st = new StringTokenizer(line);
							st.nextToken();
							int nFrame = Integer.parseInt(st.nextToken());
							st.nextToken();
							for(int i = 0; st.hasMoreTokens() && i < 20; i++)
							{
								temp[nFrame][i] = (temp[nFrame][i]+Double.parseDouble(st.nextToken()))/3;
							}
							
						}
						}
						if(q==2){
						while((line = tempin.readLine()) != null)
						{
							StringTokenizer st = new StringTokenizer(line);
							st.nextToken();
							int nFrame = Integer.parseInt(st.nextToken());
							st.nextToken();
							for(int i = 0; st.hasMoreTokens() && i < 20; i++)
							{
								temp[nFrame][i] =temp[nFrame][i]+ (Double.parseDouble(st.nextToken())/3);
							}
							
						}
						}	
						
						tempin.close();
							Log.v( "yo", "here");
						
					} catch (NumberFormatException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					}
              		
					
              		PrintWriter out;
					
						try {
							out = new PrintWriter(new FileWriter(getExternalFilesDir(null).getAbsolutePath() + 
							        File.separator + 
							        sound[0] + "A.txt"), true);
							for(int l = 0;l<299;l++)
		         			{
		         				out.print("frame "+l+" :");
		         				for(int m = 0;m<=19;m++)
		         				{
		         					out.print(" "+temp[l][m]);
		         				}
		         				out.println("");
		         			}
		         			out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
      		
      					
      				
                  }	
                  }
                  }  
   };// end if
                 
   
   
   OnClickListener recogButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
    	// create Files for temp file and new file name
          File tempFile = (File) v.getTag();
          File newFile = new File(
             getExternalFilesDir(null).getAbsolutePath() + 
                File.separator + "temp.wav");
          tempFile.renameTo(newFile); // rename the file
          
          String name = "temp";
       // get a reference to the LayoutInflater service
	              	
          try {
				
				String em = new em(name,getExternalFilesDir(null).getAbsolutePath()).analysis();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
	  		
			 try {
				 	
					recog h = new recog();
					Cursor cursor = getAll();	//取得SQLite類別的回傳值:Cursor物件
					int rows_num = cursor.getCount();	//取得資料表列數
			 
					if(rows_num != 0) {
						cursor.moveToFirst();			//將指標移至第一筆資料
						for(int i=0; i<rows_num; i++) {
							
							String sound = cursor.getString(1);
							System.out.println(sound);
							h.getFile(sound, 1);
							cursor.moveToNext();		//將指標移至下一筆資料
						}
						
					}
					
					String result = h.recognize();
					text.setText(result);
					System.out.println(result);
			  		System.out.println("end");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
      } // end method onClick // end method onClick
   }; // end OnClickListener  
   // deletes the temporary recording
   OnClickListener deleteButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
         // create an input dialog to get recording name from user
         AlertDialog.Builder confirmDialog = 
            new AlertDialog.Builder(VoiceRecorder.this);
         confirmDialog.setTitle(R.string.dialog_confirm_title); 
         confirmDialog.setMessage(R.string.dialog_confirm_message); 

         confirmDialog.setPositiveButton(R.string.button_delete, 
            new DialogInterface.OnClickListener()
            { 
               public void onClick(DialogInterface dialog, int which) 
               {
                  ((File) v.getTag()).delete(); // delete the temp file
                  saveButton.setEnabled(false); // disable
                  deleteButton.setEnabled(false); // disable
                  recordButton.setEnabled(true); // enable
                  viewSavedRecordingsButton.setEnabled(true); // enable 
               } // end method onClick 
            } // end anonymous inner class
         ); // end call to setPositiveButton
         
         confirmDialog.setNegativeButton(R.string.button_cancel, null);
         confirmDialog.show();         
         recordButton.setEnabled(true); // enable recordButton
      } // end method onClick
   }; // end OnClickListener

   // launch Activity to view saved recordings
   OnClickListener viewSavedRecordingsListener = new OnClickListener() 
   {
     
	   @Override
      public void onClick(View v) 
      {
		  
    	  // launch the SaveRecordings Activity
         Intent intent = 
            new Intent(VoiceRecorder.this, SavedRecordings.class);
         startActivity(intent);
      } // end method onClick
   }; // end OnClickListener
} // end class VoiceRecorder




/**************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/