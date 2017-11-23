package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public class UtilsTests {

    @Test
    public void testDataIntegerList() {
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
    public void testDataLongList() throws InterruptedException {
        final long count = 3500000*2;
        final DataListIterator<Data<Long>, Long> dl = new DataLongListIterator<Data<Long>>();
        final DataListIterator<Data<Long>, Long> dl2 = new DataLongListIterator<>();
        Assert.assertTrue(dl.equals(dl2));
        Assert.assertTrue(dl.isEmpty());
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
        dl.add(data);
        Assert.assertTrue(!dl.isEmpty());
        Assert.assertTrue(dl.contains(data));
        Assert.assertTrue(dl.hasNext());
        Assert.assertTrue(!dl.hasPrevious());
        Assert.assertTrue(dl.next().equals(data));
        Assert.assertTrue(!dl.hasPrevious());
        Assert.assertTrue(!dl.hasNext());
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
        dl.add(data2);
        Assert.assertTrue(dl.contains(data) && dl.contains(data2));
        Assert.assertTrue(dl.hasNext());
        Assert.assertTrue(dl.next().getId() == null);
        Assert.assertTrue(dl.hasPrevious());
        Assert.assertTrue(!dl.hasNext());
        Assert.assertTrue(dl.previous().getId() == 1L);
        Assert.assertTrue(!dl.hasPrevious());
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
            dl.addToList(data3);
        }
        Assert.assertTrue(dl.hasNext());
        dl.next();
        for (int index = 0; index < 10; index++) {
            Assert.assertTrue(dl.next().getId() == null);
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
                    synchronized (dl) {
                        dl.addToList(data2);
                        synchronized (dl2) {
                            dl2.addToList(data2);
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
        for (Collection<Data<Long>> collection : dl.allCollections()){
            Parallel.For(collection, new Parallel.Operation<Data<Long>>() {
                @Override
                public void perform(Data<Long> pParameter) {
                    System.out.println(pParameter);
                    synchronized (dl3) {
                        dl3.addToList(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
        Assert.assertTrue(dl.size().equals(dl3.size()));
        Assert.assertTrue(dl.equals(dl3));

        Assert.assertTrue(!dl.equals(dl2));
        Assert.assertTrue(dl.remove(data));
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
            Assert.assertTrue(dl.remove(data3));
        }
        Assert.assertTrue(!dl.contains(data));
        Assert.assertTrue(dl.remove(data2));
        Assert.assertTrue(dl.size().equals(dl2.size()));
        Assert.assertTrue(dl.equals(dl2));
        Assert.assertTrue(dl.remove(new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
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
        Assert.assertTrue(!dl.equals(dl2));
    }
}
