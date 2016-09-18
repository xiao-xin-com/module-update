package com.xiaoxin.update.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXUpdateThread extends Thread {
    private String updateUrl;

    public XXUpdateThread(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    @Override
    public void run() {
        super.run();
        try {
            URL url = new URL(updateUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                String result = inputStream2String(inputStream);
                interpretingData(result);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void interpretingData(String result) {

    }

    public static String inputStream2String(InputStream in) {
        BufferedWriter bw = null;
        BufferedReader br = null;
        try {
            StringWriter stringWriter = new StringWriter();
            bw = new BufferedWriter(stringWriter);
            br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while (br.readLine() != null) {
                bw.append(line);
                bw.newLine();
                bw.flush();
            }
            return stringWriter.toString();
        } catch (IOException e) {
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
