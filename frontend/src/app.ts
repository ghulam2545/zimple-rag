const BASE_URL = 'http://localhost:8080/backend/api/v1';
const convId =
    localStorage.getItem("zimple-conv-id") ||
    `conv_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

localStorage.setItem("zimple-conv-id", convId);

document.getElementById("convId")!.textContent =
    convId.slice(0, 18) + "...";

const messagesEl = document.getElementById("messages")!;
const inputEl = document.getElementById("queryInput") as HTMLTextAreaElement;
const sendBtn = document.getElementById("sendBtn") as HTMLButtonElement;
const sourcesList = document.getElementById("sourcesList")!;
const fileInput = document.getElementById("fileInput") as HTMLInputElement;
const uploadZone = document.getElementById("uploadZone")!;

function addMessage(
    role: "user" | "assistant",
    text: string,
    sources: any[] = []
) {
    const wrap = document.createElement("div");

    wrap.className = `message ${role}`;

    wrap.innerHTML = `
        <div class="avatar">${role === "user" ? "U" : "Z"}</div>
        <div class="bubble">${formatMarkdown(text)}</div>
    `;

    messagesEl.appendChild(wrap);
    messagesEl.scrollTop = messagesEl.scrollHeight;

    if (sources.length) {
        renderSources(sources);
    }
}

function formatMarkdown(text: string) {
    const html = text
        .replace(
            /```([\s\S]*?)```/g,
            "<pre><code>$1</code></pre>"
        )
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

    return `<p>${html}</p>`;
}

function renderSources(sources: any[]) {
    sourcesList.innerHTML = "";

    sources.forEach((s) => {
        const div = document.createElement("div");

        div.className = "source-card";

        div.innerHTML = `
            <div class="path">${s.filePath}</div>
            <div class="heading">${s.heading || "-"}</div>
            <div class="score">
                similarity: ${(s.score * 100).toFixed(1)}% - ${s.source}
            </div>
        `;

        sourcesList.appendChild(div);
    });
}

async function sendQuery() {
    const q = inputEl.value.trim();

    if (!q) {
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
                conversationId: convId,
                query: q
            })
        });

        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.error || "Failed");
        }

        addMessage(
            "assistant",
            data.answer,
            data.sources
        );
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
    const form = new FormData();

    files.forEach((f) => {
        form.append("files", f);
    });

    uploadZone.innerHTML = "<p>Uploading...</p>";

    try {
        const res = await fetch(
            "/api/ingest/upload/bulk",
            {
                method: "POST",
                body: form
            }
        );

        const data = await res.json();

        const totalChunks = data.reduce(
            (a: number, b: any) => a + b.chunks,
            0
        );

        addMessage(
            "assistant",
            `Ingested ${data.length} MD file(s) -> ${totalChunks} chunks indexed in PGVector.`
        );

        updateStats();
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

async function updateStats() {
    try {
        const res = await fetch("/api/health");
        const h = await res.json();

        document.getElementById("kb-stats")!.innerHTML = `
            PG: ${h.postgres}<br>
            Redis: ${h.redis}<br>
            MD-only: ${h.mdOnly ? "YES" : "NO"}
        `;
    } catch {
        // Ignore health check failure.
    }
}

document
    .getElementById("clearBtn")!
    .addEventListener("click", async () => {
        await fetch(`/api/chat/${convId}`, {
            method: "DELETE"
        });

        messagesEl.innerHTML = "";

        sourcesList.innerHTML =
            '<p class="empty">Cleared.</p>';

        addMessage(
            "assistant",
            "Conversation cleared. Ask something new."
        );
    });

document
    .getElementById("ingestDirBtn")!
    .addEventListener("click", async () => {
        addMessage(
            "assistant",
            "Ingesting ./knowledge directory..."
        );

        const res = await fetch(
            "/api/ingest/directory",
            {
                method: "POST"
            }
        );

        const data = await res.json();

        addMessage(
            "assistant",
            `Ingested ${data.processed}/${data.total} files -> ${data.totalChunks} chunks`
        );

        updateStats();
    });

updateStats();