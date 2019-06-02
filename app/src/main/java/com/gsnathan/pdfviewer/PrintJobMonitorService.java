package com.gsnathan.pdfviewer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.print.PrintJob;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PrintJobMonitorService extends Service implements Runnable {
    private static final int POLL_PERIOD=3;
    private PrintManager mgr=null;
    private ScheduledExecutorService executor=
            Executors.newSingleThreadScheduledExecutor();
    private long lastPrintJobTime=SystemClock.elapsedRealtime();

    @Override
    public void onCreate() {
        super.onCreate();

        mgr=(PrintManager)getSystemService(PRINT_SERVICE);
        executor.scheduleAtFixedRate(this, POLL_PERIOD, POLL_PERIOD,
                TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return(super.onStartCommand(intent, flags, startId));
    }

    @Override
    public void onDestroy() {
        executor.shutdown();

        super.onDestroy();
    }

    @Override
    public void run() {
        for (PrintJob job : mgr.getPrintJobs()) {
            if (job.getInfo().getState() == PrintJobInfo.STATE_CREATED
                    || job.isQueued() || job.isStarted()) {
                lastPrintJobTime=SystemClock.elapsedRealtime();
            }
        }

        long delta=SystemClock.elapsedRealtime() - lastPrintJobTime;

        if (delta > POLL_PERIOD * 2) {
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return(null);
    }
}
