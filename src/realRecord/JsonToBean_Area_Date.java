package realRecord;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.R.integer;

public class JsonToBean_Area_Date {

	public static List<RealRecord> JsonArrayToRealRecordBean(String data, List<Map<String, Object>> JXList) {

		List<RealRecord> listBean = null;
		try {
			JSONArray jsonarray = new JSONArray(data);

			listBean = new ArrayList<RealRecord>();
			listBean.clear();
			// System.out.println("jsonarray.RealRecord :length()---" +
			// jsonarray.length());
			for (int i = 0; i < jsonarray.length(); i++) {

				JSONObject jsonobj = jsonarray.getJSONObject(i);
				RealRecord mReal = new RealRecord();
				mReal.setPotNo(jsonobj.getInt("PotNo"));
				mReal.setRecTime(jsonobj.getString("DDate"));

				/*int recNo = jsonobj.getInt("RecordNo");
				recNo = recNo - 1;
				Map<String, Object> mMap = JXList.get(recNo);
			

				String name2_tmp = mMap.get("jx_name2").toString(); // 记录名 参数2
				String name3_tmp = mMap.get("jx_name3").toString();

				mReal.setRecordNo(mMap.get("jx_name").toString());

				if (jsonobj.get("Val2").equals(null)) {
					mReal.setParam1("");
				} else {
					mReal.setParam1(name2_tmp + jsonobj.getInt("Val2"));
				}
				if (jsonobj.get("Val3").equals(null)) {
					mReal.setParam2("");
				} else {
					mReal.setParam2(name3_tmp + jsonobj.getInt("Val3") + "");
				}*/

				listBean.add(mReal);
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return listBean;
	}
}
