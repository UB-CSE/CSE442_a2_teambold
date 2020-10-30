package com.teambald.cse442_project_team_bald.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.TabsController.RecordingListAdapter;
import com.teambald.cse442_project_team_bald.TabsController.SwipeActionHandler;

import java.io.File;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class CloudFragment extends Fragment {
    private ArrayList<RecordingItem> cloudList = new ArrayList<>();
    private ArrayList<String> durations = new ArrayList<>();

    private static final String durationMetaDataConst = "Duration";
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static int mColumnCount = 1;
    // TODO: Customize parameters
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private RecyclerView.Adapter mAdapter;

    private MainActivity activity;
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
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean logedIn = sharedPref.getBoolean(SettingFragment.LogedInBl,false);

        if(logedIn) {
            RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview_cloud);
            recyclerView.setHasFixedSize(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
            recyclerView.setLayoutManager(layoutManager);
            //ArrayList<String> filenames =
            //Log.d(TAG,filenames.toString());
            //Log.d(TAG,filenames.size()+"");
            listFiles("baicheng");
            mAdapter = new RecordingListAdapter(cloudList, getContext());
            recyclerView.setAdapter(mAdapter);
            ItemTouchHelper itemTouchHelper = new
                    ItemTouchHelper(new SwipeActionHandler((RecordingListAdapter) mAdapter,1,this));
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
        else
        {
            RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview_cloud);
            recyclerView.setHasFixedSize(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
            recyclerView.setLayoutManager(layoutManager);
            //ArrayList<String> filenames =
            //Log.d(TAG,filenames.toString());
            //Log.d(TAG,filenames.size()+"");
            cloudList.clear();
            cloudList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
            mAdapter = new RecordingListAdapter(cloudList, getContext());
            recyclerView.setAdapter(mAdapter);
        }
    }
    public void listFiles(String fireBaseFolder)
    {
        GoogleSignInAccount gsi = GoogleSignIn.getLastSignedInAccount(getActivity());
        Log.d(TAG,gsi+"");
        if( gsi != null) {
            StorageReference listRef = storageRef.child(fireBaseFolder);
            CloudSuccListener rstListener = new CloudSuccListener(fireBaseFolder);
            listRef.listAll()
                    .addOnSuccessListener(rstListener)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Uh-oh, an error occurred!
                            Log.d(TAG,"File list error");
                        }
                    });
        }
        else
        {
            cloudList.clear();
            cloudList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
            if(mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    public void updateFiles(ArrayList<String> filenames)
    {
        cloudList.clear();
        for (int idx = 0;idx<filenames.size();idx++)
        {
            String filename = filenames.get(idx);
            cloudList.add(new RecordingItem(filename, "Duration: ", filename, true, new File(filename)));
        }
        RecyclerView recyclerView = getView().findViewById(R.id.recording_list_recyclerview_cloud);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeActionHandler((RecordingListAdapter) mAdapter,1,this));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            Log.d(TAG,"mAdapter notified");
        }
        Log.d(TAG,"There are "+cloudList.size()+" items in cloud list");
    }
    private class CloudSuccListener implements OnSuccessListener<ListResult>
    {
        public ArrayList<String> filenames = new ArrayList<>();

        public ArrayList<String> getFilenames() {
            return filenames;
        }

        private String FBfolder = null;

        public CloudSuccListener(String fireBaseFolder)
        {
            FBfolder = fireBaseFolder;
        }
        @Override
        public void onSuccess(ListResult listResult) {
            for (StorageReference item : listResult.getItems()) {
                // All the items under listRef.
                Log.d(TAG,item.getName());
                filenames.add(item.getName());
            }
            updateFiles(filenames);
            updateMetaData(filenames,FBfolder);
        }
    }
    private void updateMetaData(ArrayList<String> filenames,String fireBaseFolder)
    {
        durations = new ArrayList<>();
        ArrayList<CloudMetaSuccListener> succListeners = new ArrayList<>();
        for(int idx = 0;idx<filenames.size();idx++)
        {
            final String filename = filenames.get(idx);
            CloudMetaSuccListener metaRstListener = new CloudMetaSuccListener(filename);
            succListeners.add(metaRstListener);
            StorageReference listRef = storageRef.child(fireBaseFolder).child(filename);
            listRef.getMetadata()
                    .addOnSuccessListener(metaRstListener)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.d(TAG,"MetaData for File: "+filename+" Downloaded successfully");
                        }
                    });
        }
        for(int idx = 0;idx<succListeners.size();idx++)
        {
            CloudMetaSuccListener listener = succListeners.get(idx);
            durations.add(listener.getMetaDataValue());
        }

        for(int idx = 0;idx<durations.size();idx++) {
            String duration = durations.get(idx);
            cloudList.get(idx).setDuration(duration);
        }

        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            Log.d(TAG,"Update the list");
        }
    }
    private class CloudMetaSuccListener implements OnSuccessListener<StorageMetadata>
    {
        private String filename;
        private String metaDataValue;// = null;
        public CloudMetaSuccListener(String fn)
        {
            metaDataValue = null;
            filename = fn;
        }
        public String getMetaDataValue() {
            return metaDataValue;
        }
        @Override
        public void onSuccess(StorageMetadata metaDataRst) {
            metaDataValue = metaDataRst.getCustomMetadata(durationMetaDataConst);
            Log.d(TAG,"MetaData for File: "+filename+" Downloaded successfully");
        }
    }
    public void deleteFile(final String filenamePref, final String filenameSuf, final String fireBaseFolder)
    {
        if(null!=filenamePref && null!=filenameSuf && null!=fireBaseFolder)
        {
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filenamePref + "." + filenameSuf);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d(TAG,"File deleted from cloud successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG,"File not deleted from cloud!");
                }
            });
        }
    }

    public void uploadFile(String path, String localFolder, final String filenamePref, final String filenameSuf, final String fireBaseFolder, String duration)
    {
        if(null!=path && null!=filenamePref && null!=filenameSuf && null!=fireBaseFolder)
        {
            final String fullPath = path + "/" +localFolder+ "/" + filenamePref + "." + filenameSuf;
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
                            Toast tst = Toast.makeText(getContext(),"File upload Successful", Toast.LENGTH_SHORT);
                            tst.show();
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
                            Toast tst = Toast.makeText(getContext(),"File upload Unsuccessful", Toast.LENGTH_SHORT);
                            tst.show();
                        }
                    });
            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/mp4")
                    .setCustomMetadata(durationMetaDataConst, duration)
                    .build();
            // Update metadata properties
            storageReference.updateMetadata(metadata)
                    .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            // Updated metadata is in storageMetadata
                            Log.d(TAG,"File metadata update successful");
                            Log.d(TAG,"For file: "+fireBaseFolder+"//"+filenamePref + "." + filenameSuf);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.d(TAG,"File metadata update unsuccessful");
                        }
                    });
        }
        else
        {
            return;
        }
    }
    public boolean downloadFile(String path,String localFolder,String filenamePref,String filenameSuf,String fireBaseFolder)
    {
        try
        {
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filenamePref + "." + filenameSuf);
            final String fullPath = path + "/" +localFolder+ "/" + filenamePref + "." + filenameSuf;
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
        }
        else
        {
            cloudList.clear();
            cloudList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
        }
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            Log.d(TAG,"Update the list");
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
    /*
    public void updateAllFiles(String bucketname)
    {
        ArrayList<String> files = listFiles(bucketname);
        Log.d(TAG,"There are "+files.size()+" items in files list");
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
    }*/
    public String parseSeconds(int seconds) {
        int min = seconds / 60;
        seconds-=(min * 60);
        return (min < 10 ? "0" + min : String.valueOf(min)) + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
    }


    public void setActivity(MainActivity mainActivity)
    {
        activity = mainActivity;
    }
}