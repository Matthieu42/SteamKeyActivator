package ga.matthieu.steamkeyactivator;

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
        new SteamLogin(username,pass,app).run();
    }

}
