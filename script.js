const taskManager = new TaskManager();

const taskForm = document.getElementById("taskForm");
const taskList = document.getElementById("taskList");
const searchInput = document.getElementById("searchInput");

function renderTasks() {

    taskList.innerHTML = "";

    let tasks = taskManager.getTasks();

    const searchText = searchInput.value.toLowerCase();

    tasks = tasks.filter(task =>
        task.title.toLowerCase().includes(searchText)
    );

    tasks.forEach(task => {

        const card = document.createElement("div");

        card.className =
            `task-card priority-${task.priority}`;

        if (task.completed) {
            card.classList.add("completed");
        }

        card.innerHTML = `
            <h3>${task.title}</h3>
            <p>${task.description}</p>
            <p>${task.date}</p>

            <div class="task-actions">
                <button onclick="completeTask(${task.id})">
                    Complete
                </button>

                <button onclick="deleteTask(${task.id})">
                    Delete
                </button>
            </div>
        `;

        taskList.appendChild(card);
    });

    updateStats();
}

function updateStats() {

    const tasks = taskManager.getTasks();

    document.getElementById("totalTasks").textContent =
        tasks.length;

    document.getElementById("completedTasks").textContent =
        tasks.filter(t => t.completed).length;

    document.getElementById("pendingTasks").textContent =
        tasks.filter(t => !t.completed).length;
}

taskForm.addEventListener("submit", function(e) {

    e.preventDefault();

    const task = {
        id: Date.now(),
        title: document.getElementById("taskTitle").value,
        description: document.getElementById("taskDescription").value,
        date: document.getElementById("taskDate").value,
        priority: document.getElementById("taskPriority").value,
        completed: false
    };

    taskManager.addTask(task);

    taskForm.reset();

    renderTasks();
});

function deleteTask(id) {
    taskManager.deleteTask(id);
    renderTasks();
}

function completeTask(id) {
    taskManager.toggleComplete(id);
    renderTasks();
}

searchInput.addEventListener("input", renderTasks);

document
.getElementById("darkModeToggle")
.addEventListener("click", () => {
    document.body.classList.toggle("dark-mode");
});

renderTasks();