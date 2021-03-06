package com.app.tnevi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.tnevi.model.Epointsmodel;
import com.app.tnevi.Adapters.EpointsAdapter;
import com.app.tnevi.Allurl.Allurl;

import com.app.tnevi.internet.CheckConnectivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Epointhistory extends AppCompatActivity {

    LinearLayout btnBuyepoints;
    ImageView btn_back;
    TextView tvshowText;
    RecyclerView rv_epoints;
    private static final String TAG = "myapp";
    private static final String SHARED_PREFS = "sharedPrefs";
    String token;
    EpointsAdapter epointsAdapter;
    private ArrayList<Epointsmodel> epointsmodelArrayList;
    TextView tvTotalepoints;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epointhistory);
        btnBuyepoints = findViewById(R.id.btnBuyepoints);
        btn_back = findViewById(R.id.btn_back);
        rv_epoints = findViewById(R.id.rv_epoints);
        tvTotalepoints = findViewById(R.id.tvTotalepoints);
        tvshowText = findViewById(R.id.tvshowText);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });

        btnBuyepoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(Epointhistory.this, Buyepoint.class);
                startActivity(intent);
            }
        });

        getAccount();

    }


    public void getAccount() {

        if (CheckConnectivity.getInstance(getApplicationContext()).isOnline()) {

            showProgressDialog();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Allurl.GetAccount,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("Response3-->", String.valueOf(response));
                            try {
                                JSONObject result = new JSONObject(String.valueOf(response));
                                String msg = result.getString("message");
                                Log.d(TAG, "msg-->" + msg);
                                String stat = result.getString("stat");
                                if (stat.equals("succ")) {

                                    JSONObject userdeatisObj = result.getJSONObject("data");
                                    JSONObject profileObj = userdeatisObj.getJSONObject("profile");
                                    JSONObject walletObj = userdeatisObj.getJSONObject("wallet");
                                    String ticketsell = walletObj.getString("ticket_sell");
                                    String commission = walletObj.getString("commission");
                                    String epoints = walletObj.getString("epoints");
                                    String address = profileObj.getString("address");
                                    String country_code = profileObj.getString("country_code");
                                    tvTotalepoints.setText(epoints);

                                } else {
                                    hideProgressDialog();
                                    Log.d(TAG, "unsuccessfull - " + "Error");
                                    Toast.makeText(Epointhistory.this, msg, Toast.LENGTH_SHORT).show();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            viewepointslist();



//                            hideProgressDialog();


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            hideProgressDialog();
                            Toast.makeText(Epointhistory.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", token);
                    return params;
                }

            };


            RequestQueue requestQueue = Volley.newRequestQueue(Epointhistory.this);
            requestQueue.add(stringRequest);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    9000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        } else {

            Toast.makeText(getApplicationContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show();

        }


    }


    public void viewepointslist(){

        if (CheckConnectivity.getInstance(getApplicationContext()).isOnline()) {


            showProgressDialog();

            JSONObject params = new JSONObject();

            try {
                params.put("page_no", "1");

            } catch (JSONException e) {
                e.printStackTrace();
            }


            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Allurl.ViewRewards, params, response -> {

                Log.i("Response-->", String.valueOf(response));

                try {
                    JSONObject result = new JSONObject(String.valueOf(response));
                    String msg = result.getString("message");
                    Log.d(TAG, "msg-->" + msg);
                    String stat = result.getString("stat");
                    if (stat.equals("succ")) {

                        epointsmodelArrayList = new ArrayList<>();
                        JSONArray response_data = result.getJSONArray("data");
                        for (int i = 0; i < response_data.length(); i++) {

                            Epointsmodel epointsmodel = new Epointsmodel();
                            JSONObject responseobj = response_data.getJSONObject(i);
                            epointsmodel.setCreated_at(responseobj.getString("created_at"));
                            epointsmodel.setEpoints(responseobj.getString("epoints"));
                            epointsmodel.setReward_description(responseobj.getString("reward_description"));
                            epointsmodelArrayList.add(epointsmodel);

                        }
                        if (response_data.length()==0){

                            tvshowText.setVisibility(View.VISIBLE);

                        }else {

                            tvshowText.setVisibility(View.GONE);
                        }

                        setupRecycler();

                    } else {

                        Toast.makeText(Epointhistory.this, "invalid", Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                hideProgressDialog();

                //TODO: handle success
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(Epointhistory.this, error.toString(), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", token);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(jsonRequest);

        } else {

            Toast.makeText(getApplicationContext(), "Ooops! Internet Connection Error", Toast.LENGTH_SHORT).show();

        }

    }


    private void setupRecycler() {

        epointsAdapter = new EpointsAdapter(this, epointsmodelArrayList);
        rv_epoints.setAdapter(epointsAdapter);
        rv_epoints.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

    }


    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}