class TaskManager {

    constructor() {
        this.tasks = Storage.loadTasks();
    }

    addTask(task) {
        this.tasks.push(task);
        Storage.saveTasks(this.tasks);
    }

    deleteTask(id) {
        this.tasks = this.tasks.filter(task => task.id !== id);
        Storage.saveTasks(this.tasks);
    }

    toggleComplete(id) {
        this.tasks = this.tasks.map(task => {
            if (task.id === id) {
                task.completed = !task.completed;
            }
            return task;
        });

        Storage.saveTasks(this.tasks);
    }

    getTasks() {
        return this.tasks;
    }
}