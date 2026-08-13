package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** Shared job gate/status bridge; each profession keeps its own discovery and action logic. */
abstract class ResumableJobGoal extends Goal {
    protected final JobLifecycle lifecycle = new JobLifecycle();
    private final AbstractHumanCompanionEntity worker;
    private final CompanionJob job;
    private int lifecycleTick = Integer.MIN_VALUE;

    protected ResumableJobGoal(AbstractHumanCompanionEntity worker, CompanionJob job) {
        this.worker = worker;
        this.job = job;
    }

    protected final boolean workActive(boolean enabled) {
        tickLifecycle();
        if (!enabled || worker.getJob() != job || !worker.isWorkEnabled()) {
            lifecycle.pause("job_status.modern_companions.paused");
            worker.checkpointJob(JobPhase.PAUSED, worker.getJobCheckpointTarget().orElse(null));
            if (worker.getJob() == job) worker.setJobStatus("job_status.modern_companions.paused");
            return false;
        }
        if (worker.isJobReturnPending()) {
            // Delivery owns movement until the saved checkpoint is reached. A deferred
            // courier retry must not let a profession goal resume work from the chest.
            lifecycle.waitFor("job_status.modern_companions.returning");
            worker.setJobStatus("job_status.modern_companions.returning");
            return false;
        }
        if (lifecycle.phase() == JobPhase.PAUSED || lifecycle.phase() == JobPhase.WAITING) lifecycle.resume();
        return true;
    }

    protected final void phase(JobPhase phase, String status) {
        lifecycle.advance(phase);
        worker.checkpointJob(phase, worker.getJobCheckpointTarget().orElse(null));
        worker.setJobStatus(status);
    }

    protected final void phase(JobPhase phase, String status, net.minecraft.core.BlockPos target) {
        lifecycle.advance(phase);
        worker.checkpointJob(phase, target);
        worker.setJobStatus(status);
    }

    protected final void waiting(String status) {
        lifecycle.waitFor(status);
        worker.checkpointJob(JobPhase.WAITING, worker.getJobCheckpointTarget().orElse(null));
        worker.setJobStatus(status);
    }

    protected final boolean reserve(String key) {
        return !(worker.level() instanceof ServerLevel level)
                || JobReservations.claim(level, reservationType(key), key, worker.getUUID(), job.id(),
                level.getGameTime(), 20L * 60L * 10L);
    }

    protected final void release(String key) {
        if (worker.level() instanceof ServerLevel level) {
            JobReservations.release(level, reservationType(key), key, worker.getUUID());
        }
    }

    protected final boolean retryReady() {
        tickLifecycle();
        return lifecycle.retryReady();
    }

    protected final boolean retry(String status, int limit) {
        boolean allowed = lifecycle.retry(status, limit);
        worker.setJobStatus(status);
        if (!allowed) {
            lifecycle.waitFor(status);
            worker.checkpointJob(JobPhase.WAITING, worker.getJobCheckpointTarget().orElse(null));
        }
        return allowed;
    }

    private ReservationType reservationType(String key) {
        if (key == null) return ReservationType.BLOCK;
        int separator = key.indexOf(':');
        if (separator <= 0) return ReservationType.BLOCK;
        return switch (key.substring(0, separator)) {
            case "tree" -> ReservationType.COMPONENT;
            case "animal" -> ReservationType.ENTITY;
            case "drop" -> ReservationType.DROP;
            case "workstation" -> ReservationType.WORKSTATION;
            case "shore" -> ReservationType.SHORE;
            case "chest" -> ReservationType.CHEST;
            case "route" -> ReservationType.ROUTE;
            default -> ReservationType.BLOCK;
        };
    }

    private void tickLifecycle() {
        if (lifecycleTick != worker.tickCount) {
            lifecycle.tick();
            lifecycleTick = worker.tickCount;
        }
    }
}
