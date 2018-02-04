package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.utils;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.LongListIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Parallel;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public class UtilsTests {

    @Test
    public void testDataIntegerListIterator() {
    }

    @Test
    public void testDataLongListIterator() throws InterruptedException {
        final long count = 10000;
        final ListIterator<Data<Long>, Long> dli = new LongListIterator<Data<Long>>();
        final ListIterator<Data<Long>, Long> dli2 = new LongListIterator<>();
        Assert.assertTrue(dli.equals(dli2));
        Assert.assertTrue(dli.isEmpty());
        Data<Long> data = new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
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
        Assert.assertTrue(dli.next().getId() == 1L);
        Assert.assertTrue(dli.hasPrevious());
        Assert.assertTrue(dli.previous().getId() == null);
        Assert.assertTrue(!dli.hasPrevious());
        Assert.assertTrue(dli.hasNext());
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
            Assert.assertTrue(dli.addAtEnd(data3));
        }
        Assert.assertTrue(!dli.hasPrevious());
        dli.next();
        for (int index = 0; index < 10; index++) {
            Assert.assertTrue(dli.next().getId() == null);
        }

        final ExecutorService exec = Executors.newSingleThreadExecutor();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (long index = 0; index < count; index++) {
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
                    data3.setId(index);
                    synchronized (dli) {
                        dli.addAtEnd(data3);

                    }
                    synchronized (dli2) {
                        dli2.addAtEnd(data3);
                    }
                    System.out.println("Index : " + String.valueOf(index));
                }
            }
        };
        try {
            exec.submit(runnable);
        } finally {
            exec.shutdown();
            while (!exec.isTerminated()) {
                Thread.sleep(0,1);
            }
        }
        final LongListIterator<Data<Long>> dli3 = new LongListIterator<>();
        Parallel.For(dli2, new Parallel.Operation<Data<Long>>() {
            @Override
            public void perform(Data<Long> pParameter) {
                System.out.println(pParameter);
                synchronized (dli3) {
                    dli3.addAtEnd(pParameter);
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        });
        /*for(List<Data<Long>> list  : dli.<List<Data<Long>>>allCollections()) {
            Parallel.For((Iterable<? extends Data<Long>>) list, new Parallel.Operation<Data<Long>>() {
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
        }*/
        //Assert.assertTrue(dli.size().equals(dl3.size()));
        //Assert.assertTrue(dli.equals(dl3));

        //Assert.assertTrue(!dli.equals(dli2));
        Collection<Data<Long>> collection = dli.getCollection();
        Assert.assertTrue(collection.remove(data));
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
            Assert.assertTrue(collection.remove(data3));
        }

        Assert.assertTrue(!collection.contains(data));
        Assert.assertTrue(collection.remove(data2));
        Assert.assertTrue(collection.size() == dli2.nextCollection().size());
        Assert.assertTrue(dli.equals(dli2));
        System.out.println("dli equals dli2");
        final long i = count-1;
        Data<Long> data3 = new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {

            Long Id = null;

            {
                setId(i);
            }

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
        Assert.assertTrue(collection.remove(data3));
        System.out.println("Remove last data");
        Assert.assertTrue(!collection.contains(data3));
        Assert.assertTrue(!dli.equals(dli2));
        Assert.assertTrue(dli2.getCollection().addAll(dli2.size().intValue()/2, new ArrayList<>(dli2.getCollection())));
        Assert.assertTrue(dli3.nextCollection().addAll(dli3.size().intValue()/2, new ArrayList<>(dli3.getCollection())));
        Assert.assertTrue(dli2.equals(dli3));
        final LongListIterator<Data<Long>> dli4 = new LongListIterator<>(dli3);
        Assert.assertTrue(dli4.equals(dli3));
    }

    @Test
    public void testKeyLongSetIterator() {
        /*final SetIterator<Long> ksi = new LongSetIterator();
        SetIterator<Long> ksi2 = new LongSetIterator();
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
            //Is ok
        }
        ksi.clear();
        Assert.assertTrue(ksi.isEmpty());
        final long count = 3500000;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (long getIndex = 0; getIndex < count; getIndex++) {
                    synchronized (ksi) {
                        ksi.add(getIndex);
                    }
                    System.out.println("Index : " + String.valueOf(getIndex));
                }
            }
        };
        try {
            executorService.submit(runnable);
        } finally {
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                try {
                    Thread.sleep(0,1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        Assert.assertTrue(ksi.size().equals(count));*/
    }

    @Test
    public void testDataLongMap() {

    }

    @Test
    public void testParallelExecute() {
        Parallel.Running running = new Parallel.Running() {

            @Override
            public boolean restartPrevious() {
                int random;
                do {
                    random = new Random().nextInt(3);
                    System.out.println(String.valueOf(random) + " Restart");
                } while (random == 1);
                return random == 0;
            }

            @Nullable
            @Override
            public Parallel.Running<?> previousRun() {
                return this;
            }

            @Override
            public Object actualParam() {
                return null;
            }

            @Override
            public boolean startNext() {
                int random;
                do {
                    random = new Random().nextInt(3);
                    System.out.println(String.valueOf(random) + " Start");
                } while (random == 1);
                return random == 0;
            }

            @Nullable
            @Override
            public Parallel.Running<?> nextRun() {
                return this;
            }

            @Override
            public void perform(Object pParameter) {

            }

            @Override
            public boolean follow() {
                return true;
            }
        };
        Parallel.Execute(running, null);
    }
}
