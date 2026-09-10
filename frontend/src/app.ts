const BASE_URL = 'http://localhost:8080/backend/api/v1';

// Default document scope
const DEFAULT_SCOPE = {
    workspace: "workspace",
    userId: "userId",
    filename: "filename.md"
};

// Currently selected file for chat.
let selectedFile: { workspace: string; userId: string; filename: string } | null = null;

const convId =
    localStorage.getItem("zimple-conv-id") ||
    `conv_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

localStorage.setItem("zimple-conv-id", convId);

document.getElementById("convId")!.textContent =
    convId.slice(0, 18) + "...";

const messagesEl = document.getElementById("messages")!;
const inputEl = document.getElementById("queryInput") as HTMLTextAreaElement;
const sendBtn = document.getElementById("sendBtn") as HTMLButtonElement;
const fileInput = document.getElementById("fileInput") as HTMLInputElement;
const uploadZone = document.getElementById("uploadZone")!;
const filesListEl = document.getElementById("filesList")!;
const activeFileBar = document.getElementById("activeFileBar")!;

function escapeHtml(text: string): string {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

function addMessage(
    role: "user" | "assistant",
    text: string
) {
    const wrap = document.createElement("div");

    wrap.className = `message ${role}`;

    const safeText = role === "user" ? escapeHtml(text) : formatMarkdown(text);

    wrap.innerHTML = `
        <div class="avatar">${role === "user" ? "U" : "Z"}</div>
        <div class="bubble">${safeText}</div>
    `;

    messagesEl.appendChild(wrap);
    messagesEl.scrollTop = messagesEl.scrollHeight;
}

function formatMarkdown(text: string) {
    const escaped = escapeHtml(text);

    const codeBlocks: string[] = [];
    const withPlaceholders = escaped.replace(
        /```(\w*)\n?([\s\S]*?)```/g,
        (_match, lang, code) => {
            const trimmed = code.replace(/^\n/, "").replace(/\n$/, "");
            codeBlocks.push(
                `<pre><code${lang ? ` class="language-${lang}"` : ""}>${trimmed}</code></pre>`
            );
            return `\x00CB${codeBlocks.length - 1}\x00`;
        }
    );

    // Process inline markdown on the non-code text.
    const formatted = withPlaceholders
        .replace(
            /`([^`]+)`/g,
            "<code>$1</code>"
        )
        .replace(
            /\*\*([^*]+)\*\*/g,
            "<strong>$1</strong>"
        )
        .replace(
            /\[source: ([^\]]+)]/g,
            '<span class="tag">FILE $1</span>'
        )
        .replace(/\n/g, "<br>");

    const restored = formatted.replace(
        /\x00CB(\d+)\x00/g,
        (_match, idx) => codeBlocks[Number(idx)]
    );

    return restored;
}

// ─── File selection ─────────────────────────────────────────────

function selectFile(workspace: string, userId: string, filename: string) {
    selectedFile = {workspace, userId, filename};

    // Update the active-file bar above the composer.
    activeFileBar.innerHTML = `
        <span class="active-label">Chatting with: <strong>${escapeHtml(filename)}</strong></span>
    `;

    // Update placeholder.
    inputEl.placeholder = `Ask about ${filename}...`;

    // Highlight the active item in the right panel.
    filesListEl.querySelectorAll(".file-item").forEach((el) => {
        el.classList.toggle(
            "active",
            el.getAttribute("data-filename") === filename &&
            el.getAttribute("data-workspace") === workspace &&
            el.getAttribute("data-userid") === userId
        );
    });
}

// ─── Chat ───────────────────────────────────────────────────────

async function sendQuery() {
    const q = inputEl.value.trim();

    if (!q) {
        return;
    }

    if (!selectedFile) {
        addMessage("assistant", "Please select a file from the right panel first.");
        return;
    }

    addMessage("user", q);

    inputEl.value = "";
    inputEl.style.height = "auto";
    sendBtn.disabled = true;

    try {
        const res = await fetch(BASE_URL + "/chat", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                documentScope: selectedFile,
                conversationId: convId,
                query: q
            })
        });

        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.message || "Failed");
        }

        addMessage("assistant", data.answer);
    } catch (e) {
        const message =
            e instanceof Error
                ? e.message
                : "Unknown error";

        addMessage(
            "assistant",
            `Error: ${message}. Is backend running at :8080?`
        );
    } finally {
        sendBtn.disabled = false;
        inputEl.focus();
    }
}

sendBtn.addEventListener("click", sendQuery);

inputEl.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendQuery();
    }
});

inputEl.addEventListener("input", () => {
    inputEl.style.height = "auto";
    inputEl.style.height =
        inputEl.scrollHeight + "px";
});

// ─── Upload ─────────────────────────────────────────────────────

uploadZone.addEventListener("click", () => {
    fileInput.click();
});

uploadZone.addEventListener("dragover", (e) => {
    e.preventDefault();
    uploadZone.style.borderColor = "#0c0a09";
});

uploadZone.addEventListener("dragleave", () => {
    uploadZone.style.borderColor = "";
});

uploadZone.addEventListener("drop", async (e) => {
    e.preventDefault();
    uploadZone.style.borderColor = "";

    const files = Array.from(e.dataTransfer!.files).filter(
        (f) =>
            f.name.endsWith(".md") ||
            f.name.endsWith(".markdown")
    );

    if (files.length) {
        await uploadFiles(files);
    }
});

fileInput.addEventListener("change", async () => {
    if (fileInput.files?.length) {
        await uploadFiles(Array.from(fileInput.files));
    }
});

async function uploadFiles(files: File[]) {
    uploadZone.innerHTML = "<p>Uploading...</p>";

    try {
        if (files.length === 1) {
            const form = new FormData();
            const scope = {...DEFAULT_SCOPE, filename: files[0].name};

            form.append("file", files[0]);
            form.append(
                "documentScope",
                new Blob([JSON.stringify(scope)], {type: "application/json"})
            );

            const res = await fetch(BASE_URL + "/upload", {
                method: "POST",
                body: form
            });

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.message || "Upload failed");
            }

            addMessage(
                "assistant",
                `Ingested **${data.fileName}** → ${data.chunks} chunks (${data.status})`
            );
        } else {
            const form = new FormData();
            const scope = {...DEFAULT_SCOPE, filename: files.map(f => f.name).join(",")};

            files.forEach((f) => {
                form.append("files", f);
            });

            form.append(
                "documentScope",
                new Blob([JSON.stringify(scope)], {type: "application/json"})
            );

            const res = await fetch(BASE_URL + "/bulk/upload", {
                method: "POST",
                body: form
            });

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.message || "Upload failed");
            }

            const totalChunks = data.reduce(
                (a: number, b: any) => a + b.chunks,
                0
            );

            addMessage(
                "assistant",
                `Ingested ${data.length} MD file(s) → ${totalChunks} chunks indexed.`
            );
        }

        updateStats();
        loadIngestedFiles();
    } catch (e) {
        const message =
            e instanceof Error
                ? e.message
                : "Unknown error";

        addMessage(
            "assistant",
            `Upload failed: ${message}`
        );
    } finally {
        uploadZone.innerHTML = `
            <p>
                Drop MD files here<br>
                <span>or click to upload</span>
            </p>
        `;
    }
}

// ─── Stats & Files ──────────────────────────────────────────────

async function updateStats() {
    try {
        const res = await fetch(BASE_URL + "/health");
        const h = await res.json();

        document.getElementById("kb-stats")!.innerHTML = `
            PG: ${escapeHtml(h.postgres || "unknown")}<br>
            Redis: ${escapeHtml(h.redis || "unknown")}
        `;
    } catch {
        document.getElementById("kb-stats")!.textContent = "Backend offline";
    }
}

async function loadIngestedFiles() {
    try {
        const pageNumber = 1;
        const pageSize = 10;

        const res = await fetch(
            `${BASE_URL}/files?pageNumber=${pageNumber}&pageSize=${pageSize}`
        );

        if (!res.ok) {
            filesListEl.innerHTML = '<p class="empty">Failed to load files.</p>';
            return;
        }

        const files: any[] = await res.json();

        if (!files.length) {
            filesListEl.innerHTML = '<p class="empty">No files yet. Upload some MD files.</p>';
            return;
        }

        filesListEl.innerHTML = "";

        files.forEach((f) => {
            const div = document.createElement("div");
            div.className = "file-item";

            const filename = f.filename || "unknown";
            const workspace = f.workspace || "—";
            const userId = f.user_id || "—";
            const timestamp = f.created_timestamp
                ? new Date(f.created_timestamp).toLocaleDateString()
                : "";

            // Store data attributes for selection matching.
            div.setAttribute("data-filename", filename);
            div.setAttribute("data-workspace", workspace);
            div.setAttribute("data-userid", userId);

            // Highlight if already selected.
            if (
                selectedFile &&
                selectedFile.filename === filename &&
                selectedFile.workspace === workspace &&
                selectedFile.userId === userId
            ) {
                div.classList.add("active");
            }

            div.innerHTML = `
                <div class="file-icon">MD</div>
                <div class="file-info">
                    <div class="file-name" title="${escapeHtml(filename)}">${escapeHtml(filename)}</div>
                    <div class="file-meta">${escapeHtml(workspace)}${timestamp ? " · " + timestamp : ""}</div>
                </div>
            `;

            div.addEventListener("click", () => {
                selectFile(workspace, userId, filename);
            });

            filesListEl.appendChild(div);
        });
    } catch {
        filesListEl.innerHTML = '<p class="empty">Backend offline.</p>';
    }
}

// ─── Clear conversation ─────────────────────────────────────────

document
    .getElementById("clearBtn")!
    .addEventListener("click", () => {
        localStorage.removeItem("zimple-conv-id");

        messagesEl.innerHTML = "";

        const newConvId = `conv_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
        localStorage.setItem("zimple-conv-id", newConvId);
        document.getElementById("convId")!.textContent =
            newConvId.slice(0, 18) + "...";

        location.reload();
    });

// ─── Init ───────────────────────────────────────────────────────

updateStats();
loadIngestedFiles();