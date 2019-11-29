package com.example.fitapp;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

  //  private Button mButtonViewWeek;
  //  private Button mButtonViewToday;
  //  private Button mButtonAddSteps;
  //  private Button mButtonUpdateSteps;
  //  private Button mButtonDeleteSteps;
    TextView stepcount;
    TextView calcount,uname,email;
    CircleImageView propic;
    String step;



    private GoogleApiClient mGoogleApiClient;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .build();
    }

    private void initViews() {

        uname=findViewById(R.id.uname);
        propic=findViewById(R.id.propic);
        email=findViewById(R.id.email);

        stepcount=findViewById(R.id.stepcount);
        calcount=findViewById(R.id.calcount);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("uname");
            String mail = extras.getString("email");
            String pic = extras.getString("picurl");
            uname.setText(name);
            email.setText(mail);
            if(pic!=null) {
                Glide.with(this).load(pic).into(propic);
            }



            //The key argument here must match that used in the other activity
        }


    }


    //In use, call this every 30 seconds in active mode, 60 in ambient on watch faces
    private void displayStepDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.MINUTES);
        showTodayStep(result.getTotal());
    }
    private void displayweightDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mGoogleApiClient, DataType.TYPE_WEIGHT).await(1, TimeUnit.MILLISECONDS);
        showTodayStep(result.getTotal());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("HistoryAPI", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("HistoryAPI", "onConnectionFailed");
    }

    public void onConnected(@Nullable Bundle bundle) {
        Log.e("HistoryAPI", "onConnected");
        new FetchCalorieAsync().execute();
     // new Fetchweight().execute();
     //  new ViewTodaysweightCountTask().execute();
       new ViewTodaysStepCountTask().execute();



    }


  //  @Override
  /*  public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_view_week: {
                new ViewWeekStepCountTask().execute();
                break;
            }
            case R.id.btn_view_today: {
                new ViewTodaysStepCountTask().execute();
                break;
            }
            case R.id.btn_add_steps: {
                new AddStepsToGoogleFitTask().execute();
                break;
            }
        }
    }*/


    private class ViewWeekStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayLastWeeksData();
            return null;
        }
    }

    private class ViewTodaysStepCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayStepDataForToday();
            return null;
        }
    }

    private class ViewTodaysweightCountTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayweightDataForToday();
            return null;
        }
    }

    private void displayLastWeeksData() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime));
        Log.e("History", "Range End: " + dateFormat.format(endTime));

        //Check how many steps were walked and recorded in the last 7 days
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();


        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);


        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        }
        //Used for non-aggregated data
        else if (dataReadResult.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                showDataSet(dataSet);
            }
        }
    }
    private void showDataSet(DataSet dataSet) {
        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tType: " + dp.getDataType().getName());
            Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    private void showTodayStep(DataSet dataSet) {
        Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tType: " + dp.getDataType().getName());
            Log.e("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                step=dp.getValue(field).toString();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        stepcount.setText(step);

                    }
                });


            }

        }
    }


    private void addStepDataToGoogleFit() {
        //Adds steps spread out evenly from start time to end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName("Step Count")
                .setType(DataSource.TYPE_RAW)
                .build();

        int stepCountDelta = 1000;
        DataSet dataSet = DataSet.create(dataSource);

        DataPoint point = dataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        point.getValue(Field.FIELD_STEPS).setInt(stepCountDelta);
        dataSet.add(point);

        Status status = Fitness.HistoryApi.insertData(mGoogleApiClient, dataSet).await(1, TimeUnit.MINUTES);

        if (!status.isSuccess()) {
            Log.e( "History", "Problem with inserting data: " + status.getStatusMessage());
        } else {
            Log.e( "History", "data inserted" );
        }
    }

    private class AddStepsToGoogleFitTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            addStepDataToGoogleFit();
            displayLastWeeksData();
            return null;
        }
    }

    private class FetchCalorieAsync extends AsyncTask<Object, Object, Float> {
        protected Float doInBackground(Object... params) {
            Float total = 0.0f;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mGoogleApiClient, DataType. TYPE_CALORIES_EXPENDED);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    total = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                }
            } else {
                Log.w("FITAPP", "There was a problem getting the calories.");
            }
            return total;
        }


        @Override
        protected void onPostExecute(Float aLong) {
            super.onPostExecute(aLong);

            //Total calories burned for that day
            Log.i("FITAPP", "Total calories: " + aLong);
            Integer cal=aLong.intValue();
            calcount.setText(cal.toString()+" Kcal");

        }
    }

   /* private class HWFETCHER extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime));
        Log.e("History", "Range End: " + dateFormat.format(endTime));

        DataReadRequest readHWTRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_HEIGHT)
                .read(DataType.TYPE_WEIGHT)
                .setLimit(1)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
       al height = dataReadResponse?.dataSets.dataPoints[0].getValue(Field.FIELD_HEIGHT).asFloat())



    }*/

    private class Fetchweight extends AsyncTask<Object, Object, Float> {
        protected Float doInBackground(Object... params) {
            Float total = 85.0f;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(mGoogleApiClient, DataType. TYPE_HEIGHT);
            DailyTotalResult totalResult = result.await(1000, TimeUnit.MILLISECONDS);
            if (totalResult.getStatus().isSuccess()) {
                System.out.println("########1");
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    System.out.println("########22");
                    total = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_HEIGHT).asFloat();
                    System.out.println("########333");
                }
            } else {
                Log.w("FITAPP", "There was a problem getting the weight.");
            }
            return total;
        }

        @Override
        protected void onPostExecute(Float aLong) {
            super.onPostExecute(aLong);

            //Total calories burned for that day
            Log.i("FITAPP", "Weight: " + aLong);

        }
    }




}
