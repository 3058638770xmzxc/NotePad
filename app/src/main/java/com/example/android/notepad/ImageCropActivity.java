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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Custom image crop activity that allows users to crop images to match screen aspect ratio
 */
public class ImageCropActivity extends Activity {
    
    private ImageView imageView;
    private View cropOverlay;
    private Button confirmButton;
    private Button cancelButton;
    
    private Bitmap originalBitmap;
    private Uri imageUri;
    private float aspectRatio;
    private int screenWidth;
    private int screenHeight;
    
    private float cropX = 0.5f; // Center position (0.0 to 1.0)
    private float cropY = 0.5f;
    private float cropWidth = 0.8f; // Crop area width (0.0 to 1.0)
    private float cropHeight = 0.8f;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        
        // Get screen dimensions
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        aspectRatio = (float) screenWidth / screenHeight;
        
        // Get image URI from intent
        imageUri = getIntent().getData();
        if (imageUri == null) {
            Toast.makeText(this, "无法加载图片", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadImage();
    }
    
    private void initViews() {
        imageView = (ImageView) findViewById(R.id.crop_image_view);
        cropOverlay = findViewById(R.id.crop_overlay);
        confirmButton = (Button) findViewById(R.id.crop_confirm);
        cancelButton = (Button) findViewById(R.id.crop_cancel);
        
        if (confirmButton != null) {
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performCrop();
                }
            });
        }
        
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        }
        
        // Make image view touchable for panning
        if (imageView != null) {
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Simple panning implementation
                    // In a full implementation, you would add pinch-to-zoom and drag-to-pan
                    return false;
                }
            });
        }
    }
    
    private void loadImage() {
        try {
            // Load and scale image to fit screen
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
            
            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;
            
            // Calculate sample size
            int sampleSize = 1;
            int maxDimension = Math.max(screenWidth, screenHeight) * 2; // Load at 2x for better quality
            if (imageWidth > maxDimension || imageHeight > maxDimension) {
                int widthRatio = imageWidth / maxDimension;
                int heightRatio = imageHeight / maxDimension;
                sampleSize = Math.max(widthRatio, heightRatio);
            }
            
            // Load bitmap with high quality settings
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888; // Use ARGB_8888 for best quality
            options.inDither = true;
            options.inPurgeable = false;
            options.inInputShareable = false;
            
            inputStream = getContentResolver().openInputStream(imageUri);
            originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
            
            if (originalBitmap != null && imageView != null) {
                imageView.setImageBitmap(originalBitmap);
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                updateCropOverlay();
            }
        } catch (Exception e) {
            android.util.Log.e("ImageCropActivity", "Error loading image", e);
            Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void updateCropOverlay() {
        // Update crop overlay position and size
        // This is a simplified version - in a full implementation, you would
        // create a custom view that draws the crop rectangle
    }
    
    private void performCrop() {
        if (originalBitmap == null) {
            Toast.makeText(this, "图片未加载", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Calculate crop area in pixels
            int bitmapWidth = originalBitmap.getWidth();
            int bitmapHeight = originalBitmap.getHeight();
            
            // Calculate crop dimensions maintaining aspect ratio
            int cropW, cropH;
            if ((float) bitmapWidth / bitmapHeight > aspectRatio) {
                // Image is wider than screen aspect ratio
                cropH = bitmapHeight;
                cropW = (int) (cropH * aspectRatio);
            } else {
                // Image is taller than screen aspect ratio
                cropW = bitmapWidth;
                cropH = (int) (cropW / aspectRatio);
            }
            
            // Center the crop area
            int cropX = (bitmapWidth - cropW) / 2;
            int cropY = (bitmapHeight - cropH) / 2;
            
            // Ensure crop area is within bounds
            cropX = Math.max(0, Math.min(cropX, bitmapWidth - cropW));
            cropY = Math.max(0, Math.min(cropY, bitmapHeight - cropH));
            
            // Crop the bitmap
            Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, cropX, cropY, cropW, cropH);
            
            // Scale to 2x screen resolution for high quality display
            // This ensures the image looks sharp even on high-DPI screens
            int targetWidth = screenWidth * 2;
            int targetHeight = screenHeight * 2;
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetWidth, targetHeight, true);
            croppedBitmap.recycle();
            
            // Save as PNG for lossless quality
            File imageFile = new File(getFilesDir(), "background_image.png");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // PNG is lossless
            outputStream.close();
            scaledBitmap.recycle();
            
            // Return result
            Intent resultIntent = new Intent();
            resultIntent.setData(Uri.fromFile(imageFile));
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            android.util.Log.e("ImageCropActivity", "Error cropping image", e);
            Toast.makeText(this, "裁剪图片失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
    }
}
