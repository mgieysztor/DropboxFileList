package com.sdacademy.gieysztor.michal.dropboxfilelist;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by RENT on 2017-02-25.
 */


public class GetFilesListTask extends AsyncTask<String, Integer, GetFilesListTask.GetFilesListResult> {

    MainActivity mainActivity;

    String path;

    public GetFilesListTask(String path) {
        this.path = path;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected GetFilesListResult doInBackground(String... params) {
        GetFilesListResult result = new GetFilesListResult();
        try {
            Response response = sendRequest();

            int statusCode = response.code();
            if (statusCode >= 200 && statusCode <= 299) {
                String stringResponse = response.body().string();

                JSONObject jsonObject = new JSONObject(stringResponse);
                result.setJsonObject(jsonObject);
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
                result.setErrorMessage(response.body().string());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            result.setErrorMessage("Błąd JSON");
            result.setSuccess(false);
        } catch (IOException e) {
            e.printStackTrace();
            result.setErrorMessage("Błąd sieci");
            result.setSuccess(false);
        }


        return result;
    }

    @Override
    protected void onPostExecute(GetFilesListResult result) {
        super.onPostExecute(result);

        if (result.isSuccess()) {
            JSONArray jsonArray = result.getJsonObject().optJSONArray("entries");
            List<DropboxFile> dropboxFiles = convert(jsonArray);

            if (mainActivity != null) {
                mainActivity.setFiles(dropboxFiles);
            }

        } else {
            if (mainActivity != null) {
                mainActivity.showToast(result.getErrorMessage());
            }
        }
    }

    private List<DropboxFile> convert(JSONArray array) {
        ArrayList<DropboxFile> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.optJSONObject(i);
            DropboxFile dropboxFile = new DropboxFile();
            dropboxFile.setName(jsonObject.optString("name"));
            dropboxFile.setPath(jsonObject.optString("path_lower"));
            dropboxFile.setTag(jsonObject.optString(".tag"));
            dropboxFile.setId(jsonObject.optString("id"));
            list.add(dropboxFile);
        }
        return list;
    }

//    @Override
//    protected void onPostExecute(JSONObject jsonObject) {
//        super.onPostExecute(jsonObject);
//
//        Log.i("Dropbox", jsonObject.toString());
//
//        JSONArray jsonArray = jsonObject.optJSONArray("entries");
//        List<DropboxFile> dropboxFiles = convert(jsonArray);
//
//        if (mainActivity !=null){
//            mainActivity.setFiles(dropboxFiles);
//        }
//
//    }

    private Response sendRequest() throws JSONException, IOException {

        MediaType mediaType = MediaType.parse("application/json");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("path", path);

        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request.Builder builder = new Request.Builder();
        builder.url("https://api.dropboxapi.com/2/files/list_folder");
        builder.addHeader("Authorization", "Bearer dDMgJ7xRgDAAAAAAAAAAFET3EXRWyKOLnM_FxIYDROmR2OiSwFBdeqLITT0YkYzG");
        builder.addHeader("Content-Type", "application/json");
        builder.post(requestBody);

        Request request = builder.build();

        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        return call.execute();


    }

    public static class GetFilesListResult {

        private JSONObject jsonObject;

        private boolean isSuccess;

        private String errorMessage;

        public JSONObject getJsonObject() {
            return jsonObject;
        }

        public void setJsonObject(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}

