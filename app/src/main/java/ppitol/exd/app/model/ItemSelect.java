package ppitol.exd.app.model;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.google.common.base.Predicate;

/**
 * Fix the selection clause when querying for individual items by prepending the restriction for
 * the id to the original selection clause, or set the selection clause to "1" when it is empty
 * in a delete all operation.
 */
public class ItemSelect {

    public static class Builder {
        private String idColumnName = BaseColumns._ID;
        private Predicate<Uri> isItemUri;
        private final boolean forDelete;

        private Builder(boolean forDelete) {
            this.forDelete = forDelete;
        }


        public Builder withIdColumnName(String idColumnName) {
            this.idColumnName = idColumnName;
            return this;
        }

        public Builder withItemUriPredicate(Predicate<Uri> isItemUri) {
            this.isItemUri = isItemUri;
            return this;
        }

        public ItemSelect selectionFor(Uri uri, String originalSelection, String[] originalArgs) {
            return new ItemSelect(idColumnName, uri, isItemUri.apply(uri), originalSelection, originalArgs, forDelete);
        }
    }

    public static Builder forQuery() {
        return new Builder(false);
    }

    public static Builder forUpdate() {
        return new Builder(false);
    }

    public static Builder forDelete() {
        return new Builder(true);
    }

    public final String selection;
    public final String[] selectionArgs;

    /**
     * It's ugly, but update and delete operations become much more readable with this.
     */
    private ItemSelect(
            String idColumnName,
            Uri uri,
            boolean forItem,
            String originalSelection,
            String[] originalArgs,
            boolean forDelete) {

        String sel = originalSelection;
        String[] args = originalArgs;

        if (forItem) {
            // Add criteria by id to the select clause
            StringBuilder sb = new StringBuilder(idColumnName + " = ?");
            if (!TextUtils.isEmpty(originalSelection)) {
                sb.append(" and (").append(originalSelection).append(")");
            }
            sel = sb.toString();

            // Add the id (from the uri) to the parameters
            if (originalArgs != null && originalArgs.length > 0) {
                args = new String[originalArgs.length + 1];
                System.arraycopy(originalArgs, 0, args, 1, originalArgs.length);
            } else {
                args = new String[1];
            }
            long id = ContentUris.parseId(uri);
            args[0] = Long.toString(id);

        } else if (forDelete && TextUtils.isEmpty(originalSelection)) {
            sel = "1";
            args = null;
        }

        this.selection = sel;
        this.selectionArgs = args;
    }
}
