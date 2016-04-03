package com.thenewboston.backendless_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.BackendlessCallback;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    String riderOrDriver = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String appVersion = "v1";
        Backendless.initApp(this, "B10F86F3-BE75-946B-FFEB-C1AEA20D8100", "750EAE5F-7614-53B3-FF12-308A2C33C700", appVersion);

//        BackendlessUser user = new BackendlessUser();
//        user.setEmail("tarunpatra53@gmail.com");
//        user.setPassword("driver");
//        Backendless.UserService.register(user, new BackendlessCallback<BackendlessUser>() {
//            @Override
//            public void handleResponse(BackendlessUser backendlessUser) {
//                Log.i("Success: ", backendlessUser.getEmail());
//            }
//        });
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();
        Switch choice = (Switch)findViewById(R.id.choice);
        choice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    riderOrDriver = "Driver";
                }else {
                    riderOrDriver = "Rider";
                }
            }
        });
        if(Backendless.UserService.CurrentUser() == null){
            Log.i("Login saving", "Failed");
        }else{
            if(Backendless.UserService.CurrentUser().getProperty("Type").equals("Rider")){
                Intent i = new Intent(this, YourLocation.class);
                startActivity(i);

            }
        }

    }

    public void start(View view){
        Intent i = new Intent(this, Registration.class);
        i.putExtra("riderOrDriver", riderOrDriver);
        startActivity(i);
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
}
