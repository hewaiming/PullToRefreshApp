/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package realRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import android.app.ListActivity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.extras.SoundPullEventListener;
import com.handmark.pulltorefresh.samples.R;
import com.handmark.pulltorefresh.samples.R.id;
import com.handmark.pulltorefresh.samples.R.layout;
import com.handmark.pulltorefresh.samples.R.raw;

public final class PullToRefreshRealRecActivity extends ListActivity {

	static final int MENU_MANUAL_REFRESH = 0;
	static final int MENU_DISABLE_SCROLL = 1;
	static final int MENU_SET_MODE = 2;
	static final int MENU_DEMO = 3;

	private LinkedList<RealRecord> mListItems;
	private PullToRefreshListView mPullRefreshListView;

	// private JSONArrayParser jsonParser = new JSONArrayParser();
	public List<RealRecord> listBean = null;
	private List<Map<String, Object>> JXList = new ArrayList<Map<String, Object>>(); // 记录号名
	private HSView_RealRecordAdapter realRec_Adapter = null;
	private RelativeLayout mHead;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_realrecord);
		initJXList();
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		init_HSView();

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
			/*	String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);*/

				long dateTaken = System.currentTimeMillis();

				String label = DateFormat.format("yyyy:MM:dd kk:mm:ss", dateTaken).toString();

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTask().execute();
			}
		});

		// Add an end-of-list listener
		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				Toast.makeText(PullToRefreshRealRecActivity.this, "到底啦!", Toast.LENGTH_SHORT).show();
			}
		});

		ListView actualListView = mPullRefreshListView.getRefreshableView();

		// Need to use the Actual ListView when registering for Context Menu
		registerForContextMenu(actualListView);

		mListItems = new LinkedList<RealRecord>();
		// mListItems.addAll(Arrays.asList(mStrings));
		realRec_Adapter = new HSView_RealRecordAdapter(getApplicationContext(), R.layout.item_hsview_real_record,
				mListItems, mHead);
		/**
		 * Add Sound Event Listener
		 */
		SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(this);
		soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pull_event);
		soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
		soundListener.addSoundEvent(State.REFRESHING, R.raw.refreshing_sound);
		mPullRefreshListView.setOnPullEventListener(soundListener);

		// You can also just use setListAdapter(mAdapter) or
		// mPullRefreshListView.setAdapter(mAdapter)
		actualListView.setAdapter(realRec_Adapter);
		new GetDataTask().execute();
	}

	private void init_HSView() {
		mHead = (RelativeLayout) findViewById(R.id.head);
		mHead.setFocusable(true);
		mHead.setClickable(true);
		mHead.setBackgroundColor(Color.parseColor("#fffffb"));
		mHead.setOnTouchListener(new ListViewAndHeadViewTouchLinstener());
		// mPullRefreshListView.setOnScrollListener(listener);
		/*
		 * lv_RealRec = (ListView) findViewById(R.id.lv_RealRec);
		 * lv_RealRec.setOnTouchListener(new
		 * ListViewAndHeadViewTouchLinstener());
		 * lv_RealRec.setCacheColorHint(0);
		 * lv_RealRec.setOnScrollListener(this);
		 */

	}

	private void initJXList() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < 60; i++) {
			map.put("id", i);
			map.put("jx_name", "jx_name" + i);
			JXList.add(map);
		}

	}

	private class GetDataTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			List<NameValuePair> mparams = new ArrayList<NameValuePair>();
			mparams.clear();
			int type = 1;
			String Area_PotNo = "11";
			TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
			Date dt = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String EndDate = sdf.format(dt);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -30);
			String BeginDate = sdf.format(cal.getTime());

			if (null != mListItems && mListItems.size() != 0) {
				BeginDate = mListItems.getFirst().getRecTime();
				try {
					Date dt1 = sdf.parse(BeginDate);
					dt1.setSeconds(dt1.getSeconds() + 1);
					BeginDate = sdf.format(dt1);
				} catch (ParseException e) {
					e.printStackTrace();
				}

			}

			if (type == 1) {
				mparams.add(new BasicNameValuePair("areaID", Area_PotNo)); // 全部槽号
				mparams.add(new BasicNameValuePair("BeginDate", BeginDate));
				mparams.add(new BasicNameValuePair("EndDate", EndDate));
			} else if (type == 2) {
				mparams.add(new BasicNameValuePair("PotNo", Area_PotNo)); // 槽号
				mparams.add(new BasicNameValuePair("BeginDate", BeginDate));
				mparams.add(new BasicNameValuePair("EndDate", EndDate));

			}
			String url = "http://125.64.59.11:8000/scgy/android/odbcPhP/RealRecordTable_area_date_test.php";
			JSONArrayParser jsonParser = new JSONArrayParser();
			JSONArray json = jsonParser.makeHttpRequest(url, "POST", mparams);
			if (json != null) {
				// Log.d("json_area_potno", json.toString());// 从服务器返回有数据
				return json.toString();
			} else {
				Log.i("PHP服务器数据返回情况：---", "从PHP服务器无数据返回！");
				return "";
			}

		}

		@Override
		protected void onPostExecute(String data) {
			if (data.equals("")) {
				Toast.makeText(getApplicationContext(), "没有获取到最新[实时记录]数据，请稍后再试！", Toast.LENGTH_SHORT).show();

			} else {
				listBean = new ArrayList<RealRecord>();
				listBean.clear();
				listBean = JsonToBean_Area_Date.JsonArrayToRealRecordBean(data, JXList);
				for (RealRecord tmp : listBean) {
					mListItems.addFirst(tmp);
				}
			}
			realRec_Adapter.notifyDataSetChanged();
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			super.onPostExecute(data);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_MANUAL_REFRESH, 0, "Manual Refresh");
		menu.add(0, MENU_DISABLE_SCROLL, 1, mPullRefreshListView.isScrollingWhileRefreshingEnabled()
				? "Disable Scrolling while Refreshing" : "Enable Scrolling while Refreshing");
		menu.add(0, MENU_SET_MODE, 0,
				mPullRefreshListView.getMode() == Mode.BOTH ? "Change to MODE_PULL_DOWN" : "Change to MODE_PULL_BOTH");
		menu.add(0, MENU_DEMO, 0, "Demo");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		menu.setHeaderTitle("Item: " + getListView().getItemAtPosition(info.position));
		menu.add("Item 1");
		menu.add("Item 2");
		menu.add("Item 3");
		menu.add("Item 4");

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem disableItem = menu.findItem(MENU_DISABLE_SCROLL);
		disableItem.setTitle(mPullRefreshListView.isScrollingWhileRefreshingEnabled()
				? "Disable Scrolling while Refreshing" : "Enable Scrolling while Refreshing");

		MenuItem setModeItem = menu.findItem(MENU_SET_MODE);
		setModeItem.setTitle(
				mPullRefreshListView.getMode() == Mode.BOTH ? "Change to MODE_FROM_START" : "Change to MODE_PULL_BOTH");

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_MANUAL_REFRESH:
			new GetDataTask().execute();
			mPullRefreshListView.setRefreshing(false);
			break;
		case MENU_DISABLE_SCROLL:
			mPullRefreshListView
					.setScrollingWhileRefreshingEnabled(!mPullRefreshListView.isScrollingWhileRefreshingEnabled());
			break;
		case MENU_SET_MODE:
			mPullRefreshListView
					.setMode(mPullRefreshListView.getMode() == Mode.BOTH ? Mode.PULL_FROM_START : Mode.BOTH);
			break;
		case MENU_DEMO:
			mPullRefreshListView.demo();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	class ListViewAndHeadViewTouchLinstener implements View.OnTouchListener {

		public boolean onTouch(View arg0, MotionEvent arg1) {
			// 当在列头 和 listView控件上touch时，将这个touch的事件分发给 ScrollView
			HorizontalScrollView headSrcrollView = (HorizontalScrollView) mHead
					.findViewById(R.id.horizontalScrollView1);
			headSrcrollView.onTouchEvent(arg1);
			return false;
		}
	}

	private String[] mStrings = { "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler", "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler" };
}
