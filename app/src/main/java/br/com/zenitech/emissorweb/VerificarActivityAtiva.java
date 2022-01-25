package br.com.zenitech.emissorweb;

/**
 * Created by Kleilson Sousa 28/5/18.
 */

public class VerificarActivityAtiva {

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;
}
