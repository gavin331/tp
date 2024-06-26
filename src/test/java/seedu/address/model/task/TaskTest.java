package seedu.address.model.task;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


public class TaskTest {
    @Test
    public void execute_taskList_success() {
        Task validTask = new Task(new TaskName("Test"), new TaskId(2), new TaskStatus(false),
                new AssignedEmployees(""));
        assertTrue(validTask.getName().equals("Test"));
        Task.setUniversalTaskId(100);
        assertTrue(validTask.getTaskId() == 2);
        assertTrue(validTask.getTaskStatus().toString().equals("In Progress"));
        Task.incrementTaskId();
        Task validTask2 = new Task(new TaskName("Test"), new TaskId(Task.getUniversalId()), new TaskStatus(false),
                new AssignedEmployees(""));
        assertTrue(validTask2.getTaskId() == 101);
    }
}
