package ppitol.exd.app.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.google.common.base.Predicate;

import lombok.Getter;

import static ppitol.exd.app.model.RateContract.AUTHORITY;
import static ppitol.exd.app.model.RateContract.DEFAULT_SORT_ORDER;
import static ppitol.exd.app.model.RateContract.RATE_CONTENT;
import static ppitol.exd.app.model.RateContract.RATE_DIR_MIME_TYPE;
import static ppitol.exd.app.model.RateContract.RATE_ITEM_MIME_TYPE;

/**
 * Content provider for currencies' rates.
 */
public class RateProvider extends ContentProvider {

    private static final int RATE_DIR_CODE = 1;
    private static final int RATE_ITEM_CODE = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, RATE_CONTENT, RATE_DIR_CODE);
        uriMatcher.addURI(AUTHORITY, RATE_CONTENT + "/#", RATE_ITEM_CODE);
    }

    private static int parseUriCode(Uri uri) {
        int code = uriMatcher.match(uri);
        if (code != RATE_DIR_CODE && code != RATE_ITEM_CODE) {
            throw new IllegalArgumentException("Illegal Uri: " + uri);
        }
        return code;
    }

    private final Predicate<Uri> isItemUri = new Predicate<Uri>() {
        @Override
        public boolean apply(Uri uri) {
            int code = parseUriCode(uri);
            return code == RATE_ITEM_CODE;
        }
    };

    @Getter
    private RateDatabase rateDatabase;

    @Override
    public boolean onCreate() {
        rateDatabase = new RateDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (parseUriCode(uri)) {
            case RATE_ITEM_CODE:
                return RATE_ITEM_MIME_TYPE;
            case RATE_DIR_CODE:
            default:
                return RATE_DIR_MIME_TYPE;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ItemSelect sel = ItemSelect.forQuery()
                .withItemUriPredicate(isItemUri)
                .selectionFor(uri, selection, selectionArgs);

        String order = TextUtils.isEmpty(sortOrder) ? DEFAULT_SORT_ORDER : sortOrder;

        Cursor cursor = rateDatabase.query(
                RATE_CONTENT,
                projection,
                sel.selection,
                sel.selectionArgs,
                order);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        parseUriCode(uri); // validate uri
        Uri result = null;
        long newId = rateDatabase.insert(RATE_CONTENT, contentValues);
        if (newId != -1) {
            result = ContentUris.withAppendedId(uri, newId);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        ItemSelect sel = ItemSelect.forUpdate()
                .withItemUriPredicate(isItemUri)
                .selectionFor(uri, selection, selectionArgs);

        int updated = rateDatabase.update(RATE_CONTENT, contentValues, sel.selection, sel.selectionArgs);
        if (updated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        ItemSelect sel = ItemSelect.forDelete()
                .withItemUriPredicate(isItemUri)
                .selectionFor(uri, selection, selectionArgs);

        int deleted = rateDatabase.delete(RATE_CONTENT, sel.selection, sel.selectionArgs);
        if (deleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleted;
    }
}
