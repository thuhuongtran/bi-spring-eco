package task.batch;

import org.springframework.cloud.task.listener.TaskExecutionListener;
import org.springframework.cloud.task.repository.TaskExecution;

public class TaskListener implements TaskExecutionListener {
    @Override
    public void onTaskStartup(TaskExecution taskExecution) {

    }

    @Override
    public void onTaskEnd(TaskExecution taskExecution) {

    }

    @Override
    public void onTaskFailed(TaskExecution taskExecution, Throwable throwable) {

    }
}
