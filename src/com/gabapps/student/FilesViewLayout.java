package com.gabapps.student;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.R;
import com.artifex.mupdfdemo.SettingsActivity;
import com.gabapps.student.interfaces.OnCopyPasteListener;
import com.gabapps.student.interfaces.OnFileSelectedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FilesViewLayout extends FrameLayout {
	public FilesViewLayout(Context context) {
		super(context);
		onCreate(context);
	}
	public FilesViewLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(context);
	}
	public FilesViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		onCreate(context);
	}


	private String workspace = Environment.getExternalStorageDirectory()+"/Student";
	private FilesView filesview;
	private ImageButton parent_folder;
	private ImageButton connect;
	private ImageButton settings;
	private Button new_folder;
	private Button new_page;
	private LinearLayout paste_layout;
	private Button paste;
	private Button cancelpaste;
	
	private String filetopaste;
	
	private OnCopyPasteListener _oncopypaste;
	
	private Context _context;
	private boolean _lock;
	public String blankFile = Environment.getExternalStorageDirectory()+"/Student/.temp/Sans titre.pdf";
	

	protected void onCreate(Context context) {
		_context=context;
		_lock=false;
		setVisibility(INVISIBLE);
		setBackgroundColor(getResources().getColor(R.color.toolbar));
		setLayoutParams(new LayoutParams((int)convertDpToPixel(380, _context), LayoutParams.FILL_PARENT));
		
		LinearLayout layout = (LinearLayout) LinearLayout.inflate(_context, R.layout.explorer, null);
		
		filesview = (FilesView)layout.findViewById(R.id.filesView1);
		parent_folder=(ImageButton)layout.findViewById(R.id.parent_folder);
		new_folder=(Button)layout.findViewById(R.id.new_folder);
		new_page=(Button)layout.findViewById(R.id.new_page);
		paste_layout=(LinearLayout)layout.findViewById(R.id.pastelayout);
		paste=(Button)layout.findViewById(R.id.paste);		
		cancelpaste=(Button)layout.findViewById(R.id.cancel);
		connect=(ImageButton)layout.findViewById(R.id.connectftp);
		settings=(ImageButton)layout.findViewById(R.id.settings_button);
		paste_layout.setVisibility(GONE);
		
		
		//Filesview Config
		
		OnPathChangedListener pathlistener = new OnPathChangedListener() {
			
			@Override
			public void onChanged(String path) {
				if(path.equals(workspace+"/")&&parent_folder.isEnabled()) {
					parent_folder.setEnabled(false);
					parent_folder.setVisibility(INVISIBLE);
					new_folder.setText(R.string.new_discipline);
				}
				if(!path.equals(workspace+"/")&&!parent_folder.isEnabled()) {
					parent_folder.setEnabled(true);
					parent_folder.setVisibility(VISIBLE);
					new_folder.setText(R.string.new_folder);
				}
				
			}
		};
		filesview.setOnPathChangedListener(pathlistener);
		

		final Activity main = (Activity)_context;
		filesview.setOnConnectionListener(new OnConnectionListener() {
			
			@Override
			public void onDisconnect() {
				main.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						connect.setImageResource(R.drawable.ic_connect_off);
					}
				});
			}
			
			@Override
			public void onConnect() {
				main.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						connect.setImageResource(R.drawable.ic_connect_on);
					}
				});
			}
			
			@Override
			public void onPostConnect() {
				SharedPreferences prefs = _context.getSharedPreferences(SettingsActivity.PREFFILE, 0);

				String username = prefs.getString(SettingsActivity.PREFUSER, "");
				String password = prefs.getString(SettingsActivity.PREFPASS, "");
				String address = prefs.getString(SettingsActivity.PREFADDR, "");
				int port = prefs.getInt(SettingsActivity.PREFPORT, 0);
				
				if(port <= 0) port = 21;
				
				if(username.equals("") || password.equals("") || address.equals("")) {
					Intent intentsettings = new Intent(_context, SettingsActivity.class);
					_context.startActivity(intentsettings);
				}
				else {
					FTP.setServer(address, port);
					FTP.setUser(username, password);	
				}
			}
		});
		
		filesview.openWorkspace(workspace);
		filesview.makeBlankFile(blankFile);
		
		//Parent folder button Config
		
		
		parent_folder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				filesview.parentFolder();
				
			}
		});
		
		//New folder button Config
		new_folder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialogNewFolder();
			}
		});
		
		//Paste cancel buttons Config
		
		paste.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				paste();
				
			}
		});
		
		cancelpaste.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cancelpaste();
				
			}
		});
		
		connect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(FTP.isConnected()) {
					filesview.disconnectFTP();
				}
				else {
					filesview.connectFTP();
				}
				
			}
		});
		
		addView(layout);
	}
	
	public void sendnewPageListener(OnClickListener listener) {
		new_page.setOnClickListener(listener);
	}
	
	public void sendSettingsButtonListener(OnClickListener listener) {
		settings.setOnClickListener(listener);
	}
	
	public void startCopy(String file) {
		setVisibility(VISIBLE);
		paste_layout.setVisibility(VISIBLE);
		setLock(true);
		filetopaste=file;
		if(_oncopypaste!=null) _oncopypaste.OnCopyStart(file);
	}
	
	public void paste() {
		int lastSlashPos = filetopaste.lastIndexOf('/');
		String filename = new String(lastSlashPos == -1
					? filetopaste
					: filetopaste.substring(lastSlashPos+1));
		filesview.copy(filetopaste, filesview.getPath()+filename);
		setLock(false);
		paste_layout.setVisibility(GONE);
		refresh();
		if(_oncopypaste!=null) _oncopypaste.OnPaste(filesview.getPath()+filename);
	}
	
	public void updateProgressDownload(View view) {
		final Activity main = (Activity)_context;
		final TextView text = (TextView)view.findViewById(R.id.filename);
		final ProgressBar bar = (ProgressBar)view.findViewById(R.id.downloadprogress);

		FTP.setOnDownloadingListener(new OnDownloadingListener() {

			@Override
			public void onDownloadStarted() {
				main.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						bar.setVisibility(VISIBLE);
						//text.setVisibility(GONE);
					}
				});
			}

			@Override
			public void onDownloadProgressed(int progress) {
			}

			@Override
			public void onDownloadFinished() {
				main.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//text.setVisibility(VISIBLE);
						bar.setVisibility(GONE);
					}
				});

			}
		});
	}
	
	public void cancelpaste() {
		setLock(false);
		filetopaste=null;
		paste_layout.setVisibility(GONE);
	}
	
	public void setOnFileSelected(OnFileSelectedListener value) {
	    filesview.setOnFileSelected(value);
	  }
	
	public void setOnCopyPaste(OnCopyPasteListener value) {
	    _oncopypaste=value;
	  }
	
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}
	
	public void refresh() {
		filesview.setPath(filesview.getPath());
	}
	
	public void setLock(boolean lock) {
		_lock=lock;
	}
	
	public boolean isLocked() {
		return _lock;
	}
	
	
	
	
	public void dialogNewFolder() {
		AlertDialog.Builder builder = new Builder(_context);
		final EditText editText = new EditText(_context);
		editText.setSingleLine();
		builder.setView(editText);
		builder.setTitle(R.string.new_folder);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		builder.setPositiveButton(R.string.new_folder, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newfile=editText.getText().toString();
				Log.d("Debug","EditText content"+newfile);
				if(!newfile.equals("")) {
					filesview.createFolder(newfile);
				}
			}
		});
		
		builder.show();
	}
	public void close() {
		setVisibility(View.INVISIBLE);
	}
	
	public void open() {
		setVisibility(View.VISIBLE);
		filesview.checkFTP();
	}
	
	public FilesView getFileView() {
		return filesview;
	}
}
