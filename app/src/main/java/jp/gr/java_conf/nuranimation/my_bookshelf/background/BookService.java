package jp.gr.java_conf.nuranimation.my_bookshelf.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.dropbox.core.android.Auth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;

public class BookService extends Service implements SearchBooksThread.ThreadFinishListener,DropboxThread.ThreadFinishListener,FileIOThread.ThreadFinishListener {
    public static final String TAG = BookService.class.getSimpleName();
    private static final boolean D = true;


    public static final String KEY_SERVICE_STATE = "KEY_SERVICE_STATE";
    public static final String KEY_PARAM_SEARCH_KEYWORD = "KEY_PARAM_SEARCH_KEYWORD";
    public static final String KEY_PARAM_SEARCH_PAGE    = "KEY_PARAM_SEARCH_PAGE";

    public static final int STATE_NONE                          =  0;
    public static final int STATE_SEARCH_BOOKS_SEARCH_START     =  1;
    public static final int STATE_SEARCH_BOOKS_SEARCH_FINISH    =  2;
    public static final int STATE_NEW_BOOKS_RELOAD_START        =  3;
    public static final int STATE_NEW_BOOKS_RELOAD_FINISH       =  4;
    public static final int STATE_EXPORT_START                  =  5;
    public static final int STATE_EXPORT_FINISH                 =  6;
    public static final int STATE_IMPORT_START                  =  7;
    public static final int STATE_IMPORT_FINISH                 =  8;
    public static final int STATE_BACKUP_START                  =  9;
    public static final int STATE_BACKUP_FINISH                 = 10;
    public static final int STATE_RESTORE_START                 = 11;
    public static final int STATE_RESTORE_FINISH                = 12;
    public static final int STATE_DROPBOX_LOGIN                 = 13;




    private static final int notifyId = 1;
    private NotificationManager mNotificationManager;
    private LocalBroadcastManager mLocalBroadcastManager;
    private MyBookshelfApplicationData mApplicationData;
    private SearchBooksThread.Result mSearchBooksResult;
    private SearchBooksThread searchBooksThread;
    private FileIOThread fileIOThread;
    private DropboxThread dropboxThread;


    private String mParamSEARCH_KEYWORD;
    private int mParamSEARCH_PAGE;
    private List<String> mAuthorsList;
    private int mState;
    private boolean isForeground;

    private List<BookData> resultNewBooks;

    private int progress_last = 0;
    private int progress_now  = 0;



    public class MBinder extends Binder {
        public BookService getService() {
            return BookService.this;
        }
    }

    public BookService() {
        super();
    }


    @Override
    public IBinder onBind(Intent intent) {
        if (D) Log.d(TAG, "onBind");
        return new MBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        if (D) Log.d(TAG, "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (D) Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onCreate() {
        if (D) Log.d(TAG, "onCreate");
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mApplicationData = (MyBookshelfApplicationData) this.getApplicationContext();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    @Override
    public void onDestroy() {
        if (D) Log.d(TAG, "onDestroy");
        switch(mState) {
            case STATE_SEARCH_BOOKS_SEARCH_START:
                cancelSearch();
                break;
            case STATE_NEW_BOOKS_RELOAD_START:
                cancelReload();
                break;
        }
     }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.d(TAG, "onStartCommand");
        Notification notification = createNotification(mState);
        isForeground = true;
        startForeground(notifyId, notification);
        return START_NOT_STICKY;
    }


    @Override
    public void deliverSearchBooksResult(SearchBooksThread.Result result) {
        switch(mState) {
            case STATE_SEARCH_BOOKS_SEARCH_START:
                saveSearchBooksResult(result);
                if (result.isSuccess()) {
                    mApplicationData.registerToSearchBooks(result.getBooks());
                }
                setServiceState(STATE_SEARCH_BOOKS_SEARCH_FINISH);
                break;
            case STATE_NEW_BOOKS_RELOAD_START:
                saveSearchBooksResult(result);
                String author = getSearchKeyword();
                int page = getSearchPage();
                if (!TextUtils.isEmpty(author) && page > 0) {
                    searchBooksThread = new SearchBooksThread(this, author, page, getString(R.string.SearchBooksSort_Code_SALES_DATE_DESCENDING));
                    searchBooksThread.start();
                } else {
                    mApplicationData.registerToNewBooks(resultNewBooks);
                    setServiceState(STATE_NEW_BOOKS_RELOAD_FINISH);
                }
                break;
            default:
                if (D) Log.d(TAG, "Error");
                setServiceState(STATE_NONE);
                break;
        }
    }

    @Override
    public void deliverExportResult(FileIOThread.Result result) {
        switch (mState) {
            case STATE_EXPORT_START:

                break;
            case STATE_BACKUP_START:
                if(result.isSuccess()) {
                    String token = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_ACCESS_TOKEN, null);
                    DropboxThread mDropboxThread = new DropboxThread(this, DropboxThread.TYPE_BACKUP, token);
                    mDropboxThread.start();
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void deliverImportResult(FileIOThread.Result result) {
        switch(mState) {
            case STATE_IMPORT_START:
                if(result.isSuccess()){
                    setServiceState(STATE_IMPORT_FINISH);
                }
                break;
            case STATE_RESTORE_START:
                if(result.isSuccess()) {
                    setServiceState(STATE_RESTORE_FINISH);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void deliverBackupResult(DropboxThread.Result result) {
        switch(mState) {
            case STATE_BACKUP_START:
                break;
            default:
                break;
        }
    }

    @Override
    public void deliverRestoreResult(DropboxThread.Result result) {
        switch(mState) {
            case STATE_RESTORE_START:
                if(result.isSuccess()) {
                    fileIOThread = new FileIOThread(this, FileIOThread.TYPE_IMPORT);
                    fileIOThread.start();
                }
                break;
            default:
                break;
        }

    }


    public void setServiceState(int state){
        this.mState = state;
        Intent intent = new Intent();
        intent.putExtra(BaseFragment.KEY_UPDATE_SERVICE_STATE, state);
        intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_SERVICE_STATE);
        mLocalBroadcastManager.sendBroadcast(intent);
        updateNotification(mState);
    }

    public int getServiceState(){
        return mState;
    }

    public void setSearchKeyword(String keyword){
        this.mParamSEARCH_KEYWORD = keyword;
    }

    public String getSearchKeyword(){
        return this.mParamSEARCH_KEYWORD;
    }

    public void setSearchPage(int page){
        this.mParamSEARCH_PAGE = page;
    }

    public int getSearchPage(){
        return this.mParamSEARCH_PAGE;
    }


    public void endForeground(){
        isForeground = false;
        stopForeground(true);
    }


    public void searchBooks(final String keyword, final int page){
        setServiceState(STATE_SEARCH_BOOKS_SEARCH_START);
        setSearchKeyword(keyword);
        setSearchPage(page);
        searchBooksThread = new SearchBooksThread(this, keyword, page, mApplicationData.getSearchBooksSortSetting());
        searchBooksThread.start();
    }

    public void cancelSearch(){
        if(searchBooksThread != null) {
            searchBooksThread.cancel();
            setServiceState(STATE_NONE);
            searchBooksThread = null;
        }
    }

    public void reloadNewBooks(final List<String> authors){
        resultNewBooks = new ArrayList<>();
        mAuthorsList = new ArrayList<>(authors);
        if(mAuthorsList.size() > 0) {
            progress_last = mAuthorsList.size();
            progress_now  = 0;
            updateProgress(progress_now, progress_last);
            String author = mAuthorsList.get(0);
            mAuthorsList.remove(0);
            setSearchKeyword(author);
            setSearchPage(1);
            setServiceState(STATE_NEW_BOOKS_RELOAD_START);
            searchBooksThread = new SearchBooksThread(this, author, 1, getString(R.string.SearchBooksSort_Code_SALES_DATE_DESCENDING));
            searchBooksThread.start();
        }else{
            if(D) Log.d(TAG, "No authorsList");
            setServiceState(STATE_NONE);
        }
    }

    public void cancelReload(){
        if(searchBooksThread != null){
            searchBooksThread.cancel();
            setServiceState(STATE_NONE);
            searchBooksThread = null;
        }
    }



    public SearchBooksThread.Result loadSearchBooksResult(){
        if(mSearchBooksResult == null){
            return SearchBooksThread.Result.error(SearchBooksThread.ERROR_UNKNOWN,"failed get result");
        }
        return mSearchBooksResult;
    }


    public FileIOThread.Result loadFileIOResult(){
        return null;
    }


    private void saveSearchBooksResult(SearchBooksThread.Result result){
        switch(mState){
            case STATE_SEARCH_BOOKS_SEARCH_START:
                mSearchBooksResult = result;
                break;
            case STATE_NEW_BOOKS_RELOAD_START:
                if(result.isSuccess()){
                    boolean needLoadNext = true;
                    List<BookData> check = result.getBooks();
                    for(BookData book : check){
                        if(isNewBook(book)){
                            String book_author = book.getAuthor();
                            book_author = book_author.replaceAll("[\\x20\\u3000]","");
                            if(book_author.contains(getSearchKeyword())){
                                if(D) Log.d(TAG,"author: " + getSearchKeyword() + " add: " + book.getTitle());
                                resultNewBooks.add(book);
                            }
                        }else{
                            needLoadNext = false;
                            break;
                        }
                    }
                    if(result.hasNext() && needLoadNext){
                        setSearchPage(getSearchPage() + 1);
                    }else{
                        progress_now++;
                        if(mAuthorsList.size() > 0){
                            String author = mAuthorsList.get(0);
                            mAuthorsList.remove(0);
                            setSearchKeyword(author);
                            setSearchPage(1);
                        }else{
                            setSearchKeyword(null);
                            setSearchPage(0);
                        }
                    }
                }else{
                    if (D) Log.d(TAG, "Error: " + result.getErrorMessage());
                    progress_now++;
                    if(mAuthorsList.size() > 0){
                        String author = mAuthorsList.get(0);
                        mAuthorsList.remove(0);
                        setSearchKeyword(author);
                        setSearchPage(1);
                    }else{
                        setSearchKeyword(null);
                        setSearchPage(0);
                    }
                }
                updateProgress(progress_now,progress_last);
                break;
        }
    }





    public void exportCSV(){
        setServiceState(STATE_EXPORT_START);
        fileIOThread = new FileIOThread(this, FileIOThread.TYPE_EXPORT);
        fileIOThread.start();
    }


    public void importCSV(){
        setServiceState(STATE_IMPORT_START);
        fileIOThread = new FileIOThread(this, FileIOThread.TYPE_IMPORT);
        fileIOThread.start();
    }


    public void backupCSV(){
        setServiceState(STATE_BACKUP_START);
        fileIOThread = new FileIOThread(this, FileIOThread.TYPE_EXPORT);
        fileIOThread.start();
    }

    public void restoreCSV(){
        setServiceState(STATE_RESTORE_START);
        String token = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_ACCESS_TOKEN, null);
        dropboxThread = new DropboxThread(this, DropboxThread.TYPE_RESTORE, token);
        dropboxThread.start();
    }

    public void startAuthenticate(){
        Auth.startOAuth2Authentication(this,getString(R.string.dropbox_key));
    }

    public String getAccessToken(){
        return Auth.getOAuth2Token();
    }

    private void updateProgress(int progress, int size){
        Intent intent = new Intent();
        intent.putExtra(BaseFragment.KEY_UPDATE_PROGRESS,  String.format(Locale.JAPAN, "%d / %d", progress, size));
        intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
        mLocalBroadcastManager.sendBroadcast(intent);
    }


    private void updateNotification(int state) {
        if(mNotificationManager != null && isForeground) {
            Notification notification = createNotification(state);
            mNotificationManager.notify(notifyId,notification);
        }
    }

    private Notification createNotification(int state){
        Notification notification;
        String title = getString(R.string.Notification_Channel_Title);
        String message = getString(R.string.Notification_Channel_Title);
        int iconId  = R.drawable.ic_vector_shelf_24dp;
        switch (state) {
            case STATE_NONE:
                break;
            case STATE_SEARCH_BOOKS_SEARCH_START:
                title = getString(R.string.Notification_Title_Search);
                message = getString(R.string.Notification_Message_Search_Incomplete);
                iconId  = R.drawable.ic_vector_search_24dp;
                break;
            case STATE_SEARCH_BOOKS_SEARCH_FINISH:
                title = getString(R.string.Notification_Title_Search);
                message = getString(R.string.Notification_Message_Search_Complete);
                iconId  = R.drawable.ic_vector_search_24dp;
                break;
            case STATE_NEW_BOOKS_RELOAD_START:
                title = getString(R.string.Notification_Title_Reload);
                message = getString(R.string.Notification_Message_Reload_Incomplete);
                iconId  = R.drawable.ic_vector_reload_24dp;
                break;
            case STATE_NEW_BOOKS_RELOAD_FINISH:
                title = getString(R.string.Notification_Title_Reload);
                message = getString(R.string.Notification_Message_Reload_Complete);
                iconId  = R.drawable.ic_vector_reload_24dp;
                break;
        }

        Intent ni = new Intent();
        ni.addCategory(Intent.CATEGORY_LAUNCHER);
        ni.setClassName(getApplicationContext().getPackageName(), MainActivity.class.getName());
        ni.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, ni, PendingIntent.FLAG_CANCEL_CURRENT);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext(), getString(R.string.Notification_Channel_ID))
                    .setContentTitle(title)
                    .setSmallIcon(iconId)
                    .setContentText(message)
                    .setContentIntent(pi)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this, getString(R.string.Notification_Channel_ID))
                    .setContentTitle(title)
                    .setSmallIcon(iconId)
                    .setContentText(message)
                    .setContentIntent(pi)
                    .build();
        }
        return notification;
    }



    private boolean isNewBook(BookData book){
        Calendar baseDate = Calendar.getInstance();
        baseDate.add(Calendar.DAY_OF_MONTH, -14);
        Calendar salesDate = MyBookshelfUtils.parseDate(book.getSalesDate());
        if(salesDate != null){
            return salesDate.compareTo(baseDate) >= 0;
        }else{
            return false;
        }
    }




}