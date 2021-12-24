package com.fthiery.go4lunch.TestUtils;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class RxSchedulerRule implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerCallable
                        -> TrampolineScheduler.instance());
                RxJavaPlugins.setIoSchedulerHandler(scheduler
                        -> TrampolineScheduler.instance());
                RxJavaPlugins.setComputationSchedulerHandler(scheduler ->
                        TrampolineScheduler.instance());

                try{
                    base.evaluate();
                }finally {
                    RxAndroidPlugins.reset();
                    RxJavaPlugins.reset();
                }

            }
        };
    }
}
