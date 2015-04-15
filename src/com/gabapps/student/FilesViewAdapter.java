package com.gabapps.student;

import com.artifex.mupdfdemo.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FilesViewAdapter extends ArrayAdapter<String> {
	  private final Context context;
	  private final String[] names;
	  private final int folder_cursor;
	  private final int ftpfile_cursor;
	  private final int ftpfolder_cursor;
	  
	  static class ViewHolder {
	    public TextView text;
	    public ImageView image;
	  }

	  public FilesViewAdapter(Context context, String[] names, int folder_cursor) {
	    super(context, R.layout.filesview, names);
	    this.context = context;
	    this.names = names;
	    this.folder_cursor=folder_cursor;
	    this.ftpfile_cursor=folder_cursor;
	    this.ftpfolder_cursor=names.length;
	  }
	  
	  public FilesViewAdapter(Context context, String[] names, int folder_cursor, int ftpfile, int ftpdir) {
		    super(context, R.layout.filesview, names);
		    this.context = context;
		    this.names = names;
		    this.folder_cursor=folder_cursor;
		    this.ftpfile_cursor=ftpfile;
		    this.ftpfolder_cursor=ftpdir;
		  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    View rowView = convertView;
	    // reuse views
	    if (rowView == null) {
	      LayoutInflater inflater = (LayoutInflater) context
	    	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	      rowView = inflater.inflate(R.layout.filesview, null);
	      // configure view holder
	      ViewHolder viewHolder = new ViewHolder();
	      viewHolder.text = (TextView) rowView.findViewById(R.id.filename);
	      viewHolder.image = (ImageView) rowView
	          .findViewById(R.id.fileicon);
	      rowView.setTag(viewHolder);
	    }

	    // fill data
	    ViewHolder holder = (ViewHolder) rowView.getTag();
	    String s = names[position];
	    holder.text.setText(s);
	    if (position<folder_cursor) {
	    	if(position>=ftpfolder_cursor) {
	    		//holder.download.setVisibility(View.VISIBLE);
	    		holder.text.setTextColor(Color.CYAN);
	    	}
	    	//else holder.download.setVisibility(View.GONE);
	    	else holder.text.setTextColor(Color.WHITE);
    		holder.image.setImageResource(R.drawable.ic_dir);
	    } else {
	    	if(position>=ftpfile_cursor) {
	    		//holder.download.setVisibility(View.VISIBLE);
	    		holder.text.setTextColor(Color.CYAN);
	    	}
	    	else holder.text.setTextColor(Color.WHITE);
	    	//else holder.download.setVisibility(View.GONE);
    		holder.image.setImageResource(R.drawable.ic_doc);
	    }

	    return rowView;
	  }
	}