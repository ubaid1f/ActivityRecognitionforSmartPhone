package com.example.euguzns.arforphone;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class HttpRequestClient extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params){
            OutputStream outputStream = null;
            byte[] outputBytes = null;
            HttpURLConnection httpConnection = null;
            StringBuilder stringbuild = new StringBuilder();
            try {
                System.out.println("Get Request Send!");
                httpConnection = (HttpURLConnection) ((new URL(params[0]).openConnection()));
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
                System.out.println("httpconnection.getresponse"+httpConnection.getResponseCode());
                int status = httpConnection.getResponseCode();

                if (status == HttpsURLConnection.HTTP_OK)
                {
                    System.out.println("Here it is!");
                    InputStream inputStream = httpConnection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    while((line=br.readLine())!=null){
                        System.out.println("line"+line);
                        stringbuild.append(line);
                    }


                }
                else
                {
                    System.out.println("Could not Connect to Server!");

                }

            } catch (MalformedURLException ex) {
                System.err.println("MalformedURLException");
                System.err.println(ex.getMessage());
            } catch (ProtocolException ex) {
                System.err.println("ProtocolException");
                System.err.println(ex.getMessage());
            } catch (UnsupportedEncodingException ex) {
                System.err.println("UnsupportedEncodingException");
                System.err.println(ex.getMessage());
            } catch (IOException ex) {
                System.err.println("IOException");
                System.err.println(ex.getMessage());
            }

            System.out.println("Async Request Sent");
            System.out.println("StringBuild: "+stringbuild.toString());
            return stringbuild.toString();
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
        }
    }


