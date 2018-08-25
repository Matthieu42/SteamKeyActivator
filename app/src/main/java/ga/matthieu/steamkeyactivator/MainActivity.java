package ga.matthieu.steamkeyactivator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private SteamKeyActivatorApp app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = (SteamKeyActivatorApp) getApplication();
        if(!app.isLogged){
            this.startActivity( new Intent(this, LoginActivity.class));
        }
    }
}
