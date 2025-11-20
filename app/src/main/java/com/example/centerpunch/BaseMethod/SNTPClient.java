package com.example.centerpunch.BaseMethod;


import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SNTPClient {

    private static final String TAG = "SNTPClient";
    private static final String NTP_HOST = "time.google.com";

    public interface Listener {
        void onTimeResponse(String rawDate, Date date, Exception ex);
    }

    public static void getDate(TimeZone timeZone, Listener listener) {
        new AsyncTask<Void, Void, Date>() {
            private Exception error;

            @Override
            protected Date doInBackground(Void... voids) {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    InetAddress address = InetAddress.getByName(NTP_HOST);

                    byte[] buffer = new byte[48];
                    buffer[0] = 0b00100011;

                    DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, 123);
                    socket.send(request);

                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                    socket.setSoTimeout(3000);
                    socket.receive(response);

                    socket.close();

                    long transmitTime = ((buffer[40] & 0xFFL) << 24)
                            | ((buffer[41] & 0xFFL) << 16)
                            | ((buffer[42] & 0xFFL) << 8)
                            | (buffer[43] & 0xFFL);

                    long secondsSince1900 = transmitTime - 2208988800L;
                    long timeMillis = secondsSince1900 * 1000L;

                    return new Date(timeMillis);

                } catch (Exception e) {
                    error = e;
                    Log.e(TAG, "NTP time fetch failed", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Date date) {
                if (listener != null) {
                    if (date != null) {
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                        format.setTimeZone(timeZone);
                        listener.onTimeResponse(format.format(date), date, null);
                    } else {
                        listener.onTimeResponse(null, null, error);
                    }
                }
            }
        }.execute();
    }
}

