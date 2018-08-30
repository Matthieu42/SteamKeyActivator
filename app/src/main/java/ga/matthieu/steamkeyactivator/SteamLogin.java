package ga.matthieu.steamkeyactivator;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.MachineAuthDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.OTPDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoginKeyCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.UpdateMachineAuthCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SteamLogin implements Runnable, OTPDialogListener {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private String user;

    private String pass;

    private SteamKeyActivatorApp app;
    private SharedPreferences preferences;
    private FragmentManager fragmentManager;
    private Activity loginActivity;
    private String authCode;

    private String twoFactorAuth;

    public SteamLogin(String user, String pass, SteamKeyActivatorApp app, SharedPreferences preferences, FragmentManager fragmentManager) {
        this.user = user;
        this.pass = pass;
        this.app = app;
        this.preferences = preferences;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void run() {

        // create our steamclient instance
        steamClient = new SteamClient();
        app.steamClient = steamClient;

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        manager.subscribe(UpdateMachineAuthCallback.class, this::onMachineAuth);
        manager.subscribe(LoginKeyCallback.class, this::onLoginKey);

        isRunning = true;

        System.out.println("Connecting to steam...");

        // initiate the connection
        steamClient.connect();

        // create our callback handling loop
        while (isRunning) {
            // in order for the callbacks to get routed, they need to be handled by the manager
            manager.runWaitCallbacks(1000L);
        }
    }

    private void onConnected(ConnectedCallback callback) {
        System.out.println("Connected to Steam! Logging in " + user + "...");

        LogOnDetails details = new LogOnDetails();
        details.setUsername(user);

        String loginkey = preferences.getString(app.getResources().getString(R.string.login_key), "");
        if (!loginkey.equals("")) {
                details.setLoginKey(loginkey);
        } else {
            details.setPassword(pass);
        }

        details.setTwoFactorCode(twoFactorAuth);
        details.setAuthCode(authCode);
        details.setShouldRememberPassword(true);

        steamUser.logOn(details);
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam, reconnecting in 5...");

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        steamClient.connect();
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        boolean isSteamGuard = callback.getResult() == EResult.AccountLogonDenied;
        boolean is2Fa = callback.getResult() == EResult.AccountLoginDeniedNeedTwoFactor;

        if (isSteamGuard || is2Fa) {
            System.out.println("This account is SteamGuard protected.");
            if (is2Fa) {
                System.out.print("Please enter your 2 factor auth code from your authenticator app: ");
                isRunning = false;
                openDialog();
               // twoFactorAuth = s.nextLine();
            } else {
                System.out.print("Please enter the auth code sent to the email at " + callback.getEmailDomain());
                isRunning = false;
                openDialog();
//                authCode = s.nextLine();
            }

            steamClient.disconnect();
            return;
        }
        if (callback.getResult() != EResult.OK) {
            System.out.println("Unable to logon to Steam: " + callback.getResult() + " / " + callback.getExtendedResult());
            isRunning = false;
            steamClient.disconnect();
            return;
        }

        System.out.println("Successfully logged on!");

        // at this point, we'd be able to perform actions on Steam
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());
        isRunning = false;
    }

    private void onMachineAuth(UpdateMachineAuthCallback callback) {
        File sentry = new File("sentry.bin");
        try (FileOutputStream fos = new FileOutputStream(sentry)) {
            FileChannel channel = fos.getChannel();
            channel.position(callback.getOffset());
            channel.write(ByteBuffer.wrap(callback.getData(), 0, callback.getBytesToWrite()));

            OTPDetails otpDetails = new OTPDetails();
            otpDetails.setIdentifier(callback.getOneTimePassword().getIdentifier());
            otpDetails.setType(callback.getOneTimePassword().getType());

            MachineAuthDetails details = new MachineAuthDetails();
            details.setJobID(callback.getJobID());
            details.setFileName(callback.getFileName());
            details.setBytesWritten(callback.getBytesToWrite());
            details.setFileSize((int) sentry.length());
            details.setOffset(callback.getOffset());
            details.seteResult(EResult.OK);
            details.setLastError(0);
            details.setOneTimePassword(otpDetails);
            details.setSentryFileHash(calculateSHA1(sentry));

            steamUser.sendMachineAuthResponse(details);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void onLoginKey(LoginKeyCallback callback) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(app.getString(R.string.login_key), callback.getLoginKey()).apply();
        steamUser.acceptNewLoginKey(callback);
    }

    private byte[] calculateSHA1(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        InputStream fis = new FileInputStream(file);
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        return digest.digest();
    }
    private void openDialog(){
        OTPDialog otpDialog = new OTPDialog();
        otpDialog.addListener(this);
        otpDialog.show(fragmentManager,"OTPDialog");
    }

    @Override
    public void applyKey(String authcode) {
        twoFactorAuth = authcode;
        this.authCode = authcode;
        onConnected(null);
    }
}
