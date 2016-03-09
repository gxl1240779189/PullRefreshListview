package com.example.pullrefreshlistview;

import java.util.ArrayList;
import java.util.List;

import com.example.pullrefreshlistview.PullRefreshListview.OnRefreshListener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView loadMoreTv;
	private List<String> list;
	private PullRefreshListview lv;
	private LvAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lv = (PullRefreshListview) findViewById(R.id.lv);
		// View footerView =
		// LayoutInflater.from(this).inflate(R.layout.load_more,
		// null);
		// loadMoreTv = (TextView) footerView.findViewById(R.id.loadMoreTv);
		// lv.addFooterView(footerView);
		//
		// loadMoreTv.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// for (int i = 0; i < 10; i++) {
		// list.add("�鿴����" + i);
		// }
		// adapter.notifyDataSetChanged();
		// }
		// });
		list = new ArrayList<String>();
		list.add("������");
		list.add("loonggg");
		list.add("samon");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
		list.add("���Ƕ��ǿ�����");
	
		adapter = new LvAdapter(list, this);
		lv.setAdapter(adapter);

		lv.setListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
						for (int i = 0; i < 20; i++) {
							list.add("ˢ�º���ӵ�����");
						}

						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						adapter.notifyDataSetChanged();
						lv.onRefreshComplete();
					}
				}.execute(null, null, null);
			}
		});
	}
}
