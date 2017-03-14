package com.axiang.newcoolweather.android.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.axiang.newcoolweather.android.R;
import com.axiang.newcoolweather.android.activities.MainActivity;
import com.axiang.newcoolweather.android.activities.MyApplication;
import com.axiang.newcoolweather.android.activities.WeatherActivity;
import com.axiang.newcoolweather.android.db.City;
import com.axiang.newcoolweather.android.db.County;
import com.axiang.newcoolweather.android.db.Province;
import com.axiang.newcoolweather.android.util.HttpUtil;
import com.axiang.newcoolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by a2389 on 2017/3/2.
 */

public class ChooseAreaFragment extends Fragment {

    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView listView;

    private Button bacKButton;
    private TextView titleText;

    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    public static final int LEVEL_COUNTY = 3;
    private int currentLevel;

    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<County> countyList = new ArrayList<>();

    public static final String BASICSQL = "http://guolin.tech/api/china/";

    private Province selectProvince;
    private City selectCity;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        listView = (ListView) view.findViewById(R.id.list_view);
        bacKButton = (Button) view.findViewById(R.id.button_back);
        titleText = (TextView) view.findViewById(R.id.text_title);
        adapter = new ArrayAdapter<String>(MyApplication.getContext(),
                android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(currentLevel == LEVEL_PROVINCE) {
                selectProvince = provinceList.get(position);
                queryCityData();
            } else if(currentLevel == LEVEL_CITY) {
                selectCity = cityList.get(position);
                queryCountyData();
            } else if(currentLevel == LEVEL_COUNTY) {
                String weatherId = countyList.get(position).getWeatherId();
                if(getActivity() instanceof MainActivity) {
                    Intent intent = new Intent(getActivity(),
                            WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                } else if (getActivity() instanceof WeatherActivity) {
                    WeatherActivity weatherActivity = (WeatherActivity)
                            getActivity();
                    weatherActivity.drawerLayout.closeDrawers();
                    weatherActivity.weatherId = weatherId;
                    weatherActivity.refreshLayout.setRefreshing(true);
                    weatherActivity.requestWeather(weatherId);
                }
            }
        });
        bacKButton.setOnClickListener(v -> {
            if(currentLevel == LEVEL_CITY) {
                queryProvinceData();
            } else if(currentLevel == LEVEL_COUNTY) {
                queryCityData();
            }
        });
        queryProvinceData();
    }

    private void queryProvinceData() {
        titleText.setText(R.string.guojia);
        bacKButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryDataFromHttp(BASICSQL, "province");
        }
    }

    private void queryCityData() {
        titleText.setText(selectProvince.getProvinceName());
        bacKButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?",
                String.valueOf(selectProvince.getId())).find(City.class);
        if(cityList.size() > 0) {
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            String address = BASICSQL + selectProvince.getProvinceCode();
            queryDataFromHttp(address, "city");
        }
    }

    private void queryCountyData() {
        titleText.setText(selectCity.getCityName());
        bacKButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?",
                String.valueOf(selectCity.getId())).find(County.class);
        if(countyList.size() > 0) {
            dataList.clear();
            for(County county:countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            String address = BASICSQL + selectProvince.getProvinceCode()
                    + "/" + selectCity.getCityCode();
            queryDataFromHttp(address, "county");
        }
    }

    private void queryDataFromHttp(String address, final String type) {
        showProgress();
        HttpUtil.sendHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    closeProgress();
                    Toast.makeText(MyApplication.getContext(), "加载失败",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                String data = response.body().string();
                boolean result = false;
                if("province".equals(type)) {
                    result = Utility.handleProvincesResponse(data);
                } else if("city".equals(type)) {
                    result = Utility.handleCitiesResponse(data,
                            selectProvince.getId());
                } else if("county".equals(type)) {
                    result = Utility.handleCountiesResponse(data,
                            selectCity.getId());
                }
                if(result) {
                    getActivity().runOnUiThread(() -> {
                        closeProgress();
                        if ("province".equals(type)) {
                            queryProvinceData();
                        } else if ("city".equals(type)) {
                            queryCityData();
                        } else if ("county".equals(type)) {
                            queryCountyData();
                        }
                    });
                }
            }
        });
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgress() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
