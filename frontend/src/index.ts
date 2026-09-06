import * as http from "node:http";
import * as path from "node:path";
import * as fs from "node:fs";

const PORT = Number(process.env.UI_PORT) || 3000;

// All HTML/CSS/static files are inside the pages directory.
const PAGES_DIR = path.join(process.cwd(), "pages");
const DIST_DIR = path.join(process.cwd(), "dist");

const MIME: Record<string, string> = {
    ".html": "text/html",
    ".css": "text/css",
    ".js": "application/javascript",
    ".json": "application/json",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".svg": "image/svg+xml",
    ".ico": "image/x-icon",
    ".woff": "font/woff",
    ".woff2": "font/woff2"
};

function serveFile(res: http.ServerResponse, filePath: string) {
    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME[ext] || "application/octet-stream";

    fs.readFile(filePath, (err, data) => {
        if (err) {
            res.writeHead(404, {
                "Content-Type": "text/plain"
            });

            res.end("Not Found");
            return;
        }

        res.writeHead(200, {
            "Content-Type": contentType,
            "Cache-Control": "no-cache"
        });

        res.end(data);
    });
}

const server = http.createServer((req, res) => {
    // Allow requests from your Spring Boot backend / other origins.
    res.setHeader("Access-Control-Allow-Origin", "*");

    // req.url can contain query parameters.
    // Example: /style.css?v=123 -> /style.css
    let urlPath = (req.url || "/").split("?")[0];

    // Handle CORS preflight requests.
    if (req.method === "OPTIONS") {
        res.writeHead(204, {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "GET,POST,PUT,DELETE,OPTIONS",
            "Access-Control-Allow-Headers": "Content-Type,Authorization"
        });

        res.end();
        return;
    }

    // Only serve GET requests for this static UI server.
    if (req.method !== "GET") {
        res.writeHead(405, {
            "Content-Type": "text/plain"
        });

        res.end("Method Not Allowed");
        return;
    }

    // "/" should serve pages/index.html
    if (urlPath === "/" || urlPath === "") {
        urlPath = "/index.html";
    }

    /*
     * Static file resolution:
     *
     * /              -> pages/index.html
     * /index.html    -> pages/index.html
     * /style.css     -> pages/style.css
     * /app.js        -> dist/app.js
     * /dist/app.js   -> dist/app.js
     */

    let filePath: string;

    // Requests explicitly starting with /dist/ are served from dist/.
    if (urlPath.startsWith("/dist/")) {
        filePath = path.join(process.cwd(), urlPath);
    }

    // JavaScript files are expected to be generated inside dist/.
    else if (urlPath.endsWith(".js")) {
        filePath = path.join(DIST_DIR, path.basename(urlPath));
    }

    // HTML, CSS, images, fonts, etc. are served from pages/.
    else {
        filePath = path.join(PAGES_DIR, urlPath);
    }

    /*
     * SPA fallback:
     *
     * If the requested path doesn't contain a file extension,
     * serve index.html.
     *
     * Example:
     * /dashboard -> pages/index.html
     */
    if (!fs.existsSync(filePath) && !path.extname(urlPath)) {
        filePath = path.join(PAGES_DIR, "index.html");
    }

    serveFile(res, filePath);
});

server.listen(PORT, () => {
    console.log(`\nZimple UI running at http://localhost:${PORT}`);
});