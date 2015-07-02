package com.philipp.scheid.psim;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity {

    Context mac;
    LinearLayout ll;
    ScrollView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.ll_1).getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        setTitle("blackhearted");
        android.support.v7.app.ActionBar ab = getSupportActionBar();

        ab.setSubtitle("87.78.178.236");

        mac = this;
        ll = (LinearLayout) findViewById(R.id.main_sv_ll);
        sv = (ScrollView) findViewById(R.id.main_sv_1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void establish(View v) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        EditText et = (EditText) findViewById(R.id.main_et1);
        TextView tv = (TextView) findViewById(R.id.main_tv1);

        tv.setText("");

        String ip = et.getText().toString();

        if (ni != null && ni.isConnected()) {
            try {
                new ConnectionTask().execute(ip);
            } catch (Exception e) {
                Log.d("Error:establish", e.getMessage());
            }
        } else {
            tv.setText("No network connection available.");
        }
    }

    public void addTextView(String message) {
        TextView addToSV = new TextView(mac);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.RIGHT;
        lp.topMargin = 5;
        lp.bottomMargin = 5;
        addToSV.setLayoutParams(lp);
        //addToSV.setMaxWidth((addToSV.getRootView().getWidth() / 3) * 2);
        addToSV.setBackgroundDrawable(getResources().getDrawable(R.drawable.cell_shape));
        addToSV.setPadding(8, 5, 5, 5);
        addToSV.setSingleLine(false);

        Calendar cal = Calendar.getInstance();
        cal.getTime();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String time = "";

        if (hour < 9 && minute < 9)
            time = "0" + hour + ":" + "0" + minute;
        else if (hour < 9 && minute > 9)
            time = "0" + hour + ":" + "" + minute;
        else if (hour > 9 && minute < 9)
            time = "" + hour + ":" + "0" + minute;
        else if (hour > 9 && minute > 9)
            time = "" + hour + ":" + "" + minute;

        SpannableString ss = new SpannableString(message + "\n" + time);
        ss.setSpan(new RelativeSizeSpan(1.2f), 0, message.length(), 0);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, message.length(), 0);
        ss.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), message.length(), message.length() + time.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        addToSV.setText(ss);
        ll.addView(addToSV);
        sv.post(new Runnable() {
            @Override
            public void run() {
                sv.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private class ConnectionTask extends AsyncTask<String, Void, Void> {
        Exception e;
        ProgressBar pb = (ProgressBar) findViewById(R.id.main_progressBar);
        EditText et2 = (EditText) findViewById(R.id.main_et2);
        TextView tv = (TextView) findViewById(R.id.main_tv1);

        @Override
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(params[0], 4444), 3000);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Recipient:" + params[0]);
                out.println("Message:" + et2.getText().toString());

                out.close();
                socket.close();
            } catch (Exception e) {
                this.e = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            pb.setVisibility(View.GONE);

            if(!(e.equals(null))) {
                if(e.getMessage().startsWith("failed to connect to")) {
                    tv.setText("Server currently offline.");
                } else {
                    tv.setText(e.getMessage());
                }
            } else {
                addTextView(et2.getText().toString());
                // Line below is changed for debugging, is supposed to be: et2.setText("");
                et2.setText(e.getMessage());
            }
        }
    }
}
