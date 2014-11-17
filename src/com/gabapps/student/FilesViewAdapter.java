package com.gabapps.student;

import com.artifex.mupdfdemo.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FilesViewAdapter extends ArrayAdapter<String> {
	  private final Context context;
	  private final String[] names;
	  private final int folder_cursor;

	  static class ViewHolder {
	    public TextView text;
	    public ImageView image;
	  }

	  public FilesViewAdapter(Context context, String[] names, int folder_cursor) {
	    super(context, R.layout.filesview, names);
	    this.context = context;
	    this.names = names;
	    this.folder_cursor=folder_cursor;
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
	      holder.image.setImageResource(R.drawable.ic_dir);
	    } else {
	      holder.image.setImageResource(R.drawable.ic_doc);
	    }

	    return rowView;
	  }
	}