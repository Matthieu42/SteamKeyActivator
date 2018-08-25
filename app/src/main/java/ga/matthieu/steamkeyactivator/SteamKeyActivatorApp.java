package ga.matthieu.steamkeyactivator;

import android.app.Application;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;

public class SteamKeyActivatorApp extends Application {

    public SteamClient steamClient = new SteamClient();
    public CallbackManager manager = new CallbackManager(steamClient);
    public SteamUser steamUser = steamClient.getHandler(SteamUser.class);


    public boolean isLogged;

    public SteamKeyActivatorApp getApplication(){
        return this;
    }
}
