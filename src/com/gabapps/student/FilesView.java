package com.gabapps.student;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.gabapps.student.FilesViewAdapter;
import com.gabapps.student.interfaces.OnFileSelectedListener;

import com.artifex.mupdfdemo.R;
import android.content.Context;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


interface OnNotifyEventListener {

  public void onNotify(Object sender);

}

interface OnPathChangedListener {

  public void onChanged(String path);

}

public class FilesView extends ListView {

  public FilesView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    init(context);
  }

  public FilesView(Context context, AttributeSet attrs) {
    super(context, attrs);

    init(context);
  }

  public FilesView(Context context) {
    super(context);

    init(context);
  }

  private void init(Context context) {
    _Context = context;
    setOnItemClickListener(_OnItemClick);
    
    FTP.setServer("192.168.1.13", 21);
	
	FTP.setUser("Gab", "");
	new Thread(new Runnable() {
        public void run() {
			FTP.connect();
			FTP.login();
			String temp = new String(_Path);
			_Path = "waiting";
			setPath(temp);
        }
	}).start();
  }

  private Context _Context = null;
  private ArrayList<String> _List = new ArrayList<String>();
  private ArrayList<String> _FolderList = new ArrayList<String>();
  private ArrayList<String> _FilesView = new ArrayList<String>();
  private FilesViewAdapter _Adapter = null;
  
  //ASyncTask
  
  AsyncTask<String, Void, FTPFile[]> _listftpfiletask;
  private FTPFile[] ftpfiles;
  

  // Property
  private String _Path = "waiting";

  // Event
  private OnPathChangedListener _OnPathChangedListener = null;
  private OnFileSelectedListener _OnFileSelectedListener = null;
  
  private boolean _pathhaschanged = false;
  
  private int ftpfile_cursor=0;
  private int ftpfolder_cursor=0;

  private boolean openPath(String path) {
    _FolderList.clear();
    _FilesView.clear();
    
    String ftppath = path.substring(path.indexOf("Student/")+7, path.length());

    
    if(FTP.logged) {
    	if(_pathhaschanged) {
    		ftpfiles=null;
    		_listftpfiletask = new AsyncTask<String, Void, FTPFile[]>() {
    			@Override
    			protected FTPFile[] doInBackground(String... path) {
    				return FTP.listFTPFiles(path[0]);
    			}

    			@Override
    			protected void onPostExecute(FTPFile[] result) {
    				ftpfiles = result;
    				refresh();
    			}
    		};
    		_listftpfiletask.execute(ftppath);
    	}
    }
    File file = new File(path);
    File[] files = file.listFiles();
    /*if (files == null)
      return false;*/
    if (files != null) {
	    for (int i = 0; i < files.length; i++) {
	    	if(!files[i].getName().startsWith(".")) {
	    		if (files[i].isDirectory()) {
	    	        _FolderList.add(files[i].getName());
	    	      } else {
	    	        _FilesView.add(files[i].getName());
	    	      }
	    	}
	    }
    }
    
    ftpfile_cursor = _FilesView.size();
    ftpfolder_cursor = _FolderList.size();
    if(ftpfiles!=null) {
	    for (int i = 0; i < ftpfiles.length; i++) {
	    	if(!ftpfiles[i].getName().startsWith(".")) {
	    		if (ftpfiles[i].isDirectory()) {
	    	        if(!_FolderList.contains(ftpfiles[i].getName())) _FolderList.add(ftpfiles[i].getName());
    			} 
	    		else {
	    			if(!_FilesView.contains(ftpfiles[i].getName())) _FilesView.add(ftpfiles[i].getName());
	    		}
	    	}
	    }
    }
    
    ftpfile_cursor += _FolderList.size();
    
    

    //Collections.sort(_FolderList);
    //Collections.sort(_FilesView);

    return true;
  }

  private void updateAdapter() {
    _List.clear();
    _List.addAll(_FolderList);
    _List.addAll(_FilesView);
    String [] filesname = _List.toArray(new String[_List.size()]);
    _Adapter = new FilesViewAdapter(_Context,filesname,_FolderList.size(), ftpfile_cursor, ftpfolder_cursor);
    setAdapter(_Adapter);
  }

  public void setPath(String value) {
	  //Log.d("Debug", "New path"+value);
    if (value.length() == 0) {
      value = "/";
    } else {
      String lastChar = value.substring(value.length() - 1,
          value.length());
      if (lastChar.matches("/") == false)
        value = value + "/";
    }
	  boolean changed = !value.equals(_Path);
      _pathhaschanged = changed;

    if (openPath(value)) {
      _Path = value;
      Log.d("Debug", "New path"+_Path);
      updateAdapter();
      if(changed) {
	      if (_OnPathChangedListener != null)
	        _OnPathChangedListener.onChanged(value);
	   }
    }
  }
  
  public void refresh() {
	  setPath(_Path);
  }
  
  public boolean createFolder(String foldername) {
	  File newfolder = new File(_Path+foldername);
	  if(newfolder.exists()) {
		  Log.d("Debug", "Directory already exists");
		  return false;
	  }
	  else {
		  Log.d("Debug", "Trying to create folder : "+_Path+foldername);
		  if(newfolder.mkdir()) {
			  refresh();
			  Log.d("Debug", "Folder created : "+_Path+foldername);
			  return true;
		  }
		  else {
			  Log.d("Debug", "Can't create : "+_Path+foldername);
			  return false;
		  }
	  }
  }
	  
	  public boolean createFile(String filename) throws IOException {
		  File newfolder = new File(_Path+filename);
		  if(newfolder.exists()) {
			  Log.d("Debug", "File already exists");
			  return false;
		  }
		  else {
			  Log.d("Debug", "Trying to create file : "+_Path+filename);
			  if(newfolder.createNewFile()) {
				  setPath(_Path);
				  Log.d("Debug", "File created : "+_Path+filename);
				  return true;
			  }
			  else {
				  Log.d("Debug", "Can't create : "+_Path+filename);
				  return false;
			  }
		  }
  }
  
  public void openWorkspace(String path) {
	  File file = new File(path);
	  if(!file.exists()) {
		  if(file.mkdir()) {
			  Toast.makeText(_Context, "L'espace de travail a �t� cr��", 5).show();
		  }
		  else {
			  Toast.makeText(_Context, "L'espace de travail ne peut �tre cr��", 5).show();
		  }
	  }
	  file = new File(path+"/.temp");
	  file.mkdir();
	  setPath(path);
  }
  
  public void makeBlankFile(String path) {
	  try {
	        InputStream inputStream = getResources().openRawResource(R.raw.blank);
	        File file = new File(path);
	        FileOutputStream fileOutputStream = new FileOutputStream(file);
	        file.mkdir();

	        byte buf[]=new byte[1024];
	        int len;
	        while((len=inputStream.read(buf))>0) {
	            fileOutputStream.write(buf,0,len);
	        }

	        fileOutputStream.close();
	        inputStream.close();
	    } catch (IOException e1) {}
  }
  
  public void copy(String target, String newfile) {
	  try {
	        InputStream inputStream = new FileInputStream(target);
	        FileOutputStream fileOutputStream = new FileOutputStream(newfile);

	        byte buf[]=new byte[1024];
	        int len;
	        while((len=inputStream.read(buf))>0) {
	            fileOutputStream.write(buf,0,len);
	        }

	        fileOutputStream.close();
	        inputStream.close();
	    } catch (IOException e1) {}
  }
  
  public void cut(String target, String newfile) {
	  copy(target, newfile);
	  (new File(target)).delete();
  }
  
  public void parentFolder() {
	  setPath(delteLastFolder(_Path));
  }

  public String getPath() {
    return _Path;
  }

  public void setOnPathChangedListener(OnPathChangedListener value) {
    _OnPathChangedListener = value;
  }

  public OnPathChangedListener getOnPathChangedListener() {
    return _OnPathChangedListener;
  }

  public void setOnFileSelected(OnFileSelectedListener value) {
    _OnFileSelectedListener = value;
  }

  public OnFileSelectedListener getOnFileSelected() {
    return _OnFileSelectedListener;
  }

  public String DelteRight(String value, String border) {
    String list[] = value.split(border);

    String result = "";

    for (int i = 0; i < list.length; i++) {
      result = result + list[i] + border;
    }

    return result;
  }

  private String delteLastFolder(String value) {
    String list[] = value.split("/");

    String result = "";

    for (int i = 0; i < list.length - 1; i++) {
      result = result + list[i] + "/";
    }

    return result;
  }

  private String getRealPathName(String newPath) {
    //String path = newPath.substring(1, newPath.length() - 1);

    /*if (path.matches("..")) {
      return delteLastFolder(_Path);
    } else {*/
      return _Path + newPath;
    //}
  }

  private AdapterView.OnItemClickListener _OnItemClick = new AdapterView.OnItemClickListener() {
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
        long id) {
      String fileName = getItemAtPosition(position).toString();
      //if (new File(_Path+fileName).isDirectory()) {
      if (position < _FolderList.size()) {
        setPath(getRealPathName(fileName));
      } else {
        if (_OnFileSelectedListener != null)
          _OnFileSelectedListener.onSelected(_Path, fileName);
      }
    }
  };

}

