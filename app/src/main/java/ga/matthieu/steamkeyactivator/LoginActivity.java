package ga.matthieu.steamkeyactivator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

public class LoginActivity extends AppCompatActivity {

    private SteamKeyActivatorApp app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        app = (SteamKeyActivatorApp) getApplication();
    }

    public void loginClick(View v){
        String username = ((EditText) findViewById(R.id.UsernameEditText)).getText().toString();
        String pass = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
        LogManager.addListener(new DefaultLogListener());
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Thread thread = new Thread(new SteamLogin(username,pass,app,sharedPref,fragmentManager));
        thread.start();
    }

}
