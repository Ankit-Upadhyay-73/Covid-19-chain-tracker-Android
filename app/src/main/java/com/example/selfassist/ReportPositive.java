package com.example.selfassist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfImage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class ReportPositive extends AppCompatActivity
{
    DataSnapshot copiedValue;
    boolean containsText = false;
    TextView textView;
    boolean isFoundPositive = false;
    String contactNumber;
    LinearLayout contentViewLayout;
    Button submitCovidReportBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_positive);
        textView = findViewById(R.id.textView);
        contentViewLayout = findViewById(R.id.contentViewLayout);
        submitCovidReportBtn = findViewById(R.id.submitCovidReportBtn);
        if (!checkPermissionForReadExtertalStorage()) {
            try { requestPermissionForReadExtertalStorage(); }
            catch (Exception e) { e.printStackTrace(); }
        }
        Intent intent = getIntent();
        contactNumber = intent.getStringExtra("mobileNumber");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        String pageText = "";
        String imageText = "";
        if (requestCode == 10 && resultCode ==RESULT_OK )
        {
            try
            {

                //commented for testing purpose
/*
                String content_des = data.getDataString();
                System.out.println("lcoation "+content_des);
                PdfReader reader = new PdfReader(content_des);
                int n = reader.getNumberOfPages();
                for (int i=0;i<n;i++)
                {
                    pageText = pageText+ PdfTextExtractor.getTextFromPage(reader,i+1).trim()+"\n";
                    if (pageText!=null && pageText.trim().length()>0)
                    {
                        containsText = true;
                        break;
                    }
                }
*/
                pageText = "covid-19positive";
                System.out.println("pdf text are "+pageText);
                if ((pageText.contains("covid-19") || pageText.contains("2019-nCov") || pageText.contains("covid19")) && (pageText.contains("Positive") || pageText.contains("positive")))
                {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("Found As Positive");
                    isFoundPositive = true;
                    textView.setTextColor(getResources().getColor(R.color.colorRed));
                }
                else
                {
                    if ((pageText.contains("covid-19") || pageText.contains("2019-nCov") || pageText.contains("covid19")) && (pageText.contains("Negative") || pageText.contains("negative")))
                    {
                        textView.setText("Found As negative");
                        textView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("Can't Fetch result");
                    }
                }
/*
                if (containsText || !containsText)
                {
                    System.out.println(pageText);
                    //reading images
                    PRStream prStream;
                    PdfObject pdfObject;
                    PdfImageObject pdfImageObject;
                    FileOutputStream fos;
                    // Create pdf reader
                    int numOfObject=reader.getXrefSize();
                    for(int i=0;i<numOfObject;i++)
                    {
                        // Get PdfObject
                        pdfObject=reader.getPdfObject(i);
                        if(pdfObject!=null && pdfObject.isStream())
                        {
                            prStream= (PRStream)pdfObject; //cast object to stream
                            PdfObject type =prStream.get(PdfName.SUBTYPE); //get the object type
                            // Check if the object is the image type object
                            if (type != null && type.toString().equals(PdfName.IMAGE.toString()))
                            {
                                // Get the image from the stream
                                pdfImageObject= new PdfImageObject(prStream);
                                byte[] imgdata=pdfImageObject.getImageAsBytes();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imgdata,0,imgdata.length);
                                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                                Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();
                                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
                                for (i = 0; i < textBlocks.size(); i++)
                                {
                                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                                    imageText+= textBlock.getValue();                   // return string
                                }
                            }
                        }
                    }
                }
                System.out.println(imageText);
*/
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean checkPermissionForReadExtertalStorage()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            int result = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExtertalStorage() throws Exception
    {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    10);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }


    //Design Controller

    public void setOnSubmitCovidReport(View view)
    {
        final DatabaseReference covidAffectedUser = FirebaseDatabase.getInstance().getReference().child("covidAffectedUsers");
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if (isFoundPositive)
        {
            submitCovidReportBtn.setText("Reported");
            databaseReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                    while (dataSnapshotIterator.hasNext())
                    {
                        final DataSnapshot values =  dataSnapshotIterator.next();
                        if (values.getKey().equals(contactNumber))
                        {
                            copiedValue = values;
                            covidAffectedUser.child(contactNumber).setValue(copiedValue.getValue());
                            values.getRef().removeValue();
                            System.out.println("Copied value is: "+copiedValue.getKey());
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }

        Snackbar.make(contentViewLayout,"Reported Successfully",Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.colorRed)).setAction("Undo", new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitCovidReportBtn.setText("Report");
                covidAffectedUser.child(contactNumber).removeValue();
                databaseReference.setValue(copiedValue);
            }
        }).setActionTextColor(getResources().getColor(R.color.colorPrimary)).show();

    }

    public void chooseDocumentFile(View view)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent,"Choose document file"),10);
    }
}
