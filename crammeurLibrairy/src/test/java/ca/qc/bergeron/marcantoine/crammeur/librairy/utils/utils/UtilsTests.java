package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.utils;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.ContainsException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataIntegerListIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataLongListIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.KeyLongSetIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.KeySetIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Parallel;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public class UtilsTests {

    @Test
    public void testDataIntegerListIterator() {
        DataListIterator<Data<Integer>, Integer> dc;
        dc = new DataIntegerListIterator<Data<Integer>>();
        Data<Integer> data = new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Integer>() {
            Integer Id = null;

            @Nullable
            @Override
            public Integer getId() {
                return Id;
            }

            @Override
            public void setId(@Nullable Integer pId) {
                Id = pId;
            }
        };
        dc.add(data);
        Assert.assertTrue(dc.contains(data));
    }

    @Test
    public void testDataLongListIterator() throws InterruptedException {
        final long count = 3500000*2;
        final DataListIterator<Data<Long>, Long> dli = new DataLongListIterator<Data<Long>>();
        final DataListIterator<Data<Long>, Long> dli2 = new DataLongListIterator<>();
        Assert.assertTrue(dli.equals(dli2));
        Assert.assertTrue(dli.isEmpty());
        final Data<Long> data = new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
            Long Id = null;

            @Nullable
            @Override
            public Long getId() {
                return Id;
            }

            @Override
            public void setId(@Nullable Long pId) {
                this.Id = pId;
            }
        };
        dli.add(data);
        Assert.assertTrue(!dli.isEmpty());
        Assert.assertTrue(dli.contains(data));
        Assert.assertTrue(dli.hasNext());
        Assert.assertTrue(!dli.hasPrevious());
        Assert.assertTrue(dli.next().equals(data));
        Assert.assertTrue(!dli.hasPrevious());
        Assert.assertTrue(!dli.hasNext());
        Data<Long> data2 = new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
            Long Id = null;

            @Nullable
            @Override
            public Long getId() {
                return Id;
            }

            @Override
            public void setId(@Nullable Long pId) {
                this.Id = pId;
            }
        };
        data2.setId(1L);
        dli.add(data2);
        Assert.assertTrue(dli.contains(data) && dli.contains(data2));
        Assert.assertTrue(dli.hasNext());
        Assert.assertTrue(dli.next().getId() == null);
        Assert.assertTrue(dli.hasPrevious());
        Assert.assertTrue(!dli.hasNext());
        Assert.assertTrue(dli.previous().getId() == 1L);
        Assert.assertTrue(!dli.hasPrevious());
        for (int index = 0; index < 10; index++) {
            Data<Long> data3 = new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
                Long Id = null;

                @Nullable
                @Override
                public Long getId() {
                    return Id;
                }

                @Override
                public void setId(@Nullable Long pId) {
                    this.Id = pId;
                }
            };
            dli.addAtEnd(data3);
        }
        Assert.assertTrue(dli.hasNext());
        dli.next();
        for (int index = 0; index < 10; index++) {
            Assert.assertTrue(dli.next().getId() == null);
        }

        final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
        try {
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                for (long index = 0; index < count; index++) {
                    final long i = index;
                    final Data<Long> data2 = new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
                        Long Id = i;

                        @Nullable
                        @Override
                        public Long getId() {
                            return Id;
                        }

                        @Override
                        public void setId(@Nullable Long pId) {
                            this.Id = pId;
                        }
                    };
                    synchronized (dli) {
                        dli.addAtEnd(data2);
                        synchronized (dli2) {
                            dli2.addAtEnd(data2);
                        }
                    }
                    System.out.println("Index : " + String.valueOf(index));
                }
                }
            };
            exec.submit(runnable);


        } finally {
            exec.shutdown();
            while (!exec.isTerminated()) {
                Thread.sleep(0,1);
            }
        }
        final DataLongListIterator<Data<Long>> dl3 = new DataLongListIterator<>();
        for (Collection<Data<Long>> collection : dli.allCollections()){
            Parallel.For(collection, new Parallel.Operation<Data<Long>>() {
                @Override
                public void perform(Data<Long> pParameter) {
                    System.out.println(pParameter);
                    synchronized (dl3) {
                        dl3.addAtEnd(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
        Assert.assertTrue(dli.size().equals(dl3.size()));
        Assert.assertTrue(dli.equals(dl3));

        Assert.assertTrue(!dli.equals(dli2));
        Assert.assertTrue(dli.remove(data));
        for (int index = 0; index < 10; index++) {
            Data<Long> data3 = new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
                Long Id = null;

                @Nullable
                @Override
                public Long getId() {
                    return Id;
                }

                @Override
                public void setId(@Nullable Long pId) {
                    this.Id = pId;
                }
            };
            Assert.assertTrue(dli.remove(data3));
        }
        Assert.assertTrue(!dli.contains(data));
        Assert.assertTrue(dli.remove(data2));
        Assert.assertTrue(dli.size().equals(dli2.size()));
        Assert.assertTrue(dli.equals(dli2));
        Assert.assertTrue(dli.remove(new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
            Long Id = count-1;

            @Nullable
            @Override
            public Long getId() {
                return Id;
            }

            @Override
            public void setId(@Nullable Long pId) {

            }
        }));
        Assert.assertTrue(!dli.equals(dli2));
    }

    @Test
    public void testKeyLongSetIterator() {
        KeySetIterator<Long> ksi = new KeyLongSetIterator();
        KeySetIterator<Long> ksi2 = new KeyLongSetIterator();
        Assert.assertTrue(ksi.equals(ksi2));
        Assert.assertTrue(ksi.isEmpty() && ksi2.isEmpty());
        ksi.add(0L);
        Assert.assertTrue(ksi.contains(0L));
        Assert.assertTrue(!ksi.equals(ksi2));
        ksi2.add(1L);
        Assert.assertTrue(ksi2.contains(1L));
        Assert.assertTrue(!ksi2.equals(ksi));
        Assert.assertTrue(ksi.size().equals(ksi2.size()));
        Assert.assertTrue(ksi2.remove(1L));
        Assert.assertTrue(ksi2.isEmpty());
        ksi2.add(0L);
        Assert.assertTrue(ksi.equals(ksi2));
        try {
            ksi.add(0L);
            Assert.fail();
        } catch (ContainsException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testDataLongMap() {

    }
}
