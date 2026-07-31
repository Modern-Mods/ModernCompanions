package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** Shared job gate/status bridge; each profession keeps its own discovery and action logic. */
abstract class ResumableJobGoal extends Goal {
    protected final JobLifecycle lifecycle = new JobLifecycle();
    private final AbstractHumanCompanionEntity worker;
    private final CompanionJob job;

    protected ResumableJobGoal(AbstractHumanCompanionEntity worker, CompanionJob job) {
        this.worker = worker;
        this.job = job;
    }

    protected final boolean workActive(boolean enabled) {
        if (!enabled || worker.getJob() != job || !worker.isWorkEnabled()) {
            lifecycle.pause("job_status.modern_companions.paused");
            worker.checkpointJob(JobPhase.PAUSED, worker.getJobCheckpointTarget().orElse(null));
            if (worker.getJob() == job) worker.setJobStatus("job_status.modern_companions.paused");
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
        worker.setJobStatus(status);
    }

    protected final boolean reserve(String key) {
        return !(worker.level() instanceof ServerLevel level)
                || JobReservations.claim(level, key, worker.getUUID(), level.getGameTime(), 20L * 60L * 10L);
    }
}
