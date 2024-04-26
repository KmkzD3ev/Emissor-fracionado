package br.com.zenitech.emissorweb.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionHelper {
    private static final String TAG = "ConnectionHelper";

    public interface ConnectionCallback {
        void onResult(boolean isConnected);
    }

    public static void checkInternetConnection(ConnectionCallback callback) {
        new InternetCheckTask(callback).execute();
    }

    private static class InternetCheckTask extends AsyncTask<Void, Void, Boolean> {
        private ConnectionCallback callback;

        InternetCheckTask(ConnectionCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                //URL url = new URL("https://www.google.com/");
                //URL url = new URL("https://emissorweb.com.br/");
                URL url = new URL("https://emissorfiscalweb.com.br/");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent", "Android");
                urlConnection.setRequestProperty("Connection", "close");
                urlConnection.setConnectTimeout(1500); // Timeout de conexão em milissegundos
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                Log.e(TAG, "Internet connection" + urlConnection.getResponseMessage());
                return (responseCode == 200);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            if (callback != null) {
                callback.onResult(isConnected);
            }
        }
    }
}
