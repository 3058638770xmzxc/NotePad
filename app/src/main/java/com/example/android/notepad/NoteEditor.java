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

import static com.example.android.notepad.R.*;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This Activity handles "editing" a note, where editing is responding to
 * {@link Intent#ACTION_VIEW} (request to view data), edit a note
 * {@link Intent#ACTION_EDIT}, create a note {@link Intent#ACTION_INSERT}, or
 * create a new note from the current contents of the clipboard {@link Intent#ACTION_PASTE}.
 *
 * NOTE: Notice that the provider operations in this Activity are taking place on the UI thread.
 * This is not a good practice. It is only done here to make the code more readable. A real
 * application should use the {@link android.content.AsyncQueryHandler}
 * or {@link android.os.AsyncTask} object to perform operations asynchronously on a separate thread.
 */
public class NoteEditor extends Activity {
    // For logging and debugging purposes
    private static final String TAG = "NoteEditor";

    /*
     * Creates a projection that returns the note ID and the note contents.
     */
    private static final String[] PROJECTION =
        new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_TAG
    };

    // A label for the saved state of the activity
    private static final String ORIGINAL_CONTENT = "origContent";

    // This Activity can be started by more than one action. Each action is represented
    // as a "state" constant
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    // Global mutable variables
    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private Button mTagButton;
    private String mCurrentTag = null;
    private String mOriginalContent;
    private String mOriginalTitle;
    private String mOriginalTag;
    private SharedPreferences prefs;
    private TextView mActionBarTitle;
    private View mActionBarBack;
    private boolean mShouldSkipSave = false; // Flag to skip auto-save when user chooses "Don't save"
    private View mRootLayout; // Root layout of note_editor.xml for background color

    /**
     * Defines a custom EditText View that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;

        // This constructor is used by LayoutInflater
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);

            // Creates a Rect and a Paint object, and sets the style and color of the Paint object.
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }

        /**
         * This is called to draw the LinedEditText object
         * @param canvas The canvas on which the background is drawn.
         */
        @Override
        protected void onDraw(Canvas canvas) {

            // Gets the number of lines of text in the View.
            int count = getLineCount();

            // Gets the global Rect and Paint objects
            Rect r = mRect;
            Paint paint = mPaint;

            /*
             * Draws one line in the rectangle for every line of text in the EditText
             */
            for (int i = 0; i < count; i++) {

                // Gets the baseline coordinates for the current line of text
                int baseline = getLineBounds(i, r);

                /*
                 * Draws a line in the background from the left of the rectangle to the right,
                 * at a vertical position one dip below the baseline, using the "paint" object
                 * for details.
                 */
                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            // Finishes up by calling the parent method
            super.onDraw(canvas);
        }
    }

    /**
     * This method is called by Android when the Activity is first started. From the incoming
     * Intent, it determines what kind of editing is desired, and then does it.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Setup custom ActionBar
        if (getActionBar() != null) {
            getActionBar().setDisplayShowCustomEnabled(true);
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowHomeEnabled(false);
            
            View customView = getLayoutInflater().inflate(R.layout.custom_actionbar, null);
            mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title);
            mActionBarBack = customView.findViewById(R.id.actionbar_back);
            getActionBar().setCustomView(customView);
            
            // Set click listener for back button
            if (mActionBarBack != null) {
                mActionBarBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleBackPressed();
                    }
                });
            }
            
            // Set click listener for title to edit title
            if (mActionBarTitle != null) {
                mActionBarTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Only allow editing title for existing notes (not new notes)
                        if (mState == STATE_EDIT && mUri != null) {
                            Intent intent = new Intent(TitleEditor.EDIT_TITLE_ACTION);
                            intent.setData(mUri);
                            startActivity(intent);
                        }
                    }
                });
            }
        }

        /*
         * Creates an Intent to use when the Activity object's result is sent back to the
         * caller.
         */
        final Intent intent = getIntent();

        /*
         *  Sets up for the edit, based on the action specified for the incoming Intent.
         */

        // Gets the action that triggered the intent filter for this Activity
        final String action = intent.getAction();

        // For an edit action:
        if (Intent.ACTION_EDIT.equals(action)) {

            // Sets the Activity state to EDIT, and gets the URI for the data to be edited.
            mState = STATE_EDIT;
            mUri = intent.getData();

            // For an insert or paste action:
        } else if (Intent.ACTION_INSERT.equals(action)
                || Intent.ACTION_PASTE.equals(action)) {

            // Sets the Activity state to INSERT, gets the general note URI, and inserts an
            // empty record in the provider
        mState = STATE_INSERT;
        try {
            mUri = getContentResolver().insert(intent.getData(), null);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting new note", e);
            mUri = null;
        }

            /*
             * If the attempt to insert the new note fails, shuts down this Activity. The
             * originating Activity receives back RESULT_CANCELED if it requested a result.
             * Logs that the insert failed.
             */
            if (mUri == null) {

                // Writes the log identifier, a message, and the URI that failed.
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());

                // Closes the activity.
                finish();
                return;
            }

            // Since the new entry was created, this sets the result to be returned
            // set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        // If the action was other than EDIT or INSERT:
        } else {

            // Logs an error that the action was not understood, finishes the Activity, and
            // returns RESULT_CANCELED to an originating Activity.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        /*
         * Using the URI passed in with the triggering Intent, gets the note or notes in
         * the provider.
         * Note: This is being done on the UI thread. It will block the thread until the query
         * completes. In a sample app, going against a simple provider based on a local database,
         * the block will be momentary, but in a real app you should use
         * android.content.AsyncQueryHandler or android.os.AsyncTask.
         */
        try {
        mCursor = managedQuery(
            mUri,         // The URI that gets multiple notes from the provider.
            PROJECTION,   // A projection that returns the note ID and note content for each note.
            null,         // No "where" clause selection criteria.
            null,         // No "where" clause selection values.
            null          // Use the default sort order (modification date, descending)
        );
        } catch (Exception e) {
            Log.e(TAG, "Error querying note", e);
            mCursor = null;
        }

        // For a paste, initializes the data from clipboard.
        // (Must be done after mCursor is initialized.)
        if (Intent.ACTION_PASTE.equals(action)) {
            // Does the paste
            performPaste();
            // Switches the state to EDIT so the title can be modified.
            mState = STATE_EDIT;
        }

        // Sets the layout for this Activity. See res/layout/note_editor.xml
        setContentView(R.layout.note_editor);
        
        // Get root layout for background color (the root LinearLayout in note_editor.xml)
        View contentView = findViewById(android.R.id.content);
        if (contentView != null && contentView instanceof android.view.ViewGroup) {
            android.view.ViewGroup contentGroup = (android.view.ViewGroup) contentView;
            if (contentGroup.getChildCount() > 0) {
                mRootLayout = contentGroup.getChildAt(0);
            }
        }

        // Gets a handle to the EditText in the the layout.
        try {
            mText = (EditText) findViewById(R.id.note);
            
            // Find tag button
            mTagButton = (Button) findViewById(R.id.tag_button);
            
            // Set click listener for tag button
            if (mTagButton != null) {
                Log.d(TAG, "Tag button found, setting click listener");
                // Initialize button text
                if (mCurrentTag == null || mCurrentTag.trim().isEmpty()) {
                    mTagButton.setText(getString(R.string.tag_hint));
                } else {
                    mTagButton.setText(mCurrentTag);
                }
                
                mTagButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Tag button clicked - onClick called");
                        android.util.Log.d(TAG, "Current Activity: " + NoteEditor.this);
                        android.util.Log.d(TAG, "Is Finishing: " + isFinishing());
                        try {
                            Log.d(TAG, "Calling showTagSelectionDialog()");
                            showTagSelectionDialog();
                            Log.d(TAG, "showTagSelectionDialog() returned");
                        } catch (Exception e) {
                            Log.e(TAG, "Error showing tag selection dialog", e);
                            e.printStackTrace();
                            // Show error toast
                            android.widget.Toast.makeText(NoteEditor.this, "错误: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.d(TAG, "Click listener set on tag button");
            } else {
                Log.e(TAG, "mTagButton is null! Cannot set click listener.");
            }

            // Apply settings after views are created
            applySettings();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            e.printStackTrace();
        }

        /*
         * If this Activity had stopped previously, its state was written the ORIGINAL_CONTENT
         * location in the saved Instance state. This gets the state.
         */
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    }

    /**
     * This method is called when the Activity is about to come to the foreground. This happens
     * when the Activity comes to the top of the task stack, OR when it is first starting.
     *
     * Moves to the first note in the list, sets an appropriate title for the action chosen by
     * the user, puts the note contents into the TextView, and saves the original text as a
     * backup.
     */
    @Override
    protected void onResume() {
        super.onResume();
        
        // Apply settings (background color, font size)
        applySettings();

        /*
         * mCursor is initialized, since onCreate() always precedes onResume for any running
         * process. This tests that it's not null, since it should always contain data.
         * 
         * IMPORTANT: Do NOT close managedQuery() cursors manually. They are managed by the Activity
         * and will be automatically closed when the Activity is destroyed. Closing them manually
         * causes crashes when the Activity restarts.
         */
        if (mUri != null) {
            try {
                // Check if cursor is valid, if not, get a new one
                boolean needNewCursor = (mCursor == null || mCursor.isClosed());
                
                if (needNewCursor) {
                    // Get a fresh cursor - managedQuery() will handle lifecycle
                    mCursor = managedQuery(
                            mUri,
                            PROJECTION,
                            null,
                            null,
                            null
                    );
                } else {
                    // If cursor exists but activity was paused (e.g. by TitleEditor dialog),
                    // we need to refresh it to show latest changes
                    mCursor.requery();
                    // Reset original title to null so it will be updated with new title
                    // This allows us to detect if title was changed after editing
                    mOriginalTitle = null;
                }

                if (mCursor != null && mCursor.moveToFirst()) {
                    // Modifies the window title for the Activity according to the current Activity state.
                    if (mState == STATE_EDIT) {
                        // Set the title of the Activity to include the note title
                        int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                        if (colTitleIndex >= 0) {
                            String title = mCursor.getString(colTitleIndex);
                            if (title != null) {
                                Resources res = getResources();
                                // String text = String.format(res.getString(R.string.title_edit), title);
                                setTitle(title); // Display only title without prefix
                                // Also update custom ActionBar title
                                if (mActionBarTitle != null) {
                                    mActionBarTitle.setText(title);
                                }
                                // Save original title for change detection
                                if (mOriginalTitle == null) {
                                    mOriginalTitle = title;
                                }
                            }
                        }
                    // Sets the title to "create" for inserts
                    } else if (mState == STATE_INSERT) {
                        setTitle(getText(R.string.title_create));
                        if (mActionBarTitle != null) {
                            mActionBarTitle.setText(getString(R.string.title_create));
                        }
                        mOriginalTitle = null; // New note has no original title
                    }

                    /*
                     * onResume() may have been called after the Activity lost focus (was paused).
                     * The user was either editing or creating a note when the Activity paused.
                     * The Activity should re-display the text that had been retrieved previously, but
                     * it should not move the cursor. This helps the user to continue editing or entering.
                     */

                    // Gets the note text from the Cursor and puts it in the TextView, but doesn't change
                    // the text cursor's position.
                    int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                    if (colNoteIndex >= 0 && mText != null) {
                        String note = mCursor.getString(colNoteIndex);
                        if (note != null) {
                            mText.setTextKeepState(note);
                            // Stores the original note text, to allow the user to revert changes.
                            if (mOriginalContent == null) {
                                mOriginalContent = note;
                            }
                        }
                    }

                    // Gets the tag text from the Cursor
                    if (mTagButton != null) {
                        int colTagIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TAG);
                        if (colTagIndex >= 0) {
                            String tag = mCursor.getString(colTagIndex);
                            if (tag != null && !tag.trim().isEmpty()) {
                                mCurrentTag = tag.trim();
                                mTagButton.setText(tag.trim());
                                // Save original tag for change detection
                                if (mOriginalTag == null) {
                                    mOriginalTag = tag.trim();
                                }
                            } else {
                                mCurrentTag = null;
                                mTagButton.setText(getString(R.string.tag_hint));
                                // Save original tag (empty) for change detection
                                if (mOriginalTag == null) {
                                    mOriginalTag = null;
                                }
                            }
                        }
                    }
                } else {
                    // Cursor is empty or invalid
                    setTitle(getText(R.string.error_title));
                    if (mActionBarTitle != null) {
                        mActionBarTitle.setText(getString(R.string.error_title));
                    }
                    if (mText != null) {
                        mText.setText(getText(R.string.error_message));
                    }
                }
            } catch (Exception e) {
                // Handle any exceptions during cursor operations
                Log.e(TAG, "Error in onResume", e);
                setTitle(getText(R.string.error_title));
                if (mActionBarTitle != null) {
                    mActionBarTitle.setText(getString(R.string.error_title));
                }
                if (mText != null) {
                    mText.setText(getText(R.string.error_message));
                }
            }

        /*
         * Something is wrong. The Cursor should always contain data. Report an error in the
         * note.
         */
        } else {
            setTitle(getText(R.string.error_title));
            if (mActionBarTitle != null) {
                mActionBarTitle.setText(getString(R.string.error_title));
            }
            if (mText != null) {
                mText.setText(getText(R.string.error_message));
            }
        }
    }

    /**
     * This method is called when an Activity loses focus during its normal operation, and is then
     * later on killed. The Activity has a chance to save its state so that the system can restore
     * it.
     *
     * Notice that this method isn't a normal part of the Activity lifecycle. It won't be called
     * if the user simply navigates away from the Activity.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
    }

    /**
     * This method is called when the Activity loses focus.
     *
     * For Activity objects that edit information, onPause() may be the one place where changes are
     * saved. The Android application model is predicated on the idea that "save" and "exit" aren't
     * required actions. When users navigate away from an Activity, they shouldn't have to go back
     * to it to complete their work. The act of going away should save everything and leave the
     * Activity in a state where Android can destroy it if necessary.
     *
     * If the user hasn't done anything, then this deletes or clears out the note, otherwise it
     * writes the user's work to the provider.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Skip auto-save if user explicitly chose "Don't save"
        if (mShouldSkipSave) {
            Log.d(TAG, "onPause: skipping auto-save because user chose 'Don't save'");
            mShouldSkipSave = false; // Reset flag
            return;
        }

        /*
         * Tests to see that the query operation didn't fail (see onCreate()). The Cursor object
         * will exist, even if no records were returned, unless the query failed because of some
         * exception or error.
         *
         * IMPORTANT: Do NOT close managedQuery() cursors here. They are managed by the Activity
         * and will be automatically closed when the Activity is destroyed.
         */
        if (mText != null && mUri != null) {

            // Get the current note text.
            String text = mText.getText().toString();
            int length = text.length();

            /*
             * If the Activity is in the midst of finishing and there is no text in the current
             * note, returns a result of CANCELED to the caller, and deletes the note. This is done
             * even if the note was being edited, the assumption being that the user wanted to
             * "clear out" (delete) the note.
             */
            if (isFinishing() && (length == 0)) {
                Log.d(TAG, "onPause: finishing with empty note, deleting");
                setResult(RESULT_CANCELED);
                deleteNote();
                return; // Exit early after deleting
            }

            /*
             * Writes the edits to the provider. The note has been edited if an existing note was
             * retrieved into the editor *or* if a new note was inserted. In the latter case,
             * onCreate() inserted a new empty note into the provider, and it is this new note
             * that is being edited.
             */
            if (mState == STATE_EDIT) {
                // Creates a map to contain the new values for the columns
                Log.d(TAG, "onPause: saving note in EDIT state, text length: " + length);
                updateNote(text, null);
            } else if (mState == STATE_INSERT) {
                Log.d(TAG, "onPause: saving note in INSERT state, text length: " + length);
                updateNote(text, text);
                mState = STATE_EDIT;
            }
        } else {
            if (mUri == null) {
                Log.w(TAG, "onPause: mUri is null, cannot save note");
            }
            if (mText == null) {
                Log.w(TAG, "onPause: mText is null, cannot save note");
            }
        }
    }

    /**
     * This method is called when the user clicks the device's Menu button the first time for
     * this Activity. Android passes in a Menu object that is populated with items.
     *
     * Builds the menus for editing and inserting, and adds in alternative actions that
     * registered themselves to handle the MIME types for this application.
     *
     * @param menu A Menu object to which items should be added.
     * @return True to display the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_options_menu, menu);

        // Removed addIntentOptions to prevent dynamic menu items (like Edit Title) from appearing
        // Title editing is now done by clicking on the title itself

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Check if note has changed and enable/disable the revert option
        try {
            if (mUri != null && mText != null && mCursor != null) {
                // Use existing cursor if available and valid
                try {
                    if (!mCursor.isClosed() && mCursor.moveToFirst()) {
                        int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                        if (colNoteIndex >= 0) {
                            String savedNote = mCursor.getString(colNoteIndex);
                            String currentNote = mText.getText().toString();
                            if (savedNote != null && savedNote.equals(currentNote)) {
                                MenuItem revertItem = menu.findItem(R.id.menu_revert);
                                if (revertItem != null) {
                                    revertItem.setVisible(false);
                                }
                            } else {
                                MenuItem revertItem = menu.findItem(R.id.menu_revert);
                                if (revertItem != null) {
                                    revertItem.setVisible(true);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error reading cursor in onPrepareOptionsMenu", e);
                    // Hide revert on error
                    MenuItem revertItem = menu.findItem(R.id.menu_revert);
                    if (revertItem != null) {
                        revertItem.setVisible(false);
                    }
                }
            } else {
                // Hide revert if URI or text is not available
                MenuItem revertItem = menu.findItem(R.id.menu_revert);
                if (revertItem != null) {
                    revertItem.setVisible(false);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onPrepareOptionsMenu", e);
            // Hide revert on error
            MenuItem revertItem = menu.findItem(R.id.menu_revert);
            if (revertItem != null) {
                revertItem.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This method is called when a menu item is selected. Android passes in the selected item.
     * The switch statement in this method calls the appropriate method to perform the action the
     * user chose.
     *
     * @param item The selected MenuItem
     * @return True to indicate that the item was processed, and no further work is necessary. False
     * to proceed to further processing as indicated in the MenuItem object.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        int id = item.getItemId();
        if(id== R.id.menu_save) {
            try {
                if (mText != null && mUri != null) {
                    String text = mText.getText().toString();
                    Log.d(TAG, "Menu save clicked, saving note, text length: " + text.length());
                    // For existing notes, preserve the title; for new notes, create title from text
                    if (mState == STATE_INSERT) {
                        updateNote(text, text);
                    } else {
                        updateNote(text, null);
                    }
                } else {
                    Log.w(TAG, "Menu save clicked but mText or mUri is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving note", e);
            }
            finish();
        } else if (id == R.id.menu_delete) {
            try {
                deleteNote();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting note", e);
            }
            finish();
        } else if (id == R.id.menu_revert) {
            try {
                cancelNote();
            } catch (Exception e) {
                Log.e(TAG, "Error reverting note", e);
            }
        } else if (id == R.id.menu_editor_settings) {
            // Launch editor settings activity
            startActivity(new Intent(this, EditorSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Check if the note has been modified (content, tag, or title)
     */
    private boolean hasChanges() {
        try {
            // Check if content has changed
            String currentContent = mText != null ? mText.getText().toString() : "";
            if (mOriginalContent == null) {
                mOriginalContent = "";
            }
            if (!currentContent.equals(mOriginalContent)) {
                return true;
            }
            
            // Check if tag has changed
            String currentTag = (mCurrentTag != null && !mCurrentTag.trim().isEmpty()) ? mCurrentTag.trim() : null;
            String originalTag = (mOriginalTag != null && !mOriginalTag.trim().isEmpty()) ? mOriginalTag.trim() : null;
            if (currentTag == null && originalTag != null) {
                return true;
            }
            if (currentTag != null && !currentTag.equals(originalTag)) {
                return true;
            }
            
            // Check if title has changed (only for existing notes)
            if (mState == STATE_EDIT && mUri != null && mCursor != null) {
                try {
                    if (!mCursor.isClosed() && mCursor.moveToFirst()) {
                        int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                        if (colTitleIndex >= 0) {
                            String currentTitle = mCursor.getString(colTitleIndex);
                            if (currentTitle != null && mOriginalTitle != null && !currentTitle.equals(mOriginalTitle)) {
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error checking title changes", e);
                }
            }
            
            // For new notes, if there's any content, it's considered a change
            if (mState == STATE_INSERT) {
                if (currentContent != null && !currentContent.trim().isEmpty()) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for changes", e);
        }
        return false;
    }
    
    /**
     * Handle back button press - check for changes and show confirmation dialog if needed
     */
    private void handleBackPressed() {
        if (hasChanges()) {
            // Show confirmation dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("保存更改");
            builder.setMessage("您有未保存的更改，是否要保存？");
            builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Save and finish
                    try {
                        if (mText != null && mUri != null) {
                            String text = mText.getText().toString();
                            if (mState == STATE_INSERT) {
                                updateNote(text, text);
                            } else {
                                updateNote(text, null);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving note on back", e);
                    }
                    finish();
                }
            });
            builder.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Set flag to skip auto-save in onPause()
                    mShouldSkipSave = true;
                    // Don't save, just finish
                    finish();
                }
            });
            builder.setCancelable(true);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // User cancelled, do nothing
                }
            });
            builder.show();
        } else {
            // No changes, just finish
            finish();
        }
    }
    
    /**
     * Override onBackPressed to use our custom handler
     */
    @Override
    public void onBackPressed() {
        handleBackPressed();
    }
    
    private void applySettings() {
        if (prefs == null) {
            return;
        }

        try {
            // Apply editor background color to root layout
            String editorBg = SettingsActivity.getEditorBackground(prefs);
            int colorRes = R.color.bg_white;
            if ("cream".equals(editorBg)) {
                colorRes = R.color.bg_cream;
            } else if ("blue".equals(editorBg)) {
                colorRes = R.color.bg_blue;
            } else if ("green".equals(editorBg)) {
                colorRes = R.color.bg_green;
            }
            
            // Apply background to the root layout of note_editor.xml
            if (mRootLayout != null) {
                try {
                    mRootLayout.setBackgroundColor(getResources().getColor(colorRes));
                    Log.d(TAG, "Applied background color: " + editorBg + " (colorRes=" + colorRes + ")");
                } catch (Exception e) {
                    Log.w(TAG, "Could not set background color", e);
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "mRootLayout is null, cannot set background color");
            }

            // Apply font size (use TypedValue.COMPLEX_UNIT_SP for sp units)
            if (mText != null) {
                int fontSize = SettingsActivity.getFontSize(prefs);
                mText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, fontSize);
                Log.d(TAG, "Applied font size: " + fontSize + "sp");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in applySettings", e);
            e.printStackTrace();
        }
    }

//BEGIN_INCLUDE(paste)
    /**
     * A helper method that replaces the note's data with the contents of the clipboard.
     */
    private final void performPaste() {

        // Gets a handle to the Clipboard Manager
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        // Gets a content resolver instance
        ContentResolver cr = getContentResolver();

        // Gets the clipboard data from the clipboard
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {

            String text=null;
            String title=null;

            // Gets the first item from the clipboard data
            ClipData.Item item = clip.getItemAt(0);

            // Tries to get the item's contents as a URI pointing to a note
            Uri uri = item.getUri();

            // Tests to see that the item actually is an URI, and that the URI
            // is a content URI pointing to a provider whose MIME type is the same
            // as the MIME type supported by the Note pad provider.
            if (uri != null && NotePad.Notes.CONTENT_ITEM_TYPE.equals(cr.getType(uri))) {

                // The clipboard holds a reference to data with a note MIME type. This copies it.
                Cursor orig = cr.query(
                        uri,            // URI for the content provider
                        PROJECTION,     // Get the columns referred to in the projection
                        null,           // No selection variables
                        null,           // No selection variables, so no criteria are needed
                        null            // Use the default sort order
                );

                // If the Cursor is not null, and it contains at least one record
                // (moveToFirst() returns true), then this gets the note data from it.
                if (orig != null) {
                    if (orig.moveToFirst()) {
                        int colNoteIndex = orig.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                        int colTitleIndex = orig.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                        if (colNoteIndex >= 0) {
                            text = orig.getString(colNoteIndex);
                        }
                        if (colTitleIndex >= 0) {
                            title = orig.getString(colTitleIndex);
                        }
                    }

                    // Closes the cursor.
                    orig.close();
                }
            }

            // If the contents of the clipboard wasn't a reference to a note, then
            // this converts whatever it is to text.
            if (text == null) {
                text = item.coerceToText(this).toString();
            }

            // Updates the current note with the retrieved title and text.
            updateNote(text, title);
        }
    }
//END_INCLUDE(paste)

    /**
     * Replaces the current note contents with the text and title provided as arguments.
     * @param text The new note contents to use.
     * @param title The new note title to use
     */
    private final void updateNote(String text, String title) {

        if (mUri == null) {
            Log.e(TAG, "updateNote called with null URI");
            return;
        }

        // Defensive defaults
        if (text == null) {
            text = "";
        }

        Log.d(TAG, "updateNote: URI=" + mUri + ", state=" + mState + ", text length=" + text.length() + ", title=" + title);

        // Sets up a map to contain values to be updated in the provider.
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
        
        // Add tag if available
        if (mCurrentTag != null && !mCurrentTag.trim().isEmpty()) {
            values.put(NotePad.Notes.COLUMN_NAME_TAG, mCurrentTag.trim());
        } else {
            values.put(NotePad.Notes.COLUMN_NAME_TAG, "");
        }

        // If the action is to insert a new note, this creates an initial title for it.
        if (mState == STATE_INSERT) {

            // If no title was provided as an argument, create one from the note text.
            if (title == null) {
                String safeText = text != null ? text : "";
  
                // Get the note's length
                int length = safeText.length();

                // Sets the title by getting a substring of the text that is 31 characters long
                // or the number of characters in the note plus one, whichever is smaller.
                if (length == 0) {
                    title = getString(R.string.title_create);
                } else {
                    title = safeText.substring(0, Math.min(30, length));
                }
  
                // If the resulting length is more than 30 characters, chops off any
                // trailing spaces
                if (length > 30) {
                    int lastSpace = title.lastIndexOf(' ');
                    if (lastSpace > 0) {
                        title = title.substring(0, lastSpace);
                    }
                }
            }
            // In the values map, sets the value of the title
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
            Log.d(TAG, "updateNote: Setting title for INSERT: " + title);
        } else if (title != null) {
            // In the values map, sets the value of the title
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
            Log.d(TAG, "updateNote: Setting title for EDIT: " + title);
        }

        // This puts the desired notes text into the map.
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);

        /*
         * Updates the provider with the new values in the map. The ListView is updated
         * automatically. The provider sets this up by setting the notification URI for
         * query Cursor objects to the incoming URI. The content resolver is thus
         * automatically notified when the Cursor for the URI changes, and the UI is
         * updated.
         * Note: This is being done on the UI thread. It will block the thread until the
         * update completes. In a sample app, going against a simple provider based on a
         * local database, the block will be momentary, but in a real app you should use
         * android.content.AsyncQueryHandler or android.os.AsyncTask.
         */
        try {
            int rowsUpdated = getContentResolver().update(
                    mUri,    // The URI for the record to update.
                    values,  // The map of column names and new values to apply to them.
                    null,    // No selection criteria are used, so no where columns are necessary.
                    null     // No where columns are used, so no where arguments are necessary.
            );
            Log.d(TAG, "updateNote: Update completed, rows updated: " + rowsUpdated);
            if (rowsUpdated == 0) {
                Log.w(TAG, "updateNote: No rows were updated! This might indicate a problem.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating note", e);
            // Fallback: try updating without the tag column if it fails (e.g. database mismatch)
            if (values.containsKey(NotePad.Notes.COLUMN_NAME_TAG)) {
                values.remove(NotePad.Notes.COLUMN_NAME_TAG);
                try {
                    int rowsUpdated = getContentResolver().update(mUri, values, null, null);
                    Log.i(TAG, "Update successful after removing tag column, rows updated: " + rowsUpdated);
                } catch (Exception e2) {
                    Log.e(TAG, "Error updating note even without tag", e2);
                }
            }
        }


    }

    /**
     * This helper method cancels the work done on a note.  It deletes the note if it was
     * newly created, or reverts to the original text of the note i
     */
    private final void cancelNote() {
        try {
            // Note: Do NOT close managedQuery() cursor here - it's managed by Activity

            if (mState == STATE_EDIT) {
                // Put the original note text back into the database
                if (mUri != null && mOriginalContent != null) {
                    ContentValues values = new ContentValues();
                    values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
                    values.put(NotePad.Notes.COLUMN_NAME_NOTE, mOriginalContent);
                    try {
                        getContentResolver().update(mUri, values, null, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reverting note", e);
                    }
                }
            } else if (mState == STATE_INSERT) {
                // We inserted an empty note, make sure to delete it
                deleteNote();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in cancelNote", e);
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Take care of deleting a note.  Simply deletes the entry.
     */
    private final void deleteNote() {
        try {
            // Note: Do NOT close managedQuery() cursor here - it's managed by Activity
            if (mUri != null) {
                try {
                    getContentResolver().delete(mUri, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting note", e);
                }
            }
            if (mText != null) {
                mText.setText("");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteNote", e);
        }
    }
    
    /**
     * Shows a dialog to select or create a tag
     */
    private void showTagSelectionDialog() {
        Log.d(TAG, "showTagSelectionDialog called");
        
        // Check if activity is still valid
        if (isFinishing()) {
            return;
        }
        
        // Get all unique tags from database
        Cursor tagCursor = null;
        try {
            // Query all notes with tags, then deduplicate in Java
            tagCursor = getContentResolver().query(
                NotePad.Notes.CONTENT_URI,
                new String[] { NotePad.Notes.COLUMN_NAME_TAG },
                NotePad.Notes.COLUMN_NAME_TAG + " IS NOT NULL AND " + NotePad.Notes.COLUMN_NAME_TAG + " != ''",
                null,
                NotePad.Notes.COLUMN_NAME_TAG + " ASC"
            );
            
            // Build tag list with deduplication using Set
            Set<String> tagSet = new HashSet<String>();
            
            if (tagCursor != null) {
                int tagColumnIndex = tagCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TAG);
                if (tagColumnIndex >= 0) {
                    while (tagCursor.moveToNext()) {
                        String tag = tagCursor.getString(tagColumnIndex);
                        if (tag != null && !tag.trim().isEmpty()) {
                            String[] parts = tag.split(",");
                            for (String t : parts) {
                                if (t != null && !t.trim().isEmpty()) {
                                    tagSet.add(t.trim());
                        }
                    }
                }
                    }
                }
            }
            
            // Convert Set to sorted List
            List<String> tagList = new ArrayList<String>(tagSet);
            Collections.sort(tagList);
            tagList.add(0, getString(R.string.no_tag)); // "无标签" option at the beginning
            tagList.add(getString(R.string.new_tag));   // "新建标签" option at the end
            
            final String[] tags = tagList.toArray(new String[tagList.size()]);
            
            // Create dialog using custom layout
            View view = getLayoutInflater().inflate(R.layout.dialog_select_tag, null);
            final android.widget.ListView listView = (android.widget.ListView) view.findViewById(R.id.tag_list);
            Button cancelButton = (Button) view.findViewById(R.id.btn_cancel);
            
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.RoundedAlertDialog);
            builder.setView(view);
            final android.app.AlertDialog dialog = builder.create();
                
            // Setup adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    R.layout.dialog_list_item,
                    tags
                );
            listView.setAdapter(adapter);
                
            listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                    @Override
                public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        dialog.dismiss();
                        
                    if (position == tags.length - 1) {
                            // Last item is "新建标签"
                            showNewTagDialog();
                    } else if (position == 0) {
                            // First item is "无标签"
                            mCurrentTag = null;
                            if (mTagButton != null) {
                                mTagButton.setText(getString(R.string.tag_hint));
                            }
                        } else {
                            // Selected an existing tag
                        mCurrentTag = tags[position];
                            if (mTagButton != null) {
                                mTagButton.setText(mCurrentTag);
                            }
                        }
                    }
                });
            
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            
            // Set window background to transparent to avoid default dialog frame
                if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                }
                
                dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing tag selection dialog", e);
            e.printStackTrace();
            android.widget.Toast.makeText(this, "查询标签时出错: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        } finally {
            if (tagCursor != null) {
                tagCursor.close();
            }
        }
    }
    
    /**
     * Shows a dialog to create a new tag
     */
    private void showNewTagDialog() {
        try {
            // Inflate custom layout which includes the title
            View view = getLayoutInflater().inflate(R.layout.dialog_new_tag, null);
            final EditText input = (EditText) view.findViewById(R.id.new_tag_input);
            Button okButton = (Button) view.findViewById(R.id.btn_ok);
            Button cancelButton = (Button) view.findViewById(R.id.btn_cancel);
            
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.RoundedAlertDialog);
            
            // Set the view
            builder.setView(view);
            
            final android.app.AlertDialog dialog = builder.create();
            
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newTag = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!newTag.isEmpty()) {
                        mCurrentTag = newTag;
                        if (mTagButton != null) {
                            mTagButton.setText(mCurrentTag);
                        }
                        Log.d(TAG, "New tag created: " + mCurrentTag);
                    }
                    dialog.dismiss();
                }
            });
            
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            
            // Set window background to transparent to avoid default dialog frame
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            }
            
            dialog.show();
            Log.d(TAG, "New tag dialog shown");
        } catch (Exception e) {
            Log.e(TAG, "Error showing new tag dialog", e);
            e.printStackTrace();
        }
    }
}
