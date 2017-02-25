package com.sdacademy.gieysztor.michal.dropboxfilelist;

/**
 * Created by RENT on 2017-02-25.
 */


import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jstefano on 25.02.2017.
 */
public class DownloadFileTask extends AsyncTask<DropboxFile, String, DownloadFileTask.DownloadResult> {

    private MainActivity mainActivity;

    public DownloadFileTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected DownloadResult doInBackground(DropboxFile... params) {

        DownloadResult result = new DownloadResult();

        try {
            DropboxFile dropboxFile = params[0];
            String fileName = dropboxFile.getName();
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            String fileExtension = fileName.substring(fileName.indexOf('.') + 1);

            Response response = sendRequest(dropboxFile.getId());
            int responseCode = response.code();

            if (responseCode >= 200 && responseCode <= 299) {
                InputStream is = response.body().byteStream();

                BufferedInputStream input = new BufferedInputStream(is);
                OutputStream output = new FileOutputStream(file);

                byte[] data = new byte[1024];

                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                result.setSuccess(true);
                result.setTargetFile(file);
                result.setContentLength(total);

                MimeTypeMap mtm = MimeTypeMap.getSingleton();
                result.setContentType(mtm.getMimeTypeFromExtension(fileExtension));
            } else {
                result.setSuccess(false);
                result.setErrorMessage(response.body().string());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            result.setErrorMessage("Błąd JSONa");
            result.setSuccess(false);
        } catch (IOException e) {
            e.printStackTrace();
            result.setErrorMessage("Błąd sieci");
            result.setSuccess(false);
        }


        return result;
    }

    @Override
    protected void onPostExecute(DownloadResult response) {
        super.onPostExecute(response);

        if (!response.isSuccess()) {
            mainActivity.showError(response.getErrorMessage());
        } else {
            File file = response.getTargetFile();
            DownloadManager dm = (DownloadManager) mainActivity.getSystemService(Context.DOWNLOAD_SERVICE);
            dm.addCompletedDownload(file.getName(), "Dropbox file", true, response.getContentType(),
                    file.getAbsolutePath(), response.getContentLength(), true);
            mainActivity.showToast("Plik pobrany");
        }
    }

    private Response sendRequest(String path) throws JSONException, IOException {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        json.put("path", path);

        RequestBody requestBody = RequestBody.create(null, "");
        Request.Builder builder = new Request.Builder();
        builder.url("https://content.dropboxapi.com/2/files/download");
        builder.addHeader("Authorization", "Bearer QU5gDEC7TYAAAAAAAAAAEWR4ouLrr400-dw7cw0zzWVfd0nXre4GXGARDyCctNJE");
        builder.addHeader("Dropbox-API-Arg", json.toString());
        builder.post(requestBody);

        Request request = builder.build();
        Call call = client.newCall(request);
        return call.execute();
    }

    public static class DownloadResult {

        private boolean isSuccess;

        private File targetFile;

        private String contentType;

        private long contentLength;

        private String errorMessage;

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

        public File getTargetFile() {
            return targetFile;
        }

        public void setTargetFile(File targetFile) {
            this.targetFile = targetFile;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public long getContentLength() {
            return contentLength;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }
    }
}

