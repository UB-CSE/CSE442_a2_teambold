package com.teambald.cse442_project_team_bald.Fragments;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.TabsController.RecordingListAdapter;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class CloudFragment extends Fragment {
    private ArrayList<RecordingItem> cloudList = new ArrayList<>();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static int mColumnCount = 1;
    // TODO: Customize parameters
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private RecyclerView.Adapter mAdapter;

    /*
    private Button down;
    private Button up;
    private Button list;
    */
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private static final String TAG = "CloudFrag";

    public CloudFragment() {
    }

    public static CloudFragment newInstance() {
        return new CloudFragment();
    }
    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CloudFragment newInstance(int columnCount) {
        CloudFragment fragment = new CloudFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cloud_list, container, false);
        /*View view = inflater.inflate(R.layout.dummy_layout_cloud, container, false);

        up = view.findViewById(R.id.uploadbu);
        down = view.findViewById(R.id.downbu);
        list = view.findViewById(R.id.listbu);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
                uploadFile(path,"Recording_2020_10_21_06_34_00","mp4","baicheng");
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
                downloadFile(path,"testRec1","mp4","baicheng");
            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFiles("baicheng");
            }
        });*/
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview_cloud);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        //ArrayList<String> filenames =
        //Log.d(TAG,filenames.toString());
        //Log.d(TAG,filenames.size()+"");
        updateAllFiles();
        mAdapter = new RecordingListAdapter(cloudList,getContext());
        recyclerView.setAdapter(mAdapter);
    }
    public ArrayList<String> listFiles(String fireBaseFolder)
    {
        StorageReference listRef = storageRef.child(fireBaseFolder);
        final ArrayList<String> filenames = new ArrayList<String>();
        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference item : listResult.getItems()) {
                            // All the items under listRef.
                            Log.d(TAG,item.getName());
                            filenames.add(item.getName());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        Log.d(TAG,"File list error");
                    }
                });
        return filenames;
    }
    public void uploadFile(String path,String filenamePref,String filenameSuf,String fireBaseFolder)
    {
        if(null!=path && null!=filenamePref && null!=filenameSuf && null!=fireBaseFolder)
        {
            final String fullPath = path + "/" + filenamePref + "." + filenameSuf;
            final String fullFBPath = fireBaseFolder + "/" + filenamePref + "." + filenameSuf;

            Uri file = Uri.fromFile(new File(fullPath));
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filenamePref + "." + filenameSuf);
            storageReference.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Log.d(TAG, "File upload successful");
                            Log.d(TAG, "From:" + fullPath);
                            Log.d(TAG, "To:" + fullFBPath);
                            Toast.makeText(getContext(),"File upload Successful", Toast.LENGTH_LONG);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Log.d(TAG, "File upload unsuccessful");
                            Log.d(TAG, "From:" + fullPath);
                            Log.d(TAG, "To:" + fullFBPath);
                            Toast.makeText(getContext(),"File upload Unsuccessful", Toast.LENGTH_LONG);
                        }
                    });
        }
        else
        {
            return;
        }
    }
    public boolean downloadFile(String path,String filenamePref,String filenameSuf,String fireBaseFolder)
    {
        try
        {
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filenamePref + "." + filenameSuf);
            final String fullPath = path + "/" + filenamePref + "." + filenameSuf;
            File tempFile = new File(fullPath);
            storageReference.getFile(tempFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        Log.d(TAG,"File download Successful");
                        Toast.makeText(getContext(),"File download Successful", Toast.LENGTH_LONG);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle failed download
                        // ...
                        Log.d(TAG,"Read file error on Failure");
                        Toast.makeText(getContext(),"File download Unsuccessful", Toast.LENGTH_LONG);
                    }
            });
        }
        catch (Exception e)
        {
            Log.d(TAG,"Read file error");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(storageRef!=null) {
            Log.d(TAG,"OnResume: ");
            listFiles("baicheng");
            if(mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(storageRef!=null){
            Log.d(TAG,"OnAttach: ");
            listFiles("baicheng");
            if(mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    public void updateAllFiles()
    {
        ArrayList<String> files = listFiles("baicheng");
        cloudList.clear();
        for (String filename : files)
        {
            File f = new File(filename);
            Uri uri = Uri.parse(f.getAbsolutePath());
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getContext(), uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int seconds = Integer.parseInt(durationStr) / 1000;
            durationStr = parseSeconds(seconds);
            cloudList.add(new RecordingItem(f.getName(), durationStr , f.getPath(), true, f));
        }
        Log.d(TAG,"There are "+cloudList.size()+" items in cloud list");
    }
    public String parseSeconds(int seconds) {
        int min = seconds / 60;
        seconds-=(min * 60);
        return (min < 10 ? "0" + min : String.valueOf(min)) + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
    }
}