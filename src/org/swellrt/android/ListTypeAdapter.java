package org.swellrt.android;

import org.swellrt.model.generic.ListType;
import org.swellrt.model.generic.StringType;
import org.swellrt.model.generic.Type;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListTypeAdapter extends BaseAdapter implements ListType.Listener {


  private final Activity mContext;
  private final ListType mList;
  private final LayoutInflater mInflater;

  public ListTypeAdapter(Activity context, ListType list) {
    this.mContext = context;
    this.mList = list;
    this.mList.addListener(this);
    this.mInflater = (LayoutInflater) this.mContext
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  public ListType getListType() {
    return mList;
  }

  // Base Adapter

  @Override
  public int getItemViewType(int position) {
    return 0;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return mList.size() == 0;
  }

  @Override
  public int getCount() {
    return mList.size();
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public Object getItem(int position) {

    Type t = mList.get(position);

    if (!(t instanceof StringType)) {
      return "<not a string>";
    }

    StringType s = (StringType) t;
    return s.getValue();
  }



  @Override
  public int getViewTypeCount() {
    return 1;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View view = convertView;

    if (convertView == null) {
      view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
      view.setOnLongClickListener(new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {

          mList.remove(v.getId());
          Toast.makeText(mContext, "Item deleted", Toast.LENGTH_LONG).show();
          return true;

        }

      });
    }

    TextView textView = (TextView) view;
    textView.setText((String) getItem(position));
    textView.setId(position);

    return textView;
  }

  // SwellRT listener

  @Override
  public void onValueAdded(Type item) {
    notifyDataSetChanged();
  }

  @Override
  public void onValueRemoved(Type item) {
    notifyDataSetChanged();
  }

}
