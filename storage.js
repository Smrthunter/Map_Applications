class Storage {
    static saveTasks(tasks) {
        localStorage.setItem("tasks", JSON.stringify(tasks));
    }

    static loadTasks() {
        return JSON.parse(localStorage.getItem("tasks")) || [];
    }
}