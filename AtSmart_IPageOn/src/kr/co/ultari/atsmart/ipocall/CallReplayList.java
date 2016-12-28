package kr.co.ultari.atsmart.ipocall;

import java.io.File;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import kr.co.ultari.atsmart.basic.R;

public class CallReplayList extends Activity implements OnClickListener, OnCompletionListener
{
	public static String TAG = "IPageOn";
	
	private ListView list;
	private ArrayList<File> files;
	private ReplayAdapter adapter;
	
	private Button btnDel;
	
	public static CallReplayList CallList;
	public ArrayList<File> delList;
	
	private ImageButton oldPlayButton = null;
	
	private MediaPlayer mp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_call_replay);
		
		CallList = this;
		
		files = new ArrayList<File>();
		
		adapter = new ReplayAdapter(this, R.layout.call_replay_item, files);

		list = (ListView)findViewById(R.id.call_replay_list);
		list.setAdapter(adapter);
		
		btnDel = (Button)findViewById(R.id.replay_delete);
		btnDel.setOnClickListener(this);
		
		delList = new ArrayList<File>();
		
		mp = new MediaPlayer();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		reloadList();
	}

	public void reloadList()
	{
		String folderPath = Environment.getExternalStorageDirectory() + File.separator + "AtSmartRecord";
		
		Log.d(TAG, "ListFile : " + folderPath);
		
		File folder = new File(folderPath);
		
		if ( !folder.exists() ) return;
		
		files.clear();
		
		File[] fl = folder.listFiles();
		
		for ( int i = 0 ; i < fl.length ; i++ )
		{
			files.add(fl[i]);
		}
		
		adapter.notifyDataSetChanged();
	}

	class ReplayAdapter extends ArrayAdapter<File>
	{
		private ArrayList<File> items;

		public ReplayAdapter(Context context, int textViewResourceId, ArrayList<File> items)
		{
			super(context, textViewResourceId, items);
			
			this.items = items;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;

			if (v == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.call_replay_item, null);
			}

			final File f = items.get(position);
			
			if ( f != null )
			{
				TextView fn = (TextView) v.findViewById(R.id.replay_file_name);
				TextView fs = (TextView) v.findViewById(R.id.replay_file_size);
				
				final ImageButton btn = (ImageButton) v.findViewById(R.id.play_replay);
				
				btn.setOnClickListener(new View.OnClickListener()
				{
	                @Override
	                public void onClick(View v)
	                {
	                	try
	                	{
	                		CallList.play(f.getCanonicalPath(), btn);
	                	}
	                	catch(Exception e)
	                	{
	                		Log.e(TAG, "PlaySound", e);
	                	}
	                }
	            });
				
				CheckBox chk = (CheckBox) v.findViewById(R.id.replay_check);
				
				chk.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						if ( isChecked )
						{
							try
							{
								CallList.delList.add(f);
							}
							catch(Exception e) {}
						}
					}
				});
				
				if ( delList.contains(f) ) chk.setChecked(true);
				else chk.setChecked(false);
				
				if ( fn != null)
				{
					fn.setText(getDateFromName(f.getName()));
				}
				
				if (fs != null)
				{
					fs.setText(getFileSizeText(f.length()));
				}
			}
			return v;
		}
	}
	
	private String getDateFromName(String filename)
	{
		if ( filename.indexOf(".wav") < 0 ) return filename;
		filename = filename.substring(0, filename.length() - 4);
		
		if ( filename.length() != 14 ) return filename;
		
		return filename.substring(2, 4) + "/" + filename.substring(4, 6) + "/" + filename.substring(6, 8) + " " + filename.substring(8, 10) + ":" + filename.substring(10, 12) + ":" + filename.substring(12, 14);
	}
	
	private String getFileSizeText( long len )
    {
		String s = len + "";
		int type = 0;
		
		if (s.length() <= 3)
			type = 0;
		else if (s.length() <= 6)
			type = 1;
		else
			type = 2;
		
		String retStr = "";
		int comma = 0;
		int startPosition = 0;
		
		if (type == 0)
			startPosition = s.length() - 1;
		else if (type == 1)
			startPosition = s.length() - 4;
		else
			startPosition = s.length() - 7;
		for (int i = startPosition; i >= 0; i--)
		{
			if ((comma % 3) == 0 && retStr.length() > 0)
				retStr = "," + retStr;
			
			retStr = s.charAt(i) + retStr;
			comma++;
		}
		
		if (type == 0) retStr += " Bytes";
		else if (type == 1) retStr += " KB";
		else retStr += " MB";
		
		return retStr;
    }

	@Override
	public void onClick(View v)
	{
		if ( v == btnDel )
		{
			for ( int i = 0 ; i < delList.size() ; i++ )
			{
				delList.get(i).delete();
			}
			
			reloadList();
		}
	}
	
	public void play(String path, ImageButton playButton)
	{
		if ( oldPlayButton != null )
		{
			oldPlayButton.setSelected(false);
		}
		
		oldPlayButton = playButton;
		playButton.setSelected(true);
		
		mp.reset(); 

		try
		{
	        mp.setDataSource(path);
	        mp.prepare();
	        mp.start();
	        
	        mp.setOnCompletionListener(this);
	        
	        oldPlayButton = playButton;
		}
		catch(Exception e)
		{
			Log.e(TAG, "PlaySound", e);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		if ( oldPlayButton != null )
		{
			oldPlayButton.setSelected(false);
		}
	}
}
