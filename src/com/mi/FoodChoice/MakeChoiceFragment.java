package com.mi.FoodChoice;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.gson.Gson;
import com.mi.FoodChoice.Handler.DianPingHandler;
import com.mi.FoodChoice.data.*;
import com.mi.FoodChoice.ui.CircleButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MakeChoiceFragment extends Fragment implements LocationListener, View.OnClickListener {

    private CircleButton mChocieBtn, mFirstBtn, mTrashBtn;
    private ImageButton mSettingBtn;
    private ImageView mLatterEmpty;
    private TextView mShopName, mShopPrice, mShopDistance, mDealListTitle;
    private LinearLayout mShopDetailsLayout;
    private RelativeLayout mDealsListLayout;

    private LocationManager mLocationManager;
    private Location mLocation;
    private Gson mGson;

    private int mChoiceCount = 0;

    private Shop mChoiceShop;
    private Fragment mSettingFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init GSON - JSON interpreter
        mGson = new Gson();
        mLocationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.make_choice, container, false);
        mTrashBtn = (CircleButton) view.findViewById(R.id.trash);
        mChocieBtn = (CircleButton) view.findViewById(R.id.make_choice);
        mFirstBtn = (CircleButton) view.findViewById(R.id.first_click);
        mSettingBtn = (ImageButton) view.findViewById(R.id.setting);
        mLatterEmpty = (ImageView) view.findViewById(R.id.latter_empty);
        mShopName = (TextView) view.findViewById(R.id.shop_name);
        mShopPrice = (TextView) view.findViewById(R.id.shop_price);
        mShopDistance = (TextView) view.findViewById(R.id.shop_distance);
        mDealListTitle = (TextView) view.findViewById(R.id.deals_list_title);
        mDealsListLayout = (RelativeLayout) view.findViewById(R.id.deals_list);
        mShopDetailsLayout = (LinearLayout) view.findViewById(R.id.shop_details);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mChocieBtn.setOnClickListener(this);
        mFirstBtn.setOnClickListener(this);
        mSettingBtn.setOnClickListener(this);
        mTrashBtn.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        String gpsLocationProvider = LocationManager.GPS_PROVIDER;
        String networkLocationProvider = LocationManager.NETWORK_PROVIDER;
        mLocationManager.requestLocationUpdates(gpsLocationProvider, 1000, 0, this);
        mLocation = mLocationManager.getLastKnownLocation(gpsLocationProvider);
        if (mLocation == null) {
            mLocation = mLocationManager.getLastKnownLocation(networkLocationProvider);
        }
    }

    @Override
    public void onStop() {
        mLocationManager.removeUpdates(this);
        saveData();
        super.onStop();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_CHOICE_FINISH: {
                    mShopName.setText(mChoiceShop.getName());
                    mShopPrice.setText(String.format(getString(R.string.shop_avg_price),
                            Integer.toString(mChoiceShop.getAvg_price())));
                    mShopDistance.setText(String.format(getString(R.string.shop_distance),
                            Float.toString(mChoiceShop.getDistance())));
                    if (mChoiceShop.getHas_deal() == 1) {
                        mDealListTitle.setText(
                                String.format(getString(R.string.shop_deals_item_title),
                                        Integer.toString(mChoiceShop.getDeal_count())));
                    } else {
                        mDealListTitle.setText(getString(R.string.shop_deals_item_title_empty));
                    }
                    if (!mDealsListLayout.isShown() || mShopDetailsLayout.isShown()) {
                        mDealsListLayout.setVisibility(View.VISIBLE);
                        mShopDetailsLayout.setVisibility(View.VISIBLE);
                    }
                    if (mFirstBtn.isShown() || mLatterEmpty.isShown()) {
                        mFirstBtn.setVisibility(View.GONE);
                        mLatterEmpty.setVisibility(View.GONE);
                    }
                    break;
                }
                case Constants.MSG_NO_SUITABLE_SHOP: {
                    showToast("没有合适的商家了");
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void showToast(CharSequence msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void saveData(){

    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.make_choice: {
                makeChoice();
                break;
            }
            case R.id.trash:
                UnExpShop unExpShop = new UnExpShop();
                unExpShop.setBusinessId(mChoiceShop.getBusiness_id());
                unExpShop.setShopName(mChoiceShop.getName());
                unExpShop.setAddDate(System.currentTimeMillis());
                unExpShop.setIsExcluded(1);
                FoodHelper.getUnexpectedShopList().add(unExpShop);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ContentValues values = new ContentValues();
                        values.put(FoodDatabase.UnexpectedShop.BUSINESS_ID, mChoiceShop.getBusiness_id());
                        values.put(FoodDatabase.UnexpectedShop.SHOP_NAME, mChoiceShop.getName());
                        values.put(FoodDatabase.UnexpectedShop.IS_EXCLUDED, 1);
                        values.put(FoodDatabase.UnexpectedShop.ADD_DATE, System.currentTimeMillis());
                        getActivity().getContentResolver().insert(FoodDatabase.UnexpectedShop.URI_UNEXPECTED_TABLE, values);
                    }
                }).run();
                showToast("已打入万劫可复之地");
                makeChoice();
                break;

            case R.id.first_click: {
                mChoiceCount++;
                showToast(getString(R.string.toast_first));
                searchNearShop();
                break;
            }
            case R.id.setting:
                startActivity((new Intent()).setClass(getActivity(), SettingActivity.class));
                break;
        }
    }

    private void makeChoice(){
        Message msg = new Message();
        mChoiceCount++;
        switch (mChoiceCount) {
            case 2:
                showToast(getString(R.string.toast_secondary));
                break;
            case 3:
                showToast(getString(R.string.toast_third));
                break;
            default:
                showToast(getString(R.string.toast_more));
                return;
        }

        if (FoodHelper.getShopList() != null && !FoodHelper.getShopList().isEmpty()) {
            mChoiceShop = getChoiceShop(FoodHelper.getShopList());
            if(mChoiceShop == null) {
                msg.what = Constants.MSG_NO_SUITABLE_SHOP;
                mHandler.sendMessage(msg);
                return;
            }
            msg.what = Constants.MSG_CHOICE_FINISH;
            mHandler.sendMessage(msg);
        }
    }

    private void searchNearShop() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("latitude", Double.toString(mLocation.getLatitude()));
        map.put("longitude", Double.toString(mLocation.getLongitude()));
        map.put("category", "美食");
        map.put("radius", "1000");
        map.put("sort", "7");
        map.put("limit", "40");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                NearShopResponse nearShopResponse = mGson.fromJson(
                        DianPingHandler.searchNearShop(map), NearShopResponse.class);
                FoodHelper.setShopList(nearShopResponse.getBusinesses());
                mChoiceShop = getChoiceShop(FoodHelper.getShopList());
                if (mChoiceShop == null) {
                    msg.what = Constants.MSG_NO_SUITABLE_SHOP;
                    mHandler.sendMessage(msg);
                    return;
                }
                msg.what = Constants.MSG_CHOICE_FINISH;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private Shop getChoiceShop(List<Shop> shopList) {
        Shop choiceShop;
        for (Shop aShopList : shopList) {
            choiceShop = shopList.get((new Random()).nextInt(FoodHelper.getShopList().size()));
            if (!FoodHelper.getUnexpectedShopList().contains(choiceShop.getBusiness_id())) {
                return choiceShop;
            }
        }
        return null;
    }

}
