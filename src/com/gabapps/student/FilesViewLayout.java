package com.gabapps.student;

import com.artifex.mupdfdemo.R;
import com.gabapps.student.interfaces.OnFileSelectedListener;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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


	String workspace = Environment.getExternalStorageDirectory()+"/Student";
	FilesView filesview;
	Button parent_folder;
	Button new_folder;
	Context _context;
	

	protected void onCreate(Context context) {
		_context=context;
		setBackgroundColor(0xff2d2d2d);
		setLayoutParams(new LayoutParams(380, LayoutParams.FILL_PARENT));
		
		LinearLayout layout = (LinearLayout) LinearLayout.inflate(_context, R.layout.explorer, null);
		
		filesview = (FilesView)layout.findViewById(R.id.filesView1);
		parent_folder=(Button)layout.findViewById(R.id.parent_folder);
		new_folder=(Button)layout.findViewById(R.id.new_folder);
		
		//Filesview Config
		
		OnPathChangedListener pathlistener = new OnPathChangedListener() {
			
			@Override
			public void onChanged(String path) {
				if(path.equals(workspace+"/")&&parent_folder.isEnabled()) {
					parent_folder.setEnabled(false);
					new_folder.setText("Nouvelle matière");
				}
				if(!path.equals(workspace+"/")&&!parent_folder.isEnabled()) {
					parent_folder.setEnabled(true);
					new_folder.setText("Nouveau dossier");
				}
				
			}
		};
		filesview.setOnPathChangedListener(pathlistener);
		filesview.openWorkspace(Environment.getExternalStorageDirectory()+"/Student");
		
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
		
		addView(layout);
	}
	
	public void setOnFileSelected(OnFileSelectedListener value) {
	    filesview.setOnFileSelected(value);
	  }
	
	
	public void dialogNewFolder() {
		AlertDialog.Builder builder = new Builder(_context);
		final EditText editText = new EditText(_context);
		editText.setSingleLine();
		builder.setView(editText);
		builder.setTitle("Nouveau Dossier");
		builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		builder.setPositiveButton("Nouveau dossier", new DialogInterface.OnClickListener() {
			
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
}
