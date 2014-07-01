package edu.cicese.anxilogger.api;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by panzer on 28/06/14.
 */
public class AnxiApi {
    private static  String BASE_URL = "http://158.97.89.138:5000";
    private static  String POST_IRDATA_URL = BASE_URL + "/api/loggerdata";


    public void postDataToServer(String value){
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(POST_IRDATA_URL);
        HttpResponse response;
        try{
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            nameValuePairs.add(new BasicNameValuePair("value", value));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = client.execute(post);
            //Log.i("AnxiLogger", response.toString());
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
