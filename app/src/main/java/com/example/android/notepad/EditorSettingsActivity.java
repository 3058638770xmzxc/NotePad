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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Editor Settings activity for NotePad application
 * This is a separate settings page for the note editor, with editor background and font size settings
 */
public class EditorSettingsActivity extends Activity {
    
    private RadioGroup editorBackgroundGroup;
    private SeekBar fontSizeSeekBar;
    private TextView fontSizeText;
    
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_settings);
        
        // Set up action bar if available
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("编辑器设置");
        } else {
            setTitle("编辑器设置");
        }
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        initViews();
        loadSettings();
        setupListeners();
    }
    
    private void initViews() {
        try {
            editorBackgroundGroup = (RadioGroup) findViewById(R.id.editor_background_group);
            fontSizeSeekBar = (SeekBar) findViewById(R.id.font_size_seekbar);
            fontSizeText = (TextView) findViewById(R.id.font_size_text);
        } catch (Exception e) {
            android.util.Log.e("EditorSettingsActivity", "Error initializing views", e);
        }
    }
    
    private void loadSettings() {
        if (prefs == null) {
            return;
        }
        
        try {
            // Load editor background
            if (editorBackgroundGroup != null) {
                String editorBg = SettingsActivity.getEditorBackground(prefs);
                switch (editorBg) {
                    case "white":
                        editorBackgroundGroup.check(R.id.editor_bg_white);
                        break;
                    case "cream":
                        editorBackgroundGroup.check(R.id.editor_bg_cream);
                        break;
                    case "blue":
                        editorBackgroundGroup.check(R.id.editor_bg_blue);
                        break;
                    case "green":
                        editorBackgroundGroup.check(R.id.editor_bg_green);
                        break;
                }
            }
            
            // Load font size
            if (fontSizeSeekBar != null && fontSizeText != null) {
                int fontSize = SettingsActivity.getFontSize(prefs);
                fontSizeSeekBar.setProgress(fontSize - 14); // 14-30 range
                fontSizeText.setText(getString(R.string.font_size_label, fontSize));
            }
        } catch (Exception e) {
            android.util.Log.e("EditorSettingsActivity", "Error loading settings", e);
        }
    }
    
    private void setupListeners() {
        if (prefs == null) {
            return;
        }
        
        try {
            if (editorBackgroundGroup != null) {
                editorBackgroundGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        try {
                            String color = "white";
                            if (checkedId == R.id.editor_bg_cream) color = "cream";
                            else if (checkedId == R.id.editor_bg_blue) color = "blue";
                            else if (checkedId == R.id.editor_bg_green) color = "green";
                            prefs.edit().putString(SettingsActivity.PREF_EDITOR_BACKGROUND, color).apply();
                        } catch (Exception e) {
                            android.util.Log.e("EditorSettingsActivity", "Error saving editor background color", e);
                        }
                    }
                });
            }
            
            if (fontSizeSeekBar != null && fontSizeText != null) {
                fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            int fontSize = progress + 14; // 14-30 range
                            fontSizeText.setText(getString(R.string.font_size_label, fontSize));
                            prefs.edit().putInt(SettingsActivity.PREF_FONT_SIZE, fontSize).apply();
                        }
                    }
                    
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }
        } catch (Exception e) {
            android.util.Log.e("EditorSettingsActivity", "Error setting up listeners", e);
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
}
