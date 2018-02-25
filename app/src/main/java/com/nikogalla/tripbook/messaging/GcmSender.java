package com.nikogalla.tripbook.messaging;

/**
 * Created by nick on 17/02/18.
 */

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.google.firebase.iid.FirebaseInstanceId;
import com.nikogalla.tripbook.BuildConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// NOTE:
// This class emulates a server for the purposes of this sample,
// but it's not meant to serve as an example for a production app server.
// This class should also not be included in the client (Android) application
// since it includes the server's API key. For information on GCM server
// implementation see: https://developers.google.com/cloud-messaging/server
public class GcmSender {
    public static void send(final String message) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // Prepare JSON containing the GCM message content. What to send and where to send.
                    // Where to send GCM message.
                    // What to send in GCM message.
                    String token = FirebaseInstanceId.getInstance().getToken();
                    // Create connection to send GCM Message request.
                    String parameter = "?text="+message;
                    URL url = new URL(BuildConfig.MESSAGE_ENDPOINT+parameter);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Read GCM response.
                    InputStream inputStream = conn.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder resp = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        resp.append(line).append('\n');
                    }
                    System.out.println(resp);
                    System.out.println("Check your device/emulator for notification or logcat for " +
                            "confirmation of the receipt of the GCM message.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
