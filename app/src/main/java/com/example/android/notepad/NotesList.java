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

import com.example.android.notepad.NotePad;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;


/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the incoming Intent if there is one, otherwise it defaults to displaying the
 * contents of the {@link NotePadProvider}.
 *
 * NOTE: Notice that the provider operations in this Activity are taking place on the UI thread.
 * This is not a good practice. It is only done here to make the code more readable. A real
 * application should use the {@link android.content.AsyncQueryHandler} or
 * {@link android.os.AsyncTask} object to perform operations asynchronously on a separate thread.
 */
public class NotesList extends ListActivity {

    // For logging and debugging
    private static final String TAG = "NotesList";
    
    // Track last sort order to refresh list when it changes
    private String mLastSortOrder;
    
    // Key for saving tag filter state
    private static final String STATE_TAG_FILTER = "tag_filter";
    private static final String PREF_TAG_FILTER = "pref_current_tag_filter";

    /**
     * The columns needed by the cursor adapter
     */
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2
            NotePad.Notes.COLUMN_NAME_TAG, // 3
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    
    /** The index of the modification date column */
    private static final int COLUMN_INDEX_MODIFIED = 2;
    
    /** The cursor adapter */
    private SimpleCursorAdapter mAdapter;
    
    /** Shared preferences */
    private SharedPreferences prefs;
    
    /** Current search selection */
    private String mSelection;
    private String[] mSelectionArgs;
    
    /** Current tag filter */
    private String mCurrentTagFilter;
    
    /** Gesture detector for swipe gestures */
    private GestureDetector gestureDetector;
    
    /** Whether list items are currently hidden (showing only background) */
    private boolean isListHidden = false;
    
    /** Minimum swipe distance in pixels */
    private static final int SWIPE_MIN_DISTANCE = 50; // Reduced for easier detection
    
    /** Minimum swipe velocity */
    private static final int SWIPE_THRESHOLD_VELOCITY = 100; // Reduced for easier detection
    
    /**
     * Creates a SimpleCursorAdapter with tag display support
     */
    private SimpleCursorAdapter createNotesAdapter(Cursor cursor) {
        String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };
        int[] viewIDs = { android.R.id.text1, android.R.id.text2 };
        
        return new SimpleCursorAdapter(
            this,
            R.layout.noteslist_item,
            cursor,
            dataColumns,
            viewIDs
        ) {
            @Override
            public void setViewText(TextView v, String text) {
                if (v.getId() == android.R.id.text2) {
                    try {
                        long timestamp = Long.parseLong(text);
                        Date date = new Date(timestamp);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        v.setText(sdf.format(date));
                    } catch (NumberFormatException e) {
                        v.setText(text);
                    }
                } else {
                    super.setViewText(v, text);
                }
            }
            
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                
                // Display tag if available and make it clickable for filtering
                View tagViewObj = view.findViewById(R.id.tag_text);
                TextView tagView = (tagViewObj instanceof TextView) ? (TextView) tagViewObj : null;
                if (tagView != null) {
                    int tagIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TAG);
                    if (tagIndex >= 0) {
                        String tag = cursor.getString(tagIndex);
                        if (tag != null && !tag.trim().isEmpty()) {
                            tagView.setText(tag.trim());
                            tagView.setVisibility(View.VISIBLE);
                            // Make tag clickable to filter by tag
                            final String tagValue = tag.trim();
                            tagView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    filterByTag(tagValue);
                                }
                            });
                        } else {
                            tagView.setVisibility(View.GONE);
                        }
                    } else {
                        tagView.setVisibility(View.GONE);
                    }
                }
            }
        };
    }

    /**
     * onCreate is called when Android starts this Activity from scratch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        applyBackgroundColor();
        
        // Setup custom ActionBar
        if (getActionBar() != null) {
            getActionBar().setDisplayShowCustomEnabled(true);
            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowHomeEnabled(false);
            
            View customView = getLayoutInflater().inflate(R.layout.custom_actionbar_list, null);
            getActionBar().setCustomView(customView);
            
            // Initialize title
            TextView titleView = (TextView) customView.findViewById(R.id.actionbar_title);
            if (titleView != null) {
                titleView.setText(getString(R.string.app_name));
            }
        }

        // The user does not need to hold down the key to use menu shortcuts.
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        /* If no data is given in the Intent that started this Activity, then this Activity
         * was started when the intent filter matched a MAIN action. We should use the default
         * provider URI.
         */
        // Gets the intent that started this Activity.
        Intent intent = getIntent();

        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of notes.
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        
        // Restore tag filter state if Activity was recreated
        if (savedInstanceState != null) {
            mCurrentTagFilter = savedInstanceState.getString(STATE_TAG_FILTER);
            Log.d(TAG, "onCreate: Restored tag filter from saved state = " + mCurrentTagFilter);
        }
        
        // Also try to restore from SharedPreferences (more reliable for Activity recreation)
        if (mCurrentTagFilter == null && prefs != null) {
            mCurrentTagFilter = prefs.getString(PREF_TAG_FILTER, null);
            Log.d(TAG, "onCreate: Restored tag filter from SharedPreferences = " + mCurrentTagFilter);
        }

        /*
         * Sets the callback for context menu activation for the ListView. The listener is set
         * to be this Activity. The effect is that context menus are enabled for items in the
         * ListView, and the context menu is handled by a method in NotesList.
         */
        getListView().setOnCreateContextMenuListener(this);
        
        // Setup gesture detector for swipe gestures
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }
                
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                
                // Check if horizontal swipe is dominant
                // Allow swipe even if vertical movement exists, as long as horizontal is larger
                if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
                    // Check velocity OR distance (make it easier to trigger)
                    if (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY || Math.abs(deltaX) > SWIPE_MIN_DISTANCE * 2) {
                        if (deltaX > 0) {
                            // Swipe right - show list
                            Log.d(TAG, "Swipe right detected, showing list");
                            showList();
                            return true;
                        } else {
                            // Swipe left - hide list
                            Log.d(TAG, "Swipe left detected, hiding list");
                            hideList();
                            return true;
                        }
                    }
                }
                
                return false;
            }
        });
        
        // Set touch listener on ListView
        getListView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Let gesture detector handle the touch event
                // Return false to allow ListView to handle clicks and scrolling normally
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        
        // Also set touch listener on root view to detect swipes when list is hidden
        // We need to intercept touch events when list is hidden
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Always let gesture detector process the event
                    // It will handle swipe detection regardless of list state
                    boolean handled = gestureDetector.onTouchEvent(event);
                    // If list is hidden and we detected a swipe, consume the event
                    if (isListHidden && handled) {
                        return true;
                    }
                    return false;
                }
            });
        }
        
        // Also override onTouchEvent to catch touches when list is hidden
        // This ensures we can detect swipes even when ListView is invisible

        // Handle search query if present
        handleIntent(intent);
        
        // Get sort order from preferences
        String sortOrder = getSortOrder();
        mLastSortOrder = sortOrder;

        /* Performs a managed query. The Activity handles closing and requerying the cursor
         * when needed.
         *
         * Please see the introductory note about performing provider operations on the UI thread.
         */
        Cursor cursor = managedQuery(
            getIntent().getData(),            // Use the default content URI for the provider.
            PROJECTION,                       // Return the note ID, title, and modified date for each note.
            mSelection,                       // Where clause for search
            mSelectionArgs,                   // Where clause arguments
            sortOrder                         // Use the sort order from preferences
        );

        /*
         * The following two arrays create a "map" between columns in the cursor and view IDs
         * for items in the ListView. Each element in the dataColumns array represents
         * a column name; each element in the viewID array represents the ID of a View.
         * The SimpleCursorAdapter maps them in ascending order to determine where each column
         * value will appear in the ListView.
         */

        // Creates the backing adapter for the ListView with tag display support
        mAdapter = createNotesAdapter(cursor);

        // Sets the ListView's adapter to be the cursor adapter that was just created.
        setListAdapter(mAdapter);
        
        // Update title to reflect current filter/search state
        updateTitle();
    }

    /**
     * Called when a new Intent is delivered to this activity.
     * This handles search queries that come in while the activity is already running.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: Action = " + intent.getAction() + ", Current tag filter = " + mCurrentTagFilter);
        
        // Restore tag filter from SharedPreferences if not already set
        if (mCurrentTagFilter == null && prefs != null) {
            mCurrentTagFilter = prefs.getString(PREF_TAG_FILTER, null);
            Log.d(TAG, "onNewIntent: Restored tag filter from SharedPreferences = " + mCurrentTagFilter);
        }
        
        setIntent(intent);
        
        // Ensure data URI is set
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        
        // Handle search query if present (this will preserve mCurrentTagFilter if it exists)
        handleIntent(intent);
        
        // Get sort order from preferences
        String sortOrder = getSortOrder();
        
        Log.d(TAG, "onNewIntent: Final selection = " + mSelection + ", Args = " + java.util.Arrays.toString(mSelectionArgs));
        
        // Requery the cursor with the new search parameters
        Cursor cursor = managedQuery(
            intent.getData(),
            PROJECTION,
            mSelection,
            mSelectionArgs,
            sortOrder
        );
        
        // Recreate adapter with new cursor using createNotesAdapter to preserve tag display
        if (cursor != null) {
            Log.d(TAG, "onNewIntent: Query returned " + cursor.getCount() + " results");
            mAdapter = createNotesAdapter(cursor);
            setListAdapter(mAdapter);
        }
        
        // Update title to reflect current filter/search state
        updateTitle();
        
        // Invalidate menu to update clear search button visibility
        invalidateOptionsMenu();
    }
    
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "handleIntent: Search query = " + query + ", Current tag filter = " + mCurrentTagFilter);
            
            if (query != null && !query.isEmpty()) {
                String searchPattern = "%" + query + "%";
                
                // If we have a current tag filter, combine it with search
                // Search will be limited to notes with the current tag
                if (mCurrentTagFilter != null && !mCurrentTagFilter.isEmpty()) {
                    mSelection = "(" + NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + 
                                NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?) AND " +
                                NotePad.Notes.COLUMN_NAME_TAG + " = ?";
                    mSelectionArgs = new String[] { searchPattern, searchPattern, mCurrentTagFilter };
                    Log.d(TAG, "handleIntent: Combined search with tag filter. Selection = " + mSelection);
                } else {
                    // Search in title, note content, and tag without tag filter
                    mSelection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + 
                                NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ? OR " +
                                NotePad.Notes.COLUMN_NAME_TAG + " LIKE ?";
                    mSelectionArgs = new String[] { searchPattern, searchPattern, searchPattern };
                    Log.d(TAG, "handleIntent: Search without tag filter. Selection = " + mSelection);
                }
            } else {
                // Empty query - restore tag filter if exists
                if (mCurrentTagFilter != null && !mCurrentTagFilter.isEmpty()) {
                    // Restore tag filter if query is empty
                    mSelection = NotePad.Notes.COLUMN_NAME_TAG + " = ?";
                    mSelectionArgs = new String[] { mCurrentTagFilter };
                    Log.d(TAG, "handleIntent: Empty query, restoring tag filter");
                } else {
                    mSelection = null;
                    mSelectionArgs = null;
                    Log.d(TAG, "handleIntent: Empty query, no tag filter");
                }
            }
        } else {
            // Not a search intent, preserve tag filter if exists
            if (mCurrentTagFilter != null && !mCurrentTagFilter.isEmpty()) {
                mSelection = NotePad.Notes.COLUMN_NAME_TAG + " = ?";
                mSelectionArgs = new String[] { mCurrentTagFilter };
                Log.d(TAG, "handleIntent: Not search intent, preserving tag filter = " + mCurrentTagFilter);
            } else {
                mSelection = null;
                mSelectionArgs = null;
            }
        }
    }
    
    /**
     * Updates the activity title based on current filter/search state
     */
    private void updateTitle() {
        Intent intent = getIntent();
        String query = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
        
        String titleText;
        String baseTitle = getString(R.string.app_name); // Use app name instead of title_notes_list
        if (mCurrentTagFilter != null && !mCurrentTagFilter.isEmpty() && query != null && !query.isEmpty()) {
            // Both tag filter and search active
            titleText = baseTitle + " - " + mCurrentTagFilter + " (" + query + ")";
        } else if (mCurrentTagFilter != null && !mCurrentTagFilter.isEmpty()) {
            // Only tag filter active
            titleText = baseTitle + " - " + mCurrentTagFilter;
        } else if (query != null && !query.isEmpty()) {
            // Only search active
            titleText = baseTitle + " - " + query;
        } else {
            // No filter or search
            titleText = baseTitle;
        }
        
        // Update both system title and custom ActionBar title
        setTitle(titleText);
        
        // Update custom ActionBar title TextView if it exists
        if (getActionBar() != null && getActionBar().getCustomView() != null) {
            View customView = getActionBar().getCustomView();
            TextView titleView = (TextView) customView.findViewById(R.id.actionbar_title);
            if (titleView != null) {
                titleView.setText(titleText);
            }
        }
    }
    
    /**
     * Filter notes by tag
     */
    private void filterByTag(String tag) {
        Log.d(TAG, "filterByTag: Setting tag filter to = " + tag);
        mCurrentTagFilter = tag;
        
        // Save to SharedPreferences for persistence across Activity recreation
        if (prefs != null) {
            prefs.edit().putString(PREF_TAG_FILTER, tag).apply();
            Log.d(TAG, "filterByTag: Saved tag filter to SharedPreferences");
        }
        
        mSelection = NotePad.Notes.COLUMN_NAME_TAG + " = ?";
        mSelectionArgs = new String[] { tag };
        
        // Clear any existing search intent
        Intent intent = new Intent(Intent.ACTION_VIEW, NotePad.Notes.CONTENT_URI);
        setIntent(intent);
        
        // Reload notes with tag filter
        String sortOrder = getSortOrder();
        Cursor cursor = managedQuery(
            NotePad.Notes.CONTENT_URI,
            PROJECTION,
            mSelection,
            mSelectionArgs,
            sortOrder
        );
        
        if (cursor != null) {
            Log.d(TAG, "filterByTag: Query returned " + cursor.getCount() + " results for tag = " + tag);
            mAdapter = createNotesAdapter(cursor);
            setListAdapter(mAdapter);
        }
        
        // Update title
        updateTitle();
        
        // Invalidate menu to show clear filter option
        invalidateOptionsMenu();
    }
    
    /**
     * Clears the current search/filter and reloads all notes
     */
    private void clearSearch() {
        // Clear search and filter parameters
        mSelection = null;
        mSelectionArgs = null;
        mCurrentTagFilter = null;
        
        // Clear from SharedPreferences
        if (prefs != null) {
            prefs.edit().remove(PREF_TAG_FILTER).apply();
            Log.d(TAG, "clearSearch: Removed tag filter from SharedPreferences");
        }
        
        // Clear the search intent
        Intent intent = new Intent(Intent.ACTION_VIEW, NotePad.Notes.CONTENT_URI);
        setIntent(intent);
        
        // Reload all notes
        String sortOrder = getSortOrder();
        Cursor cursor = managedQuery(
            NotePad.Notes.CONTENT_URI,
            PROJECTION,
            null,  // No selection (show all notes)
            null,  // No selection args
            sortOrder
        );
        
        // Update adapter with new cursor
        if (cursor != null) {
            mAdapter = createNotesAdapter(cursor);
            setListAdapter(mAdapter);
        }
        
        // Update title
        updateTitle();
        
        // Invalidate options menu to update the clear search button visibility
        invalidateOptionsMenu();
    }
    
    /**
     * Called when the user clicks the device's Menu button the first time for
     * this Activity. Android passes in a Menu object that is populated with items.
     *
     * Sets up a menu that provides the Insert option plus a list of alternative actions for
     * this Activity. Other applications that want to handle notes can "register" themselves in
     * Android by providing an intent filter that includes the category ALTERNATIVE and the
     * mimeTYpe NotePad.Notes.CONTENT_TYPE. If they do this, the code in onCreateOptionsMenu()
     * will add the Activity that contains the intent filter to its list of options. In effect,
     * the menu will offer the user other applications that can handle notes.
     * @param menu A Menu object, to which menu items should be added.
     * @return True, always. The menu should be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Show/hide clear search button based on whether we're in search or filter mode
        MenuItem clearSearchItem = menu.findItem(R.id.menu_clear_search);
        if (clearSearchItem != null) {
            // Show clear search button when there's an active search or tag filter
            boolean isSearching = (mSelection != null && mSelectionArgs != null);
            clearSearchItem.setVisible(isSearching);
        }

        // The paste menu item is enabled if there is data on the clipboard.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);


        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

        // If the clipboard contains an item, enables the Paste option on the menu.
        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            // If the clipboard is empty, disables the menu's Paste option.
            mPasteItem.setEnabled(false);
        }

        // Gets the number of notes currently being displayed.
        final boolean haveItems = getListAdapter().getCount() > 0;

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {

            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Creates an array of Intents with one element. This will be used to send an Intent
            // based on the selected menu item.
            Intent[] specifics = new Intent[1];

            // Sets the Intent in the array to be an EDIT action on the URI of the selected note.
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            // Creates an array of menu items with one element. This will contain the EDIT option.
            MenuItem[] items = new MenuItem[1];

            // Creates an Intent with no specific action, using the URI of the selected note.
            Intent intent = new Intent(null, uri);

            /* Adds the category ALTERNATIVE to the Intent, with the note ID URI as its
             * data. This prepares the Intent as a place to group alternative options in the
             * menu.
             */
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

            /*
             * Add alternatives to the menu
             */
            // menu.addIntentOptions(
            //    Menu.CATEGORY_ALTERNATIVE,  // Add the Intents as options in the alternatives group.
            //    Menu.NONE,                  // A unique item ID is not required.
            //    Menu.NONE,                  // The alternatives don't need to be in order.
            //    null,                       // The caller's name is not excluded from the group.
            //    specifics,                  // These specific options must appear first.
            //    intent,                     // These Intent objects map to the options in specifics.
            //    Menu.NONE,                  // No flags are required.
            //    items                       // The menu items generated from the specifics-to-
            //                                // Intents mapping
            // );
                // If the Edit menu item exists, adds shortcuts for it.
                if (items[0] != null) {

                    // Sets the Edit menu item shortcut to numeric "1", letter "e"
                    items[0].setShortcut('1', 'e');
                }
            } else {
                // If the list is empty, removes any existing alternative actions from the menu
                menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
            }

        // Displays the menu
        return true;
    }

    /**
     * This method is called when the user selects an option from the menu, but no item
     * in the list is selected. If the option was INSERT, then a new Intent is sent out with action
     * ACTION_INSERT. The data from the incoming Intent is put into the new Intent. In effect,
     * this triggers the NoteEditor activity in the NotePad application.
     *
     * If the item was not INSERT, then most likely it was an alternative option from another
     * application. The parent method is called to process the item.
     * @param item The menu item that was selected by the user
     * @return True, if the INSERT menu item was selected; otherwise, the result of calling
     * the parent method.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add) {
            /*
             * Launches a new Activity using an Intent. The intent filter for the Activity
             * has to have action ACTION_INSERT. No category is set, so DEFAULT is assumed.
             * In effect, this starts the NoteEditor Activity in NotePad.
             */
            startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            return true;
        } else if (item.getItemId() == R.id.menu_paste) {
            /*
             * Launches a new Activity using an Intent. The intent filter for the Activity
             * has to have action ACTION_PASTE. No category is set, so DEFAULT is assumed.
             * In effect, this starts the NoteEditor Activity in NotePad.
             */
            startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
            return true;
        } else if (item.getItemId() == R.id.menu_search) {
            // Launch search activity
            onSearchRequested();
            return true;
        } else if (item.getItemId() == R.id.menu_clear_search) {
            // Clear search and return to all notes
            clearSearch();
            return true;
        } else if (item.getItemId() == R.id.menu_settings) {
            // Launch settings activity
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from settings
        try {
            if (prefs != null) {
                try {
                    applyBackgroundColor();
                } catch (Exception e) {
                    Log.e(TAG, "Error applying background color", e);
                }
                
                // Check if sort order changed
                String sortOrder = getSortOrder();
                boolean sortOrderChanged = mLastSortOrder != null && !mLastSortOrder.equals(sortOrder);
                mLastSortOrder = sortOrder;
                
                // IMPORTANT: Do NOT call changeCursor() or swapCursor() on managedQuery cursors
                // in onResume() as it causes crashes when Activity restarts. The managedQuery()
                // cursors are automatically managed by the Activity and will be updated when needed.
                // If we need to refresh the list, we should let the ContentProvider's notification
                // system handle it, or restart the activity.
                // For now, we only refresh if the adapter's cursor is null or closed OR if sort order changed.
                if (mAdapter != null) {
                    Cursor currentCursor = mAdapter.getCursor();
                    if (currentCursor == null || currentCursor.isClosed() || sortOrderChanged) {
                        // Only recreate adapter if cursor is invalid or sort order changed
                        Uri dataUri = getIntent().getData();
                        if (dataUri == null) {
                            dataUri = NotePad.Notes.CONTENT_URI;
                        }
                        Cursor cursor = null;
                        try {
                            cursor = managedQuery(
                                dataUri,
                                PROJECTION,
                                mSelection,
                                mSelectionArgs,
                                sortOrder
                            );
                        } catch (Exception e) {
                            Log.e(TAG, "Error querying notes in onResume", e);
                        }
                        if (cursor != null) {
                            // Recreate adapter with new cursor instead of changing cursor
                            String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };
                            int[] viewIDs = { android.R.id.text1, android.R.id.text2 };
                            mAdapter = createNotesAdapter(cursor);
                            setListAdapter(mAdapter);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save tag filter state
        if (mCurrentTagFilter != null) {
            outState.putString(STATE_TAG_FILTER, mCurrentTagFilter);
            Log.d(TAG, "onSaveInstanceState: Saved tag filter = " + mCurrentTagFilter);
        }
    }
    
    private void applyBackgroundColor() {
        if (prefs == null) {
            return;
        }
        
        // Check if background image is set
        String imagePath = prefs.getString(SettingsActivity.PREF_BACKGROUND_IMAGE, null);
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    // Get screen dimensions
                    android.util.DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    int screenWidth = displayMetrics.widthPixels;
                    int screenHeight = displayMetrics.heightPixels;
                    
                    // First, get image dimensions
                    android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    android.graphics.BitmapFactory.decodeFile(imagePath, options);
                    
                    int imageWidth = options.outWidth;
                    int imageHeight = options.outHeight;
                    
                    // Image is saved at 2x screen resolution, so we need to scale down for display
                    // Calculate sample size: if image is 2x screen size, use sampleSize=2
                    int sampleSize = 1;
                    // Target is screen size, but allow some flexibility for different aspect ratios
                    int targetWidth = screenWidth;
                    int targetHeight = screenHeight;
                    
                    if (imageWidth > targetWidth || imageHeight > targetHeight) {
                        // Calculate sample size to get close to screen size
                        int widthRatio = (int) Math.ceil((float) imageWidth / targetWidth);
                        int heightRatio = (int) Math.ceil((float) imageHeight / targetHeight);
                        sampleSize = Math.max(widthRatio, heightRatio);
                    }
                    
                    // Load bitmap with high quality settings
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = sampleSize;
                    options.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888; // Highest quality
                    options.inDither = true; // Better color transitions
                    options.inPurgeable = false; // Don't allow system to purge (better quality)
                    options.inInputShareable = false;
                    
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imagePath, options);
                    if (bitmap != null) {
                        // Final scale to exact screen size if needed (should be minimal after sampling)
                        int bitmapWidth = bitmap.getWidth();
                        int bitmapHeight = bitmap.getHeight();
                        
                        android.graphics.Bitmap finalBitmap = bitmap;
                        if (bitmapWidth != screenWidth || bitmapHeight != screenHeight) {
                            // Use high-quality scaling with filtering
                            // Scale to fill screen while maintaining aspect ratio (center crop effect)
                            float scaleX = (float) screenWidth / bitmapWidth;
                            float scaleY = (float) screenHeight / bitmapHeight;
                            float scale = Math.max(scaleX, scaleY); // Use max to fill entire screen
                            
                            int scaledWidth = (int) (bitmapWidth * scale);
                            int scaledHeight = (int) (bitmapHeight * scale);
                            
                            // Create scaled bitmap with high-quality filtering
                            finalBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
                            
                            // If scaled, crop to screen size (center crop)
                            if (scaledWidth > screenWidth || scaledHeight > screenHeight) {
                                int x = (scaledWidth - screenWidth) / 2;
                                int y = (scaledHeight - screenHeight) / 2;
                                android.graphics.Bitmap croppedBitmap = android.graphics.Bitmap.createBitmap(finalBitmap, x, y, screenWidth, screenHeight);
                                if (finalBitmap != bitmap) {
                                    finalBitmap.recycle();
                                }
                                finalBitmap = croppedBitmap;
                            }
                            
                            // Recycle original if different
                            if (finalBitmap != bitmap) {
                                bitmap.recycle();
                            }
                        }
                        
                        // Use finalBitmap instead of scaledBitmap
                        android.graphics.Bitmap scaledBitmap = finalBitmap;
                        
                        // Create drawable with the scaled bitmap
                        android.graphics.drawable.BitmapDrawable drawable = new android.graphics.drawable.BitmapDrawable(getResources(), scaledBitmap);
                        
                        // Apply to window background first (this is the base layer)
                        if (getWindow() != null) {
                            getWindow().setBackgroundDrawable(drawable);
                        }
                        
                        // Apply to the root content view (this covers the entire activity)
                        View rootView = findViewById(android.R.id.content);
                        if (rootView != null) {
                            rootView.setBackground(drawable);
                            // Also try to get the parent and set background there
                            ViewGroup parent = (ViewGroup) rootView.getParent();
                            if (parent != null) {
                                parent.setBackground(drawable);
                            }
                        }
                        
                        // Apply to ListView and set cache color hint to transparent
                        ListView listView = getListView();
                        if (listView != null) {
                            listView.setBackground(drawable);
                            listView.setCacheColorHint(android.graphics.Color.TRANSPARENT);
                            listView.setScrollingCacheEnabled(false);
                            
                            // Also apply to ListView's parent if it exists
                            ViewGroup listParent = (ViewGroup) listView.getParent();
                            if (listParent != null) {
                                listParent.setBackground(drawable);
                            }
                        }
                        
                        return;
                    }
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error loading background image", e);
                e.printStackTrace();
            }
        }
        
        // Fall back to color background
        String bgColor = SettingsActivity.getBackgroundColor(prefs);
        int colorRes = R.color.bg_white;
        if ("cream".equals(bgColor)) {
            colorRes = R.color.bg_cream;
        } else if ("blue".equals(bgColor)) {
            colorRes = R.color.bg_blue;
        } else if ("green".equals(bgColor)) {
            colorRes = R.color.bg_green;
        }
        int backgroundColor = getResources().getColor(colorRes);
        
        // Apply to window background first
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(backgroundColor));
        }
        
        // Apply to root view background
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setBackgroundColor(backgroundColor);
            // Also try to get the parent and set background there
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.setBackgroundColor(backgroundColor);
            }
        }
        
        // Apply to ListView
        ListView listView = getListView();
        if (listView != null) {
            listView.setBackgroundColor(backgroundColor);
            listView.setCacheColorHint(android.graphics.Color.TRANSPARENT);
            listView.setScrollingCacheEnabled(false);
            
            // Also apply to ListView's parent if it exists
            ViewGroup listParent = (ViewGroup) listView.getParent();
            if (listParent != null) {
                listParent.setBackgroundColor(backgroundColor);
            }
        }
    }
    
    private String getSortOrder() {
        if (prefs == null) {
            return NotePad.Notes.DEFAULT_SORT_ORDER;
        }
        String sortOrderPref = SettingsActivity.getSortOrder(prefs);
        switch (sortOrderPref) {
            case "modified_asc":
                return NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " ASC";
            case "modified_desc":
            default:
                return NotePad.Notes.DEFAULT_SORT_ORDER;
        }
    }
    
    /**
     * Hide list items to show only background with smooth animation
     */
    private void hideList() {
        if (isListHidden) {
            return; // Already hidden
        }
        
        ListView listView = getListView();
        if (listView == null) {
            return;
        }
        
        // Create smooth slide and fade out animation
        AnimationSet animationSet = new AnimationSet(true);
        
        // Slide to the left
        TranslateAnimation slideOut = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        );
        slideOut.setDuration(400);
        slideOut.setInterpolator(new DecelerateInterpolator(1.5f)); // Smooth deceleration
        
        // Fade out
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(400);
        
        animationSet.addAnimation(slideOut);
        animationSet.addAnimation(fadeOut);
        animationSet.setFillAfter(true);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                ListView listView = getListView();
                if (listView != null) {
                    listView.setVisibility(View.INVISIBLE);
                    listView.clearAnimation();
                    // Set translation and alpha to keep it hidden
                    listView.setTranslationX(-listView.getWidth());
                    listView.setAlpha(0.0f);
                }
                isListHidden = true;
                Log.d(TAG, "List hidden, isListHidden = " + isListHidden);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        listView.startAnimation(animationSet);
    }
    
    /**
     * Show list items (restore normal view) with smooth animation
     */
    private void showList() {
        if (!isListHidden) {
            return; // Already visible
        }
        
        ListView listView = getListView();
        if (listView == null) {
            return;
        }
        
        Log.d(TAG, "Showing list, isListHidden = " + isListHidden);
        
        // Reset translation and alpha first
        listView.setTranslationX(0);
        listView.setAlpha(1.0f);
        
        // Make list view visible first
        listView.setVisibility(View.VISIBLE);
        
        // Create smooth slide and fade in animation
        AnimationSet animationSet = new AnimationSet(true);
        
        // Slide in from the left
        TranslateAnimation slideIn = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, -1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        );
        slideIn.setDuration(400);
        slideIn.setInterpolator(new DecelerateInterpolator(1.5f)); // Smooth deceleration
        
        // Fade in
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(400);
        
        animationSet.addAnimation(slideIn);
        animationSet.addAnimation(fadeIn);
        animationSet.setFillAfter(true);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                ListView listView = getListView();
                if (listView != null) {
                    listView.clearAnimation();
                    // Reset translation and alpha to ensure it's in the correct position
                    listView.setTranslationX(0);
                    listView.setAlpha(1.0f);
                }
                isListHidden = false;
                Log.d(TAG, "List shown, isListHidden = " + isListHidden);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        listView.startAnimation(animationSet);
    }
    
    /**
     * Override onTouchEvent to handle swipes when list is hidden
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Always let gesture detector process the event
        if (gestureDetector != null) {
            boolean handled = gestureDetector.onTouchEvent(event);
            // If list is hidden and we detected a swipe, consume the event
            if (isListHidden && handled) {
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * This method is called when the user context-clicks a note in the list. NotesList registers
     * itself as the handler for context menus in its ListView (this is done in onCreate()).
     *
     * The only available options are COPY and DELETE.
     *
     * Context-click is equivalent to long-press.
     *
     * @param menu A ContexMenu object to which items should be added.
     * @param view The View for which the context menu is being constructed.
     * @param menuInfo Data associated with view.
     * @throws ClassCastException
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        // Tries to get the position of the item in the ListView that was long-pressed.
        try {
            // Casts the incoming data object into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            // If the menu object can't be cast, logs an error.
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        /*
         * Gets the data associated with the item at the selected position. getItem() returns
         * whatever the backing adapter of the ListView has associated with the item. In NotesList,
         * the adapter associated all of the data for a note with its list item. As a result,
         * getItem() returns that data as a Cursor.
         */
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        // If the cursor is empty, then for some reason the adapter can't get the data from the
        // provider, so returns null to the caller.
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Inflate menu from XML resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        // Sets the menu header to be the title of the selected note.
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // Append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(), 
                                        Integer.toString((int) info.id) ));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
    }

    /**
     * This method is called when the user selects an item from the context menu
     * (see onCreateContextMenu()). The only menu items that are actually handled are DELETE and
     * COPY. Anything else is an alternative option, for which default handling should be done.
     *
     * @param item The selected menu item
     * @return True if the menu item was DELETE, and no default processing is need, otherwise false,
     * which triggers the default handling of the item.
     * @throws ClassCastException
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // The data from the menu item.
        AdapterView.AdapterContextMenuInfo info;

        /*
         * Gets the extra info from the menu item. When an note in the Notes list is long-pressed, a
         * context menu appears. The menu items for the menu automatically get the data
         * associated with the note that was long-pressed. The data comes from the provider that
         * backs the list.
         *
         * The note's data is passed to the context menu creation routine in a ContextMenuInfo
         * object.
         *
         * When one of the context menu items is clicked, the same data is passed, along with the
         * note ID, to onContextItemSelected() via the item parameter.
         */
        try {
            // Casts the data object in the item into the type for AdapterView objects.
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {

            // If the object can't be cast, logs an error
            Log.e(TAG, "bad menuInfo", e);

            // Triggers default processing of the menu item.
            return false;
        }
        // Appends the selected note's ID to the URI sent with the incoming Intent.
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        /*
         * Gets the menu item's ID and compares it to known actions.
         */
        int id = item.getItemId();
        if (id == R.id.context_open) {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
            return true;
        } else if (id == R.id.context_copy) { //BEGIN_INCLUDE(copy)
            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);

            // Copies the notes URI to the clipboard. In effect, this copies the note itself
            clipboard.setPrimaryClip(ClipData.newUri(   // new clipboard item holding a URI
                    getContentResolver(),               // resolver to retrieve URI info
                    "Note",                             // label for the clip
                    noteUri));                          // the URI

            // Returns to the caller and skips further processing.
            return true;
            //END_INCLUDE(copy)
        } else if (id == R.id.context_delete) {
            // Deletes the note from the provider by passing in a URI in note ID format.
            // Please see the introductory note about performing provider operations on the
            // UI thread.
            getContentResolver().delete(
                    noteUri,  // The URI of the provider
                    null,     // No where clause is needed, since only a single note ID is being
                    // passed in.
                    null      // No where clause is used, so no where arguments are needed.
            );

            // Returns to the caller and skips further processing.
            return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * This method is called when the user clicks a note in the displayed list.
     *
     * This method handles incoming actions of either PICK (get data from the provider) or
     * GET_CONTENT (get or create data). If the incoming action is EDIT, this method sends a
     * new Intent to start NoteEditor.
     * @param l The ListView that contains the clicked item
     * @param v The View of the individual item
     * @param position The position of v in the displayed list
     * @param id The row ID of the clicked item
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
        // Ignore invalid clicks (e.g. invalid row IDs) to prevent crashes
        if (id < 0) {
            Log.w(TAG, "onListItemClick received invalid id: " + id);
            return;
        }

        // Constructs a new URI from the incoming URI and the row ID
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // Gets the action from the incoming Intent
        String action = getIntent().getAction();

        // Handles requests for note data
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // Sets the result to return to the component that called this Activity. The
            // result contains the new URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {

            // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
            // Intent's data is the note ID URI. The effect is to call NoteEdit.
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}
