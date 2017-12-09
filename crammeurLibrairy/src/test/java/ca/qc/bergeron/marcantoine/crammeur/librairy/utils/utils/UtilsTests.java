package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.utils;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataIntegerListIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.LongListIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Parallel;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public class UtilsTests {

    @Test
    public void testDataIntegerListIterator() {
        ListIterator<Data<Integer>, Integer> dc;
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
        final long count = 3500000;
        final ListIterator<Data<Long>, Long> dli = new LongListIterator<Data<Long>>();
        final ListIterator<Data<Long>, Long> dli2 = new LongListIterator<>();
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
        Assert.assertTrue(dli.hasPrevious());
        Assert.assertTrue(dli.previous().getId() == 1L);
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
        for (int index = 0; index < 10; index++) {
            Assert.assertTrue(dli.next().getId() == null);
        }

        final ExecutorService exec = Executors.newSingleThreadExecutor();
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

                    }
                    synchronized (dli2) {
                        dli2.addAtEnd(data2);
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
        final LongListIterator<Data<Long>> dl3 = new LongListIterator<>();
        for(List<Data<Long>> list  : dli.<List<Data<Long>>>allCollections()) {
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
        }
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
        System.out.println("dil equals dli2");
        Assert.assertTrue(collection.remove(new ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data<Long>() {
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
        System.out.println("Remove last data");
        Assert.assertTrue(!dli.equals(dli2));
        ListIterator<Data<Long>,Long> li =  dli2.<Data<Long>>listIterator(dli2.size()/2);
        Assert.assertTrue(li.size().equals(dli2.size()/2));
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
