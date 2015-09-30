package ppitol.exd.app.model.test;

import android.content.ContentUris;
import android.net.Uri;

import junit.framework.Assert;

/**
* The id (or rowid) and uri of a fixture inserted by some provider.
*/
public class FixtureInsertion {
    final Uri uri;
    final long id;

    FixtureInsertion(Uri dirUri, long id) {
        Assert.assertTrue(id != -1);
        this.id = id;
        this.uri = ContentUris.withAppendedId(dirUri, id);
    }
}
