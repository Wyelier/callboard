package com.example.callboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.callboard.Utils.MyConstants;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class EditActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private ImageView imgItem;
    private Uri uploadUri;
    private Spinner spinner;
    private DatabaseReference dRef;
    private FirebaseAuth mAuth;
    private EditText editTitle, editPrice, editPhone, editDesc;
    private boolean edit_state = false;
    private String temp_cat = "";
    private String temp_uid = "";
    private String temp_time = "";
    private String temp_key = "";
    private String temp_img_Url = "";
    private boolean is_img_upd = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        init();
    }
    private void init()
    {
        editTitle = findViewById(R.id.editTitle);
        editPrice = findViewById(R.id.editPrice);
        editPhone = findViewById(R.id.editPhone);
        editDesc = findViewById(R.id.editDesc);
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        imgItem = findViewById(R.id.imgItem);
        getMyIntent();
    }
    private void getMyIntent()
    {
        if(getIntent() != null)
        {
            Intent i = getIntent();
            edit_state = i.getBooleanExtra(MyConstants.EDIT_STATE, false);
            if(edit_state)setDataAds(i);

        }
    }
    private void setDataAds(Intent i)
    {
        Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imgItem);
        editTitle.setText(i.getStringExtra(MyConstants.TITLE));
        editPrice.setText(i.getStringExtra(MyConstants.PRICE));
        editPhone.setText(i.getStringExtra(MyConstants.PHONE));
        editDesc.setText(i.getStringExtra(MyConstants.DESC));

        temp_cat = i.getStringExtra(MyConstants.CAT);
        temp_uid = i.getStringExtra(MyConstants.UID);
        temp_time = i.getStringExtra(MyConstants.TIME);
        temp_key = i.getStringExtra(MyConstants.KEY);
        temp_img_Url = i.getStringExtra(MyConstants.IMAGE_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10 && data != null && data.getData() != null)
        {
            if(resultCode == RESULT_OK)
            {
                imgItem.setImageURI(data.getData());
                is_img_upd = true;
            }
        }
    }
    private void uploadImage()
    {
        Bitmap bitmap = ((BitmapDrawable)imgItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mRef = mStorageRef.child(System.currentTimeMillis() + "_image");
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                    assert uploadUri != null;
                    savePost();
                    Toast.makeText(EditActivity.this, "Upload done: " + uploadUri.toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
    private void uploadUpdateImage()
    {
        Bitmap bitmap = ((BitmapDrawable)imgItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(temp_img_Url);
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                assert uploadUri != null;
                temp_img_Url = uploadUri.toString();
                updatePost();
                Toast.makeText(EditActivity.this, "Upload done: " + uploadUri.toString(), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
    public void onClickSavePost(View view)
    {
        if(!edit_state)
        {
            uploadImage();
        }
        else
        {
            if(is_img_upd)
            {
                uploadUpdateImage();
            }
            else
            {
                updatePost();
            }
        }
        finish();
    }
    public void onClickImage(View view)
    {
        getImage();
    }
    private void getImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 10);
    }
    private void savePost()
    {
        dRef = FirebaseDatabase.getInstance().getReference(spinner.getSelectedItem().toString());
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getUid() != null)
        {
            String key = dRef.push().getKey();
            NewPost post = new NewPost();

            post.setImageId(uploadUri.toString());
            post.setTitle(editTitle.getText().toString());
            post.setPrice(editPrice.getText().toString());
            post.setPhone(editPhone.getText().toString());
            post.setDesc(editDesc.getText().toString());
            post.setKey(key);
            post.setCat(spinner.getSelectedItem().toString());
            post.setTime(String.valueOf(System.nanoTime()));
            post.setUid(mAuth.getUid());

            if(key != null)dRef.child(key).child("ad").setValue(post);
        }
    }
    private void updatePost()
    {
        dRef = FirebaseDatabase.getInstance().getReference(temp_cat);
        NewPost post = new NewPost();
            post.setImageId(temp_img_Url);
            post.setTitle(editTitle.getText().toString());
            post.setPrice(editPrice.getText().toString());
            post.setPhone(editPhone.getText().toString());
            post.setDesc(editDesc.getText().toString());
            post.setKey(temp_key);
            post.setCat(temp_cat);
            post.setTime(temp_time);
            post.setUid(temp_uid);
            dRef.child(temp_key).child("ad").setValue(post);
    }
}
