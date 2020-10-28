package com.teambald.cse442_project_team_bald.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.teambald.cse442_project_team_bald.Encryption.AudioEncryptionUtils;
import com.teambald.cse442_project_team_bald.Encryption.FileUtils;
import com.teambald.cse442_project_team_bald.R;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;


public class HomeFragment extends Fragment {

    private ImageButton recorderButton;
    private boolean isRecording;
    private MediaPlayer mediaPlayer = null;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private String fileToPlay;
    private MediaRecorder mediaRecorder;
    private String recordFile;
    //Path of new recording.
    private String filePath;

    private Chronometer timer;
    
    
    private static final String TAG = "HOME_FRAGMENT: ";

    private ImageButton recordButton;

    private TextView accountText;

    private HomeFragment homeFragObj;

    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
  
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_fragment, container, false);
    }
  
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        recordButton = view.findViewById(R.id.recorder_button);
        recordButton.setOnClickListener(new recordClickListener());
        accountText = view.findViewById(R.id.login_account_text);
  
        checkPermissions();
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        recorderButton = view.findViewById(R.id.recorder_button);
        isRecording= false;
    }
    @Override
    public void onStart() {
        super.onStart();

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        updateUI(account);
        // [END on_start_sign_in]
    }
    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            accountText.setText("Signed In as: "+account.getDisplayName());
        } else {
            accountText.setText("None");
        }
    }

    private class recordClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
                if (isRecording) {
                    //Stop Recording
                    recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_recorder_icon_150, null));
                    stopRecording();
                    isRecording = false;

                } else {
                    //Check permission to record audio
                    //Start Recording
                    startRecording();
                    recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button, null));
                    isRecording = true;
            }
        }
    }
    private void startRecording() {

        //Get app external directory path
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.US);
        Date now = new Date();
        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_"+formatter.format(now)+ ".mp4";

        //Path used for encryption.
//        final String filePath = recordPath + "/" + recordFile;
        filePath = recordPath + "/" + recordFile;

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //Save recording periodically.
        //Read saved recording length (default to 5 mins).
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int max = sharedPref.getInt(getString(R.string.recording_length_key), 5) * 60 * 1000;
//        mediaRecorder.setMaxDuration(max);
        mediaRecorder.setMaxDuration(5000);
        //Will be executed when reach max duration.
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                //When reach max duration, stop, save the file and start again.
                if (i == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    //Stop and save the audio.
                    mediaRecorder.stop();
                    mediaRecorder.release();

                    //Encrypt audio file.
                    if(encrypt(filePath)){
                        //Show toast to notify user that the file has been saved.
                        Toast toast = Toast.makeText(getContext(), "Recording has been saved.", Toast.LENGTH_SHORT);
                        toast.show();

                        //Restart the recorder.
                        startRecording();
                    }else{
                        Toast toast = Toast.makeText(getContext(), "Recording encryption failed.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
        mediaRecorder.start();
    }

    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
        }
    }

    private void stopRecording() {

        //Change text on page to file saved
        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        //Encrypt audio file.
        if(encrypt(filePath)){
            //Show toast to notify user that the file has been saved.
            Toast toast = Toast.makeText(getContext(), "Recording has been saved.", Toast.LENGTH_SHORT);
            toast.show();

            Log.e("Encryption", "encrypt " + filePath + " succeed.");

        }else{
            Toast toast = Toast.makeText(getContext(), "Recording encryption failed.", Toast.LENGTH_SHORT);
            toast.show();

            Log.e("Encryption", "encrypt " + filePath + " failed.");
        }
    }

    /**
     * Encrypt and save to disk
     *
     * @return
     */
    private boolean encrypt(String filePath) {
        try {
            byte[] fileData = FileUtils.readFile(filePath);
            byte[] encodedBytes = AudioEncryptionUtils.encode(AudioEncryptionUtils.getInstance(getContext()).getSecretKey(), fileData);
            FileUtils.saveFile(encodedBytes, filePath);
            return true;
        } catch (Exception e) {
            Log.e("Encryption", e.toString());
            Toast toast = Toast.makeText(getContext(), "Encryption failed.", Toast.LENGTH_SHORT);
            toast.show();
        }
        return false;
    }

    /**
     * Decrypt and return the decoded bytes
     *
     * @return
     */
    private byte[] decrypt(String filePath) {
        try {
            byte[] fileData = FileUtils.readFile(filePath);
            byte[] decryptedBytes = AudioEncryptionUtils.decode(AudioEncryptionUtils.getInstance(getContext()).getSecretKey(), fileData);
            return decryptedBytes;
        } catch (Exception e) {
            Toast toast = Toast.makeText(getContext(), "Decryption failed.", Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }

}