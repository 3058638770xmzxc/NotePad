/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Settings activity for NotePad application
 */
public class SettingsActivity extends Activity {
    
    private static final String PREF_BACKGROUND_COLOR = "pref_background_color";
    public static final String PREF_EDITOR_BACKGROUND = "pref_editor_background";
    public static final String PREF_FONT_SIZE = "pref_font_size";
    private static final String PREF_SORT_ORDER = "pref_sort_order";
    public static final String PREF_BACKGROUND_IMAGE = "pref_background_image";
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_CROP_IMAGE = 1002;
    // Removed compression limits - use full quality images
    
    private RadioGroup backgroundGroup;
    private RadioGroup sortOrderGroup;
    private Button selectImageButton;
    private Button removeImageButton;
    
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        // Set up action bar if available
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(R.string.settings_title);
        } else {
            setTitle(R.string.settings_title);
        }
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        initViews();
        loadSettings();
        setupListeners();
    }
    
    private void initViews() {
        try {
            backgroundGroup = (RadioGroup) findViewById(R.id.background_color_group);
            sortOrderGroup = (RadioGroup) findViewById(R.id.sort_order_group);
            selectImageButton = (Button) findViewById(R.id.select_image_button);
            removeImageButton = (Button) findViewById(R.id.remove_image_button);
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Error initializing views", e);
        }
    }
    
    private void loadSettings() {
        if (prefs == null) {
            return;
        }
        
        try {
            // Load background color setting
            if (backgroundGroup != null) {
                String bgColor = prefs.getString(PREF_BACKGROUND_COLOR, "white");
                switch (bgColor) {
                    case "white":
                        backgroundGroup.check(R.id.bg_white);
                        break;
                    case "cream":
                        backgroundGroup.check(R.id.bg_cream);
                        break;
                    case "blue":
                        backgroundGroup.check(R.id.bg_blue);
                        break;
                    case "green":
                        backgroundGroup.check(R.id.bg_green);
                        break;
                }
            }
            
            // Load sort order setting
            if (sortOrderGroup != null) {
                String sortOrder = prefs.getString(PREF_SORT_ORDER, "modified_desc");
                switch (sortOrder) {
                    case "modified_desc":
                        sortOrderGroup.check(R.id.sort_modified_desc);
                        break;
                    case "modified_asc":
                        sortOrderGroup.check(R.id.sort_modified_asc);
                        break;
                    default:
                        sortOrderGroup.check(R.id.sort_modified_desc);
                        break;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Error loading settings", e);
        }
    }
    
    private void setupListeners() {
        if (prefs == null) {
            return;
        }
        
        try {
            if (backgroundGroup != null) {
                backgroundGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        try {
                            String color = "white";
                            if (checkedId == R.id.bg_cream) color = "cream";
                            else if (checkedId == R.id.bg_blue) color = "blue";
                            else if (checkedId == R.id.bg_green) color = "green";
                            prefs.edit().putString(PREF_BACKGROUND_COLOR, color).apply();
                        } catch (Exception e) {
                            android.util.Log.e("SettingsActivity", "Error saving background color", e);
                        }
                    }
                });
            }
            
            if (sortOrderGroup != null) {
                sortOrderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        try {
                            String sortOrder = "modified_desc";
                            if (checkedId == R.id.sort_modified_asc) sortOrder = "modified_asc";
                            prefs.edit().putString(PREF_SORT_ORDER, sortOrder).apply();
                        } catch (Exception e) {
                            android.util.Log.e("SettingsActivity", "Error saving sort order", e);
                        }
                    }
                });
            }
            
            // Setup image selection button
            if (selectImageButton != null) {
                selectImageButton.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View v) {
                        // Open image picker
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_PICK_IMAGE);
                    }
                });
            }
            
            // Setup remove image button
            if (removeImageButton != null) {
                removeImageButton.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View v) {
                        // Remove background image
                        prefs.edit().remove(PREF_BACKGROUND_IMAGE).apply();
                        // Delete saved image file
                        File imageFile = new File(getFilesDir(), "background_image.png");
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                        Toast.makeText(SettingsActivity.this, "背景图片已移除", Toast.LENGTH_SHORT).show();
                    }
                });
                
                // Update button visibility based on whether image exists
                String imagePath = prefs.getString(PREF_BACKGROUND_IMAGE, null);
                removeImageButton.setEnabled(imagePath != null && !imagePath.isEmpty());
            }
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Error setting up listeners", e);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Start crop activity
                startCropActivity(selectedImageUri);
            }
        } else if (requestCode == REQUEST_CODE_CROP_IMAGE && resultCode == RESULT_OK) {
            // Handle cropped image
            if (data != null) {
                Uri croppedImageUri = data.getData();
                if (croppedImageUri != null) {
                    // Image was saved to file by crop activity, just update preferences
                    File imageFile = new File(getFilesDir(), "background_image.jpg");
                    if (imageFile.exists()) {
                        prefs.edit().putString(PREF_BACKGROUND_IMAGE, imageFile.getAbsolutePath()).apply();
                        
                        // Update remove button state
                        if (removeImageButton != null) {
                            removeImageButton.setEnabled(true);
                        }
                        
                        Toast.makeText(this, "背景图片已设置", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If no data, try to get from extras (some crop activities return bitmap)
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap bitmap = (Bitmap) extras.getParcelable("data");
                        if (bitmap != null) {
                            saveBitmapToFile(bitmap);
                        }
                    }
                }
            } else {
                // Check if file was created by crop activity
                File imageFile = new File(getFilesDir(), "background_image.jpg");
                if (imageFile.exists()) {
                    prefs.edit().putString(PREF_BACKGROUND_IMAGE, imageFile.getAbsolutePath()).apply();
                    
                    // Update remove button state
                    if (removeImageButton != null) {
                        removeImageButton.setEnabled(true);
                    }
                    
                    Toast.makeText(this, "背景图片已设置", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void startCropActivity(Uri imageUri) {
        try {
            // Get screen dimensions
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            
            // Get image dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
            
            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;
            
            // Calculate crop aspect ratio based on screen size
            float aspectRatio = (float) screenWidth / screenHeight;
            
            // Try to use system crop intent
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(imageUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", screenWidth);
            cropIntent.putExtra("aspectY", screenHeight);
            cropIntent.putExtra("outputX", screenWidth);
            cropIntent.putExtra("outputY", screenHeight);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data", true);
            
            // Try to start crop activity
            if (cropIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE);
            } else {
                // If system crop is not available, use custom crop
                startCustomCropActivity(imageUri, screenWidth, screenHeight, aspectRatio);
            }
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Error starting crop activity", e);
            // Fallback: process image without cropping
            processAndSaveImage(imageUri);
        }
    }
    
    private void startCustomCropActivity(Uri imageUri, int screenWidth, int screenHeight, float aspectRatio) {
        // Start custom crop activity
        Intent cropIntent = new Intent(this, ImageCropActivity.class);
        cropIntent.setData(imageUri);
        startActivityForResult(cropIntent, REQUEST_CODE_CROP_IMAGE);
    }
    
    private void processAndSaveImage(Uri imageUri) {
        try {
            // Load and compress image
            Bitmap bitmap = loadAndCompressImage(imageUri);
            if (bitmap != null) {
                saveBitmapToFile(bitmap);
            } else {
                Toast.makeText(this, "处理图片失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Error processing image", e);
            Toast.makeText(this, "处理图片失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private Bitmap loadAndCompressImage(Uri imageUri) {
        try {
            // Load image directly without compression
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888; // Use ARGB_8888 for best quality
            options.inDither = true;
            options.inPurgeable = false;
            options.inInputShareable = false;
            
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
            
            return bitmap;
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Error loading image", e);
            return null;
        }
    }
    
    private void saveBitmapToFile(Bitmap bitmap) {
        try {
            // Get screen dimensions to save at 2x resolution for better quality
            android.util.DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            
            // Save at 2x screen resolution for high quality display
            int targetWidth = screenWidth * 2;
            int targetHeight = screenHeight * 2;
            
            // Scale bitmap to target resolution if needed
            Bitmap finalBitmap = bitmap;
            if (bitmap.getWidth() != targetWidth || bitmap.getHeight() != targetHeight) {
                // Use high-quality scaling
                finalBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                if (finalBitmap != bitmap) {
                    bitmap.recycle();
                }
            }
            
            // Save as PNG for lossless quality (or use JPEG at 100% quality)
            File imageFile = new File(getFilesDir(), "background_image.png");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            
            // Use PNG for lossless compression, or JPEG at 100% quality
            // PNG is better for quality but larger file size
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            
            outputStream.close();
            finalBitmap.recycle();
            
            // Save image path to preferences
            prefs.edit().putString(PREF_BACKGROUND_IMAGE, imageFile.getAbsolutePath()).apply();
            
            // Update remove button state
            if (removeImageButton != null) {
                removeImageButton.setEnabled(true);
            }
            
            Toast.makeText(this, "背景图片已设置", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("SettingsActivity", "Error saving bitmap to file", e);
            Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public static String getBackgroundColor(SharedPreferences prefs) {
        return prefs.getString(PREF_BACKGROUND_COLOR, "white");
    }
    
    public static String getEditorBackground(SharedPreferences prefs) {
        return prefs.getString(PREF_EDITOR_BACKGROUND, "white");
    }
    
    public static int getFontSize(SharedPreferences prefs) {
        return prefs.getInt(PREF_FONT_SIZE, 22);
    }
    
    public static String getSortOrder(SharedPreferences prefs) {
        return prefs.getString(PREF_SORT_ORDER, "modified_desc");
    }
}

