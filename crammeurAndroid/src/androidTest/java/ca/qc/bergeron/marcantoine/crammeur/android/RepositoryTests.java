package ca.qc.bergeron.marcantoine.crammeur.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Client;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.Repository;

/**
 * Created by Marc-Antoine on 2017-06-19.
 */
@RunWith(AndroidJUnit4.class)
public class RepositoryTests {

    private Repository repo;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        repo = null;
        System.gc();
    }

    @Before
    public void setUp() throws Exception {
        repo = new ca.qc.bergeron.marcantoine.crammeur.android.repository.Repository(InstrumentationRegistry.getContext());
    }

    @Test
    public void testSave() throws KeyException {
        for (int i = 1; i <= 10000; i++) {
            Client c = new Client();
            c.Id = i;
            c.Name = "Test";
            c.EMail = "test" + c.Id.toString() + "@gmail.com";
            repo.save(c);
        }
        Assert.assertEquals(10000,repo.getAllKeys(Client.class).size());
    }

    @Test
    public void testGetAll() throws KeyException {
        for (int i = 1; i <= 10000; i++) {
            Client c = new Client();
            c.Id = i;
            c.Name = "Test";
            c.EMail = "test" + c.Id.toString() + "@gmail.com";
            if (!repo.contains(Client.class,c.Id))
                repo.save(c);
        }
        long start = System.currentTimeMillis();
        Assert.assertEquals(repo.getAll(Client.class).size(),10000);
        long end = System.currentTimeMillis();
        long delta = end-start;
        Assert.assertTrue(delta <= 2000);
    }

    @Test
    public void testDelete() {
        repo.clear(Client.class);
        Assert.assertEquals(repo.getAllKeys(Client.class).size(),0);
    }

    @Test
    public void test() {
        Client client = new Client();
        client.Id = null;
        client.Name = "Test";
        client.EMail = "test1@gmail.com";
        if (repo.contains(Client.class,client)) {
            Assert.assertTrue(1 == repo.getKey(Client.class, client));
            Log.i("test", "1");
        } else {
            Assert.assertTrue(null == repo.getKey(Client.class,client));
            Log.i("test","null");
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
