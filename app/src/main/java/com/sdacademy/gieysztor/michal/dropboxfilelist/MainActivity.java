package com.sdacademy.gieysztor.michal.dropboxfilelist;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;

    private ArrayAdapter<DropboxFile> listAdapter;

    private GetFilesListTask getFilesListTask;

    private DownloadFileTask downLoadFileTask;

    public List<String> pendingPathList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        listView.setAdapter(listAdapter);

//        pendingPathList = new ArrayList<>();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DropboxFile file = (DropboxFile) parent.getItemAtPosition(position);
//                Toast.makeText(MainActivity.this, file.getName(), Toast.LENGTH_SHORT).show();

                String tag = file.getTag();
                if (file.getTag().equals("folder")) {
                    listAdapter.clear();
                    pendingPathList.add(file.getPath());
                    getFiles(file.getPath());
                } else if (file.getTag().equals("file")){
                    downLoadFileTask = new DownloadFileTask(MainActivity.this);
                    downLoadFileTask.execute(file);
                    Log.i("FileClicked",file.getName().toString());
                }
//                getFiles("");
            }
        });
        getFiles("");

    }

    private void getFiles(String path) {
        getFilesListTask = new GetFilesListTask(path);
        getFilesListTask.setMainActivity(this);
        getFilesListTask.execute();
    }

    @Override
    public void onBackPressed() {
//        for (int i = 0; i < pendingPathList.size(); i++) {
//            Log.i("PATH", pendingPathList.get(i).toString());
//        }
        int listSize = pendingPathList.size();
        if (!pendingPathList.get(listSize - 1).equals("")) {
            listAdapter.clear();
            if (listSize > 1) {
                getFiles(pendingPathList.get(listSize - 2));
                pendingPathList.remove(listSize - 1);
            } else if (listSize == 1) {
                getFiles("");
                pendingPathList.remove(listSize - 1);

            }
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (getFilesListTask != null) {

            getFilesListTask.setMainActivity(null);
        }
    }

    public void setFiles(List<DropboxFile> files) {
        listAdapter.addAll(files);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT);

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    public void showError(String errorMessage) {
//        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

        new AlertDialog.Builder(this)
                .setMessage(errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }
}
