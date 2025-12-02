# NotePad - 涓€娆惧姛鑳戒赴瀵岀殑Android绗旇搴旂敤

## 椤圭洰浠嬬粛

NotePad鏄竴娆惧熀浜嶢ndroid骞冲彴鐨勭瑪璁板簲鐢紝婕旂ず浜咥ndroid鏁版嵁搴撴搷浣滅殑鍩烘湰鏁欑▼銆傝搴旂敤鎻愪緵浜嗗畬鏁寸殑绗旇绠＄悊鍔熻兘锛屽寘鎷垱寤恒€佺紪杈戙€佸垹闄ょ瑪璁帮紝鏀寔鏍囩鍒嗙被銆佹悳绱㈠姛鑳藉拰涓€у寲璁剧疆銆?
## 鍔熻兘鐗圭偣

- 馃摑 **绗旇绠＄悊**锛氬垱寤恒€佺紪杈戙€佸垹闄ょ瑪璁?- 馃攳 **鎼滅储鍔熻兘**锛氭敮鎸佸叏灞€鎼滅储鍜屾爣绛惧唴鎼滅储
- 馃彿锔?**鏍囩鍒嗙被**锛氭敮鎸佹爣绛惧垱寤哄拰绗旇鍒嗙被
- 馃帹 **涓€у寲瀹氬埗**锛氬彲鑷畾涔夎儗鏅鑹插拰鑳屾櫙鍥剧墖
- 鈿欙笍 **鎺掑簭璁剧疆**锛氭敮鎸佹渶鏂颁紭鍏堝拰鏈€鏃т紭鍏堟帓搴?- 馃摫 **鎵嬪娍鎿嶄綔**锛氬乏鍙虫粦鍔ㄩ殣钘?鏄剧ず鍒楄〃
- 馃捑 **鏁版嵁瀹夊叏**锛氳嚜鍔ㄤ繚瀛樺拰鏈繚瀛樹慨鏀规彁绀?- 馃枊锔?**缂栬緫鍣ㄨ缃?*锛氭敮鎸佺紪杈戦〉鑳屾櫙棰滆壊鍜屽瓧浣撳ぇ灏忚皟鏁?
## 鎶€鏈爤

- Java
- Android SDK
- SQLite鏁版嵁搴?- ContentProvider
- CursorAdapter

## 瀹夎鍜岃繍琛?
### 鐜瑕佹眰

- Android Studio
- Android SDK
- JDK 8+

### 鏋勫缓姝ラ

1. 鍏嬮殕椤圭洰鍒版湰鍦?   ```bash
   git clone https://github.com/yourusername/NotePad.git
   ```

2. 浣跨敤Android Studio鎵撳紑椤圭洰

3. 杩炴帴Android璁惧鎴栧惎鍔ㄦā鎷熷櫒

4. 鐐瑰嚮杩愯鎸夐挳鏋勫缓骞跺畨瑁呭簲鐢?
### 鍛戒护琛屾瀯寤?
```bash
./gradlew assembleDebug
```

## 璇︾粏浣跨敤璇存槑

### 1. 涓荤晫闈?
**鎴浘**锛歴creenshots/(5Y60A~V~C~EZQS@CPTX02C.png

**鐣岄潰璇存槑**锛?- 椤堕儴鑷畾涔堿ctionBar锛屾樉绀哄簲鐢ㄥ悕绉?- 绗旇鍒楄〃浠ュ崱鐗囧舰寮忓睍绀猴紝鍖呭惈鏍囬銆佷慨鏀规椂闂村拰鏍囩
- 鍙充笂瑙掓湁娣诲姞鏂扮瑪璁扮殑"+"鍥炬爣
- 鏀寔宸﹀彸婊戝姩鎵嬪娍闅愯棌/鏄剧ず鍒楄〃

### 2. 鍒涘缓鏂扮瑪璁?
**鎿嶄綔娴佺▼**锛?
1. **杩涘叆鏂板缓绗旇鐣岄潰**
   - 鎴浘锛歴creenshots/)P1E_KJK0CC`W8VC@FOMOE0.png
   - 鎿嶄綔锛氱偣鍑讳富鐣岄潰鍙充笂瑙掔殑"+"鍥炬爣
   - 鏁堟灉锛氳繘鍏ョ┖鐧界殑绗旇缂栬緫鐣岄潰
   - **鍏抽敭浠ｇ爜**锛?     ```java
     // 鍦∟otesList.java涓鐞嗘坊鍔犳柊绗旇
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_add) {
             startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
             return true;
         }
         // 鍏朵粬閫夐」澶勭悊...
     }
     ```

2. **杈撳叆绗旇鍐呭**
   - 鎴浘锛歴creenshots/DQ]VL11US40[W)42~(L1@34.png
   - 鎿嶄綔锛氬湪姝ｆ枃鍖哄煙杈撳叆绗旇鍐呭
   - 鏁堟灉锛氭爣棰樺尯鍩熸樉绀洪粯璁ょ殑"鏃犳爣棰?鎴栬嚜鍔ㄦ彁鍙栫殑鍐呭
   - **鍏抽敭浠ｇ爜**锛?     ```java
     // 鍦∟oteEditor.java涓幏鍙栧拰璁剧疆绗旇鍐呭
     mText = (EditText) findViewById(R.id.note);
     if (mCursor != null && mCursor.moveToFirst()) {
         String note = mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE));
         if (note != null) {
             mText.setTextKeepState(note);
         }
     }
     ```

3. **娣诲姞鏍囩**
   - 鎴浘锛歴creenshots/P3AT~{G9O2$D5MKR]75B6LP.png
   - 鎿嶄綔锛氱偣鍑绘爣绛炬寜閽紝閫夋嫨宸叉湁鏍囩鎴栧垱寤烘柊鏍囩
   - 鏁堟灉锛氭爣绛炬坊鍔犲埌绗旇涓?   - **鍏抽敭浠ｇ爜**锛?     ```java
     // 鍦∟oteEditor.java涓鐞嗘爣绛捐缃?     mTagButton = (Button) findViewById(R.id.tag_button);
     if (mTagButton != null) {
         mTagButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showTagSelectionDialog();
             }
         });
     }
     ```

4. **淇敼绗旇鏍囬**
   - 鎴浘锛歴creenshots/E5YMSSCLN~FP~2XC@XX(68Y.png
   - 鎿嶄綔锛氱偣鍑绘爣棰樺尯鍩燂紝杈撳叆鏂版爣棰?   - 鏁堟灉锛氭爣棰樻洿鏂?   - **鍏抽敭浠ｇ爜**锛?     ```java
     // 鍦∟oteEditor.java涓鐞嗘爣棰樼紪杈?     mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title);
     if (mActionBarTitle != null) {
         mActionBarTitle.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(TitleEditor.EDIT_TITLE_ACTION);
                 intent.setData(mUri);
                 startActivity(intent);
             }
         });
     }
     ```

5. **淇濆瓨绗旇**
   - 鎴浘锛歴creenshots/V2~{{}P`_7LFW$A`97YA[C9.png
   - 鎿嶄綔锛氱偣鍑婚《閮ˋctionBar鐨?淇濆瓨"鎸夐挳
   - 鏁堟灉锛氱瑪璁颁繚瀛樺埌鏁版嵁搴擄紝杩斿洖涓荤晫闈?   - **鍏抽敭浠ｇ爜**锛?     ```java
     // 鍦∟oteEditor.java涓繚瀛樼瑪璁?     @Override
     protected void onPause() {
         super.onPause();
         // 淇濆瓨绗旇鍒版暟鎹簱
         ContentValues values = new ContentValues();
         values.put(NotePad.Notes.COLUMN_NAME_NOTE, mText.getText().toString());
         values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
         if (mCurrentTag != null) {
             values.put(NotePad.Notes.COLUMN_NAME_TAG, mCurrentTag);
         }
         getContentResolver().update(mUri, values, null, null);
     }
     ```

### 3. 鎵嬪娍鎿嶄綔

#### 3.1 宸﹀彸婊戝姩鏄剧ず/闅愯棌鍒楄〃

- **鍚戝乏婊戝姩**锛氶殣钘忕瑪璁板垪琛紝鏄剧ず绌鸿儗鏅?  - 鎴浘锛歴creenshots/JZ5C6}@4G]WK%B2{_{[)@13.png
  - 鎿嶄綔锛氬湪涓荤晫闈粠鍙冲悜宸︽粦鍔?  - 鏁堟灉锛氱瑪璁板垪琛ㄥ悜宸︽粦鍑哄睆骞曪紝鍙樉绀鸿儗鏅?  - **鍏抽敭浠ｇ爜**锛?    ```java
    // 鍦∟otesList.java涓垵濮嬪寲鎵嬪娍妫€娴嬪櫒
    gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();
            
            if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
                if (deltaX < 0) {
                    // 宸︽粦 - 闅愯棌鍒楄〃
                    hideList();
                    return true;
                }
            }
            return false;
        }
    });
    ```

- **鍚戝彸婊戝姩**锛氭仮澶嶇瑪璁板垪琛?  - 鎿嶄綔锛氬綋鍒楄〃闅愯棌鏃讹紝浠庡乏鍚戝彸婊戝姩
  - 鏁堟灉锛氱瑪璁板垪琛ㄥ钩婊戞粦鍏ュ睆骞曪紝鎭㈠姝ｅ父鏄剧ず
  - **鍏抽敭浠ｇ爜**锛?    ```java
    // 鍦∟otesList.java涓鐞嗗彸婊戞仮澶嶅垪琛?    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // ... 鍓嶉潰鐨勪唬鐮?...
        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
            if (deltaX > 0) {
                // 鍙虫粦 - 鏄剧ず鍒楄〃
                showList();
                return true;
            }
            // ... 宸︽粦澶勭悊 ...
        }
        return false;
    }
    
    // 鏄剧ず鍒楄〃鐨勬柟娉?    private void showList() {
        if (!isListHidden) {
            return;
        }
        ListView listView = getListView();
        // 鍔ㄧ敾鏁堟灉瀹炵幇...
        listView.setVisibility(View.VISIBLE);
        isListHidden = false;
    }
    ```

### 4. 鎼滅储鍔熻兘

#### 4.1 鍏ㄥ眬鎼滅储

- 鎴浘锛歴creenshots/%8E}Y07FFQ$GOSE81_KP{1G.png
- 鎿嶄綔锛?  1. 鍦ㄤ富鐣岄潰鐐瑰嚮鎼滅储鍥炬爣
  2. 杈撳叆鎼滅储鍏抽敭璇?  3. 绯荤粺鍦ㄦ墍鏈夌瑪璁颁腑鎼滅储
- 鏁堟灉锛氭樉绀烘墍鏈夊尮閰嶅叧閿瘝鐨勭瑪璁?- **鍏抽敭浠ｇ爜**锛?  ```java
  // 鍦∟otesList.java涓鐞嗘悳绱㈡剰鍥?  private void handleIntent(Intent intent) {
      if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          if (query != null && !query.isEmpty()) {
              String searchPattern = "%" + query + "%";
              // 鍏ㄥ眬鎼滅储锛氬湪鏍囬銆佸唴瀹瑰拰鏍囩涓悳绱?              mSelection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + 
                          NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ? OR " +
                          NotePad.Notes.COLUMN_NAME_TAG + " LIKE ?";
              mSelectionArgs = new String[] { searchPattern, searchPattern, searchPattern };
          }
      }
  }
  ```

#### 4.2 鏍囩鍐呮悳绱?
- 鎴浘锛歴creenshots/6D)_SCZD4DYOK$`%3~@]X]0.png
- 鎿嶄綔锛?  1. 鐐瑰嚮鏍囩杩涘叆鏍囩杩囨护鐘舵€?  2. 鐐瑰嚮鎼滅储鍥炬爣
  3. 杈撳叆鎼滅储鍏抽敭璇嶏紙濡?鍛ㄤ簩"锛?- 鏁堟灉锛氬彧鍦ㄥ綋鍓嶆爣绛惧唴鎼滅储锛屾樉绀哄尮閰嶇粨鏋?- **鍏抽敭浠ｇ爜**锛?  ```java
  // 鍦∟otesList.java涓鐞嗘爣绛惧唴鎼滅储
  private void handleIntent(Intent intent) {
      if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          if (query != null && !query.isEmpty()) {
              String searchPattern = "%" + query + "%";
              
              // 濡傛灉鏈夊綋鍓嶆爣绛捐繃婊わ紝鍦ㄦ爣绛惧唴鎼滅储
              if (mCurrentTagFilter != null && !mCurrentTagFilter.isEmpty()) {
                  mSelection = "(" + NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ? OR " + 
                              NotePad.Notes.COLUMN_NAME_NOTE + " LIKE ?) AND " +
                              NotePad.Notes.COLUMN_NAME_TAG + " = ?";
                  mSelectionArgs = new String[] { searchPattern, searchPattern, mCurrentTagFilter };
              } else {
                  // 鍏ㄥ眬鎼滅储閫昏緫...
              }
          }
      }
  }
  ```

### 5. 璁剧疆鍔熻兘

#### 5.1 鎺掑簭鏂瑰紡璁剧疆

- 鎴浘锛歴creenshots/KDK)HVZB`YM~9K}PV68~0XV.png
- 鎿嶄綔锛?  1. 杩涘叆璁剧疆鐣岄潰
  2. 鎵惧埌"鎺掑簭鏂瑰紡"閫夐」
  3. 閫夋嫨"鏈€鏂颁紭鍏?鎴?鏈€鏃т紭鍏?
- 鏁堟灉锛氱瑪璁板垪琛ㄦ寜閫夋嫨鐨勬柟寮忔帓搴?- **鍏抽敭浠ｇ爜**锛?  ```java
  // 鍦∟otesList.java涓幏鍙栨帓搴忔柟寮?  private String getSortOrder() {
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
  ```

#### 5.2 鑳屾櫙璁剧疆

##### 5.2.1 鑳屾櫙棰滆壊鏇存崲

- 鎴浘锛歴creenshots/U5_H[%9M(%M}(R3Y7O{BJYQ.png
- 鎿嶄綔锛?  1. 杩涘叆璁剧疆鐣岄潰
  2. 鐐瑰嚮"鑳屾櫙棰滆壊"閫夐」
  3. 浠庨鑹查€夋嫨鍣ㄤ腑閫夋嫨棰滆壊锛堢櫧鑹层€佺背鑹层€佽摑鑹层€佺豢鑹茬瓑锛?  4. 绯荤粺鑷姩搴旂敤鎵€閫夐鑹?- **鍏抽敭浠ｇ爜**锛?  ```java
  // 鍦∟otesList.java涓簲鐢ㄨ儗鏅鑹?  private void applyBackgroundColor() {
      if (prefs == null) {
          return;
      }
      
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
      
      // 搴旂敤鑳屾櫙棰滆壊鍒板悇涓鍥?      if (getWindow() != null) {
          getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
      }
      View rootView = findViewById(android.R.id.content);
      if (rootView != null) {
          rootView.setBackgroundColor(backgroundColor);
      }
      ListView listView = getListView();
      if (listView != null) {
          listView.setBackgroundColor(backgroundColor);
      }
  }
  ```

##### 5.2.2 娣诲姞鑳屾櫙鍥剧墖

- 鎴浘锛歴creenshots/FAV5TU_H%W1ZS{3X0}~3XHP.png
- 鎿嶄綔锛?  1. 杩涘叆璁剧疆鐣岄潰
  2. 鐐瑰嚮"鑳屾櫙鍥剧墖"閫夐」
  3. 浠庣浉鍐岄€夋嫨鍥剧墖
  4. 杩涘叆鍥剧墖瑁佸壀鐣岄潰锛岃皟鏁磋鍓
  5. 鐐瑰嚮"淇濆瓨"瀹屾垚瑁佸壀
- 鏁堟灉锛氬簲鐢ㄨ儗鏅彉涓烘墍閫夊浘鐗?- **鍏抽敭浠ｇ爜**锛?  ```java
  // 鍦∟otesList.java涓簲鐢ㄨ儗鏅浘鐗?  private void applyBackgroundColor() {
      // ... 鑳屾櫙棰滆壊浠ｇ爜 ...
      
      // 妫€鏌ユ槸鍚﹁缃簡鑳屾櫙鍥剧墖
      String imagePath = prefs.getString(SettingsActivity.PREF_BACKGROUND_IMAGE, null);
      if (imagePath != null && !imagePath.isEmpty()) {
          try {
              File imageFile = new File(imagePath);
              if (imageFile.exists()) {
                  Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                  BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                  
                  // 搴旂敤鑳屾櫙鍥剧墖
                  if (getWindow() != null) {
                      getWindow().setBackgroundDrawable(drawable);
                  }
                  View rootView = findViewById(android.R.id.content);
                  if (rootView != null) {
                      rootView.setBackground(drawable);
                  }
                  ListView listView = getListView();
                  if (listView != null) {
                      listView.setBackground(drawable);
                  }
                  return;
              }
          } catch (Exception e) {
              Log.e(TAG, "Error loading background image", e);
          }
      }
      
      // ... 鑳屾櫙棰滆壊榛樿澶勭悊 ...
  }
  ```

### 6. 绗旇缂栬緫椤佃缃?
- 鎴浘锛歴creenshots/F$L[FHZV0}G_MXIP(7V8G(8.png)銆乻creenshots/RT%SON24_O5Y41R3]@7VBLI.png
- 鎿嶄綔锛?  1. 杩涘叆绗旇缂栬緫鐣岄潰
  2. 鐐瑰嚮璁剧疆鍥炬爣
  3. 璋冩暣缂栬緫椤佃儗鏅鑹?  4. 璋冩暣绗旇瀛椾綋澶у皬
- 鏁堟灉锛氬疄鏃堕瑙堝苟淇濆瓨璁剧疆
- **鍏抽敭浠ｇ爜**锛?  ```java
  // 鍦∟oteEditor.java涓簲鐢ㄧ紪杈戝櫒璁剧疆
  private void applySettings() {
      // 搴旂敤鑳屾櫙棰滆壊
      if (mRootLayout != null) {
          String editorBgColor = prefs.getString(SettingsActivity.PREF_EDITOR_BG_COLOR, "white");
          int colorRes = R.color.editor_bg_white;
          if ("cream".equals(editorBgColor)) {
              colorRes = R.color.editor_bg_cream;
          } else if ("blue".equals(editorBgColor)) {
              colorRes = R.color.editor_bg_blue;
          } else if ("green".equals(editorBgColor)) {
              colorRes = R.color.editor_bg_green;
          }
          mRootLayout.setBackgroundColor(getResources().getColor(colorRes));
      }
      
      // 搴旂敤瀛椾綋澶у皬
      if (mText != null) {
          String fontSize = prefs.getString(SettingsActivity.PREF_FONT_SIZE, "medium");
          int textSize = getResources().getDimensionPixelSize(R.dimen.font_size_medium);
          if ("small".equals(fontSize)) {
              textSize = getResources().getDimensionPixelSize(R.dimen.font_size_small);
          } else if ("large".equals(fontSize)) {
              textSize = getResources().getDimensionPixelSize(R.dimen.font_size_large);
          }
          mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
      }
  }
  ```

### 7. 鍒犻櫎绗旇

- 鎴浘锛歴creenshots/FT%FS%0(%JJ)04CSBA{J@WU.png
- 鎿嶄綔锛?  1. 鍦ㄧ瑪璁扮紪杈戠晫闈㈢偣鍑诲垹闄ゅ浘鏍?  2. 寮瑰嚭鍒犻櫎纭瀵硅瘽妗?  3. 纭鍚庡垹闄ょ瑪璁?- 鏁堟灉锛氱瑪璁颁粠鏁版嵁搴撲腑鍒犻櫎锛岃繑鍥炰富鐣岄潰
- **鍏抽敭浠ｇ爜**锛?  ```java
  // 鍦∟oteEditor.java涓鐞嗗垹闄ゆ搷浣?  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == R.id.menu_delete) {
          // 鏄剧ず鍒犻櫎纭瀵硅瘽妗?          new AlertDialog.Builder(this)
                  .setTitle(R.string.dialog_delete_title)
                  .setMessage(R.string.dialog_delete_message)
                  .setPositiveButton(R.string.dialog_delete_confirm, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          deleteNote();
                      }
                  })
                  .setNegativeButton(R.string.dialog_delete_cancel, null)
                  .show();
          return true;
      }
      // 鍏朵粬閫夐」澶勭悊...
  }
  
  // 鎵ц鍒犻櫎鎿嶄綔
  private void deleteNote() {
      if (mUri != null) {
          getContentResolver().delete(mUri, null, null);
          setResult(RESULT_OK);
          finish();
      }
  }
  ```

### 8. 鏈繚瀛樹慨鏀规彁绀?
- 鎴浘锛歴creenshots/_0Q4QTW3EOH[OF(J%0(JB76.png)銆乻creenshots/]U$)B6DG(]S$ZLGOI{UWHOH.png
- 鎿嶄綔鍦烘櫙锛氬湪绗旇缂栬緫鐣岄潰淇敼鍐呭鍚庯紝鏈偣鍑讳繚瀛樿€岀偣鍑昏繑鍥?- 鏁堟灉锛?  - 寮瑰嚭鎻愮ず瀵硅瘽妗嗭細"鎮ㄦ湁鏈繚瀛樼殑鏇存敼"
  - 鎻愪緵"淇濆瓨"鍜?涓嶄繚瀛?閫夐」
  - 鐐瑰嚮"淇濆瓨"锛氫繚瀛樻洿鏀瑰苟杩斿洖
  - 鐐瑰嚮"涓嶄繚瀛?锛氭斁寮冩洿鏀瑰苟杩斿洖
- **鍏抽敭浠ｇ爜**锛?  ```java
  // 鍦∟oteEditor.java涓鐞嗚繑鍥炴寜閽?  private void handleBackPressed() {
      // 妫€鏌ュ唴瀹规槸鍚︽湁鍙樺寲
      boolean contentChanged = false;
      if (mText != null && mOriginalContent != null) {
          String currentContent = mText.getText().toString();
          contentChanged = !currentContent.equals(mOriginalContent);
      }
      
      // 妫€鏌ユ爣绛炬槸鍚︽湁鍙樺寲
      boolean tagChanged = (mCurrentTag != null && !mCurrentTag.equals(mOriginalTag)) ||
                          (mCurrentTag == null && mOriginalTag != null);
      
      if (contentChanged || tagChanged) {
          // 鏄剧ず鏈繚瀛樹慨鏀规彁绀哄璇濇
          new AlertDialog.Builder(this)
                  .setTitle(R.string.dialog_unsaved_title)
                  .setMessage(R.string.dialog_unsaved_message)
                  .setPositiveButton(R.string.dialog_save, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          // 淇濆瓨鏇存敼
                          onPause(); // 璋冪敤onPause()鏉ヤ繚瀛樺唴瀹?                          finish();
                      }
                  })
                  .setNegativeButton(R.string.dialog_dont_save, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          // 涓嶄繚瀛橈紝鐩存帴閫€鍑?                          mShouldSkipSave = true; // 璁剧疆鏍囧織璺宠繃鑷姩淇濆瓨
                          finish();
                      }
                  })
                  .setCancelable(false)
                  .show();
      } else {
          // 娌℃湁鍙樺寲锛岀洿鎺ラ€€鍑?          finish();
      }
  }
  ```

## 鍏抽敭浠ｇ爜瑙ｆ瀽

### 1. ContentProvider瀹炵幇

```java
public class NotePadProvider extends ContentProvider {
    // 鏁版嵁搴撶増鏈?    private static final int DATABASE_VERSION = 2;
    // 鏁版嵁搴撳悕绉?    private static final String DATABASE_NAME = "note_pad.db";
    // 琛ㄥ悕
    private static final String NOTES_TABLE_NAME = "notes";
    
    // 鍒涘缓琛ㄧ殑SQL璇彞
    private static final String CREATE_NOTES_TABLE = "CREATE TABLE " + NOTES_TABLE_NAME + " (" +
            NotePad.Notes._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            NotePad.Notes.COLUMN_NAME_TITLE + " TEXT, " +
            NotePad.Notes.COLUMN_NAME_NOTE + " TEXT, " +
            NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER, " +
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER, " +
            NotePad.Notes.COLUMN_NAME_TAG + " TEXT" +
            ");";
    
    // 鏁版嵁搴撹緟鍔╃被
    private static class DatabaseHelper extends SQLiteOpenHelper {
        // 鏋勯€犲嚱鏁?        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        // 鍒涘缓鏁版嵁搴?        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_NOTES_TABLE);
        }
        
        // 鏇存柊鏁版嵁搴?        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 绠€鍖栫増锛屽疄闄呭簲鐢ㄤ腑搴旇€冭檻鏁版嵁杩佺Щ
            db.execSQL("DROP TABLE IF EXISTS " + NOTES_TABLE_NAME);
            onCreate(db);
        }
    }
    
    // 鍏朵粬ContentProvider鏂规硶瀹炵幇...
}
```

### 2. 鎵嬪娍妫€娴嬪疄鐜?
```java
// 鍒濆鍖栨墜鍔挎娴嬪櫒
gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null) {
            return false;
        }
        
        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();
        
        // 妫€娴嬫按骞虫粦鍔?        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
            if (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY || Math.abs(deltaX) > SWIPE_MIN_DISTANCE * 2) {
                if (deltaX > 0) {
                    // 鍙虫粦 - 鏄剧ず鍒楄〃
                    showList();
                    return true;
                } else {
                    // 宸︽粦 - 闅愯棌鍒楄〃
                    hideList();
                    return true;
                }
            }
        }
        
        return false;
    }
});
```

### 3. 绗旇鍒楄〃閫傞厤鍣?
```java
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
        
        // 鏍囩鏄剧ず鍜岀偣鍑诲鐞?        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            
            // 鏄剧ず鏍囩骞舵坊鍔犵偣鍑讳簨浠?            TextView tagView = view.findViewById(R.id.tag_text);
            if (tagView != null) {
                String tag = cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TAG));
                if (tag != null && !tag.trim().isEmpty()) {
                    tagView.setText(tag.trim());
                    tagView.setVisibility(View.VISIBLE);
                    tagView.setOnClickListener(v -> filterByTag(tag.trim()));
                } else {
                    tagView.setVisibility(View.GONE);
                }
            }
        }
    };
}
```

## 椤圭洰缁撴瀯

```
app/
鈹溾攢鈹€ src/main/
鈹?  鈹溾攢鈹€ java/com/example/android/notepad/
鈹?  鈹?  鈹溾攢鈹€ NotesList.java          # 绗旇鍒楄〃涓荤晫闈?鈹?  鈹?  鈹溾攢鈹€ NoteEditor.java         # 绗旇缂栬緫鐣岄潰
鈹?  鈹?  鈹溾攢鈹€ NotePadProvider.java    # 鏁版嵁搴撳唴瀹规彁渚涜€?鈹?  鈹?  鈹溾攢鈹€ NotePad.java            # 甯搁噺瀹氫箟
鈹?  鈹?  鈹溾攢鈹€ TitleEditor.java        # 鏍囬缂栬緫鐣岄潰
鈹?  鈹?  鈹溾攢鈹€ SettingsActivity.java   # 璁剧疆鐣岄潰
鈹?  鈹?  鈹溾攢鈹€ EditorSettingsActivity.java # 缂栬緫鍣ㄨ缃?鈹?  鈹?  鈹斺攢鈹€ ImageCropActivity.java  # 鍥剧墖瑁佸壀鍔熻兘
鈹?  鈹斺攢鈹€ res/                        # 璧勬簮鏂囦欢
鈹斺攢鈹€ build.gradle                    # 鏋勫缓閰嶇疆
```

## 鏁版嵁搴撹璁?
### 琛ㄧ粨鏋?
| 瀛楁鍚?| 绫诲瀷 | 鎻忚堪 |
|--------|------|------|
| _id | INTEGER | 绗旇ID锛屼富閿?|
| title | TEXT | 绗旇鏍囬 |
| note | TEXT | 绗旇鍐呭 |
| created | INTEGER | 鍒涘缓鏃堕棿鎴?|
| modified | INTEGER | 淇敼鏃堕棿鎴?|
| tag | TEXT | 绗旇鏍囩 |

## 瀛︿範浠峰€?
杩欎釜椤圭洰鏄疉ndroid瀹樻柟鐨勭粡鍏哥ず渚嬶紝閫傚悎瀛︿範浠ヤ笅鍐呭锛?
1. Android鏁版嵁搴撴搷浣?2. ContentProvider鐨勪娇鐢?3. ListView鍜孋ursorAdapter鐨勫簲鐢?4. 鑷畾涔夎鍥惧拰鍔ㄧ敾
5. 鎼滅储鍔熻兘瀹炵幇
6. 鎵嬪娍鎿嶄綔澶勭悊
7. 鑷畾涔変富棰樺拰鏍峰紡

## 璐＄尞鎸囧崡

娆㈣繋鎻愪氦Issue鍜孭ull Request锛?
## 璁稿彲璇?
Apache License 2.0

## 鎴浘灞曠ず

### 涓荤晫闈?
![涓荤晫闈(screenshots/(5Y60A~V~C~EZQS@CPTX02C.png)

### 绗旇缂栬緫

![绗旇缂栬緫](screenshots/)P1E_KJK0CC`W8VC@FOMOE0.png)

### 鎼滅储鍔熻兘

![鎼滅储鍔熻兘](screenshots/%8E}Y07FFQ$GOSE81_KP{1G.png)

### 璁剧疆鐣岄潰

![璁剧疆鐣岄潰](screenshots/SOZQ{M`1@SY11YM{64X0SBW.png)

### 鑳屾櫙璁剧疆

![鑳屾櫙璁剧疆](screenshots/U5_H[%9M(%M}(R3Y7O{BJYQ.png)

