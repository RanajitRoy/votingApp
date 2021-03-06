/*
 * Author: Arnab Kumar Basu, Ranajit Roy
 */

package com.votingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.votingapp.entities.Voter;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;

public class FacialRecognition extends AppCompatActivity {
    private boolean successFlag;
    private boolean logout;
    private String candidatesStr;
    private Voter voter;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button submit;
    private ImageView capturedImage;
    int countFc = 0;
    private String capImage;
    private ProgressBar pBarfc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_recognition);

        Intent g=getIntent();
        candidatesStr = (String) g.getStringExtra("CANDIDATES");
        voter = (Voter) g.getSerializableExtra("Voter");
        successFlag = true;
        logout = false;
        pBarfc = (ProgressBar) findViewById(R.id.pBarfc);
        pBarfc.setVisibility(View.INVISIBLE);

        final Button fcCapture = (Button) findViewById(R.id.faceRecCaptureButton);
        fcCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
                {
                    launchCamera1();
                }
            }
        });

        submit = (Button) findViewById(R.id.faceRecSubmitButton);
        submit.setEnabled(false);
        submit.setBackgroundColor(0xFFBFBFBF);
        submit.setTextColor(0xFF858383);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(logout)
                    finish();
                if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
                {
                    pBarfc.setVisibility(View.VISIBLE);
                    FacialRecognition.VoterDB ob = new FacialRecognition.VoterDB();
                    ob.execute(voter.getEpic_no()+" "+capImage);
                    submit.setEnabled(false);
                    submit.setBackgroundColor(0xFFBFBFBF);
                    submit.setTextColor(0xFF858383);
                }
            }
        });

        capturedImage = (ImageView) findViewById(R.id.capturedImage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capturedImage = (ImageView) findViewById(R.id.capturedImage);
        TextView tv = (TextView) findViewById(R.id.facerecogWarning);
        final Button fcCapture = (Button) findViewById(R.id.faceRecCaptureButton);
        final Button fcSubmit = (Button) findViewById(R.id.faceRecSubmitButton);
        if(capturedImage.getDrawable() != null)
        {
            fcCapture.setEnabled(false);
            fcCapture.setBackgroundColor(0xFFBFBFBF);
            fcCapture.setTextColor(0xFF858383);
            tv.setText("Image has been captured");
            fcSubmit.setEnabled(true);
            fcSubmit.setBackgroundColor(0xFF0099CC);
            fcSubmit.setTextColor(0xFFFFFFFF);

        }
        else{
            fcSubmit.setEnabled(false);
            fcSubmit.setBackgroundColor(0xFFBFBFBF);
            fcSubmit.setTextColor(0xFF858383);

        }
    }


    public void goToCastVote(){
        Intent i = new Intent(getApplicationContext(), CastVote.class);
        i.putExtra("CANDIDATES", candidatesStr);
        i.putExtra("Voter", voter);
        startActivity(i);
        finish();
    }

    public void launchCamera1() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            capturedImage.setImageBitmap(imageBitmap);
            capImage = convert(imageBitmap);
        }
    }


    // method for converting bitmap to string base 64
    public String convert(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }

    public class VoterDB extends AsyncTask<String,String,Void> {
        private String IP = Connect.IP;
        private int port = Connect.port;
        private Socket s = null;
        private DataOutputStream out = null;

        @Override
        protected Void doInBackground(String... params) {
            String mssg = params[0];
            try {
                s = new Socket(IP, port);

                out = new DataOutputStream(s.getOutputStream());
                out.flush();
                out.writeUTF(5 + " " + mssg);
                out.flush();
                s.close();

                s = new Socket(IP, port);
                InputStream in = s.getInputStream();
                DataInputStream d_in = new DataInputStream(in);
                String l = d_in.readUTF();

                if(l.equals("INVALID"))
                    successFlag = false;
                else
                    successFlag = true;

                in.close();
                s.close();
            } catch (Exception e) {
                successFlag = false;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void _void) {
            pBarfc.setVisibility(View.INVISIBLE);
            if(countFc >= 3){
                finish();
            }
            else {
                if (successFlag) {
                    goToCastVote();
                }
                else {
                    countFc++;
                    TextView tv = (TextView) findViewById(R.id.facerecogWarning);

                    if (countFc >= 3) {
                        tv.setText("Face Recognition FAILED!");
                        logout = true;
                        submit.setText("Logout");
                        submit.setEnabled(true);
                        submit.setBackgroundColor(0xFF0099CC);
                        submit.setTextColor(0xFFFFFFFF);
                    } else {
                        capturedImage = (ImageView) findViewById(R.id.capturedImage);
                        capturedImage.setImageDrawable(null);
                        capImage = "NA";

                        final Button fcCapture = (Button) findViewById(R.id.faceRecCaptureButton);
                        fcCapture.setEnabled(true);
                        fcCapture.setBackgroundColor(0xFF0099CC);
                        fcCapture.setTextColor(0xFFFFFFFF);
                        submit.setEnabled(false);
                        submit.setBackgroundColor(0xFFBFBFBF);
                        submit.setTextColor(0xFF858383);


                        if (countFc == 1) {
                            tv.setText("FAILED! You have 2 chances left");
                        } else if (countFc == 2) {
                            tv.setText("FAILED! You have 1 chance left");
                        }

                    }
                }
            }
        }
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to logout", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}
