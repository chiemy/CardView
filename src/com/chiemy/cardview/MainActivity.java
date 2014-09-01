package com.chiemy.cardview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chiemy.cardview.view.CardStackAdapter;
import com.chiemy.cardview.view.CardView;
import com.chiemy.cardview.view.CardView.OnCardClickListener;

public class MainActivity extends Activity {
	private List<String> list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		CardView cardView = (CardView) findViewById(R.id.cardView1);
		cardView.setOnCardClickListener(new OnCardClickListener() {
			@Override
			public void onCardClick(View view, int position) {
				Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
			}
		});
		MyCardAdapter adapter = new MyCardAdapter(this);
		adapter.addAll(initData());
		cardView.setAdapter(adapter);
	}
	
	private List<String> initData() {
		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");
		list.add("f");
		list.add("g");
		return list;
	}

	public class MyCardAdapter extends CardStackAdapter<String>{

		public MyCardAdapter(Context context) {
			super(context);
		}

		@Override
		protected View getCardView(int position,
				View convertView, ViewGroup parent) {
			TextView tv = null;
			if(convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
				convertView = inflater.inflate(R.layout.item_layout, parent, false);
			}
			tv = (TextView) convertView.findViewById(R.id.textView1);
			tv.setText(getItem(position).toString());
			return convertView;
		}
	}
	
}
